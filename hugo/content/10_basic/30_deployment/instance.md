---
title: "Instance"
date: 2017-12-11T21:35:04+01:00
weight: 50
disableToc: true
showHeaderLink: true 
---

Now that we have a working VPC with an appropriate ip subnet to deploy new instances in, we will start with our first EC2 instance. Before we start we only need a few prerequisites, the first one being a disk with an operating system image to boot the instance.

## AMI
In AWS terms those disks are called [AMIs](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html) (Amazon Machine Images) that apart from some meta data contain the system image that is used as a template for the root disk of our instance when we start it.
For now we will use a prebuild AMI from Amazon based on Amazon Linux. Because those images are regularly updated we want to make sure to always use the latest version of this AMI.
Terraform offers the concept of data providers that allow us to fetch or compute information that is only available outside our Terraform configuration. In our case we use the `aws_ami` data source that lets us filter the list of available AMIs. Because AMIs always belong to an AWS account we have to specify from which account we want to pull the AMI and also how the name of the AMI looks like (in our case `^amzn2-ami-hvm-`). Because the `name_regex` will match multiple AMIs (`amzn2-ami-hvm-2017-07-02`, `amzn2-ami-hvm-2017-07-03`, ...) we set `most_recent` to true so only the latest image is returned.

<!-- snippet:deploy_aws_ami -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L1-L5" %}}todo.tf{{% /github %}}
{{< highlight go "" >}}
data "aws_ami" "amazon_linux2_ami" {
  most_recent = true
  name_regex  = "^amzn2-ami-hvm-"
  owners      = ["137112412989"]
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_ami -->

## SSH key
Of course we not only want to start an instance but we also may need to login to the running instance to debug problems. AWS EC2 instances that are configured using [cloud-init](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/amazon-linux-ami-basics.html#amazon-linux-cloud-init) that lets us add arbitrary actions that will be executed on instance boot. As the installation of SSH keys is a rather common task cloud-init provides an abstraction for this task, that is exposed via AWS and therefore usable in Terraform. We will use a local public ssh key and upload it to our AWS account (if you do not have yet an SSH keypair, have a look [here](https://www.ssh.com/ssh/keygen/), on how to create one).
As we have already seen, Terraform offers a interpolation functionality, that lets us reference other Terraform resources. Furthermore it also provides various other functions, for example the `file` function that provides access to files from the local file system. For the SSH key upload we use the `file` function to read the content of the SSH public key and upload it to AWS.

<!-- snippet:deploy_aws_key -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L7-L10" %}}todo.tf{{% /github %}}
{{< highlight go "" >}}
resource "aws_key_pair" "todo_keypair" {
  key_name   = "todo_keypair"
  public_key = "${file("~/.ssh/id_rsa.pub")}"
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_key -->

## Network Security

Although t VPC we created beforehand provides total network isolation from other VPCs we can further refine the access to our instance by creating firewall rules which in AWS terms are called security group.
When comparing these security groups to traditional firewall rules there is a small but important difference. Firewall rules normally operate on a ip subnet level, for example you could say "please allow all traffic to subnet 192.168.1.0/24 from host 10.12.34.5". Security groups on the other hand are a virtual construct that operates on instance level, for example "please allow all traffic to this machine coming from 10.4.1.0/24". This concept abstracts away the problems we would otherwise encounter when we start to distribute our subnets across multiple availability zones have a look at this [article](https://blog.rackspace.com/network-segregation-aws-2) to get a short overview of VPC networking. 

For our application that means that we have to define some security groups to allow access from the outside as the default of course is to deny any traffic.

<!-- snippet:deploy_aws_security_group_http -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L31-L48" %}}todo.tf{{% /github %}}
{{< highlight go "" >}}
resource "aws_security_group" "todo_instance_http_security_group" {
  name   = "todo_instance_http_security_group"
  vpc_id = "${aws_vpc.todo_vpc.id}"

  ingress {
    from_port   = 9090
    to_port     = 9090
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_security_group_http -->

We allow all incoming traffic from anywhere to port 9090 and for do not impose any limitations to the outgoing traffic.


## Application Instance

Now we have finally reached the point where we are able to launch and EC2 instance and provision it with our application. For `ami`, `subnet_id` and `key_name` we use the already created resources and reference them with their id. As `instance_type` we start with `t2.micro` which translates to a single core machine with 1Gb of memory. Have a look [here](https://aws.amazon.com/ec2/instance-types/) for a complete list of available instance types.

<!-- snippet:deploy_aws_instance -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L60-L64" %}}todo.tf{{% /github %}}
{{< highlight go "" >}}
resource "aws_instance" "todo_instance" {

 [..]

  ami             = "${data.aws_ami.amazon_linux2_ami.id}"
  subnet_id       = "${aws_subnet.public_subnet.id}"
  instance_type   = "t2.micro"
  key_name        = "${aws_key_pair.todo_keypair.id}"
  security_groups = ["${aws_security_group.todo_instance_ssh_security_group.id}", "${aws_security_group.todo_instance_http_security_group.id}"]

 [..]

}
{{< / highlight >}}
<!-- /snippet:deploy_aws_instance -->

Terraform comes with various provisioners that can be used to configure instances. We will use the `file` provisioner to copy local files to the remote machine, and the `remote-exec` provisioner to execute the commands needed to install the application on the instance. By default the provisioners use SSH as backend, therefore we have to specify the remote user user which is `ec2-user` by default for all Amazon Linux based instances.

To avoid hard coding the path of the jar file (as it may change depending on build-version) we replace the source for the file provisioner with a variable that we have to define beforehand.

<!-- snippet:deploy_aws_instance_var -->
{{% github href="10_basic/30_deployment/deploy/variables.tf#L1-L3" %}}variables.tf{{% /github %}}
{{< highlight go "" >}}
variable "application_jar" {
  type = "string"
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_instance_var -->

This variable can now be used with the usual interpolation syntax `var.$variable_name`.

<!-- snippet:deploy_aws_instance_jar -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L66-L73" %}}todo.tf{{% /github %}}
{{< highlight go "" >}}
resource "aws_instance" "todo_instance" {

 [..]

  provisioner "file" {
    source      = "../todo-server/build/libs/${var.application_jar}"
    destination = "${var.application_jar}"

    connection {
      user = "ec2-user"
    }
  }

 [..]

}
{{< / highlight >}}
<!-- /snippet:deploy_aws_instance_jar -->

Now that the application jar is uploaded we have to think how we start the application. We chose an Amazon Linux 2 based image for our instance that comes with [systemd](https://www.freedesktop.org/wiki/Software/systemd/) support by default, replacing the traditional way of starting services.

> systemd is an init system used in Linux distributions to bootstrap the user space and to manage system processes after booting. It is a replacement for the UNIX System V and Berkeley Software Distribution init systems.

Services (and any other resources that are managed by systemd) are called units and configured by unit files. The unit configuration tells systemd what program to start with which user context and also lets us define eventual dependencies on other services.
So as next step we upload a systemd unit to start and stop our application. As the creation of configuration files is a common task when provisioning infrastructure, Terraform includes a templating system supporting all interpolations that are available in HCL. First we have to create the template file.

<!-- file:10_basic/30_deployment/deploy/todo.service.tpl -->
{{% github href="/10_basic/30_deployment/deploy/todo.service.tpl" %}}todo.service.tpl{{% /github %}}
{{< highlight go "" >}}
[Unit]
Description=todo application
After=syslog.target

[Service]
User=todo
ExecStart=/todo/${application_jar}
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
{{< / highlight >}}
<!-- /file:10_basic/30_deployment/deploy/todo.service.tpl -->

And now create the according template datasource. All variables that are used in the template have to be passed via the `vars` block. The template itself can either be given inline or read from a file using the `file` function like in our case.

<!-- snippet:deploy_aws_instance_systemd_unit -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L50-L56" %}}todo.tf{{% /github %}}
{{< highlight go "" >}}
data "template_file" "todo_systemd_service" {
  template = "${file("todo.service.tpl")}"

  vars {
    application_jar = "${var.application_jar}"
  }
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_instance_systemd_unit -->

Now we copy the processed template to the server instance. The rendered output can be accessed by the `.rendered` attribute of the template datasource.

<!-- snippet:deploy_aws_instance_systemd -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L75-L82" %}}todo.tf{{% /github %}}
{{< highlight go "" >}}
resource "aws_instance" "todo_instance" {

 [..]

  provisioner "file" {
    content     = "${data.template_file.todo_systemd_service.rendered}"
    destination = "todo.service"

    connection {
      user = "ec2-user"
    }
  }

 [..]

}
{{< / highlight >}}
<!-- /snippet:deploy_aws_instance_systemd -->

The last step now is to install Java on the machine, create a user to run the application and install and enable the systemd unit. To keep everything neat and tidy we move everything related to the application in a separate folder that is owned by the same user that is also used to run the application. As a last step we enable the newly created systemd unit so that it started on every system boot and finally start the application right away.

<!-- snippet:deploy_aws_instance_install -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L84-L100" %}}todo.tf{{% /github %}}
{{< highlight go "" >}}
resource "aws_instance" "todo_instance" {

 [..]

  provisioner "remote-exec" {
    inline = [
      "sudo yum install -y java",
      "sudo useradd todo",
      "sudo mkdir /todo",
      "sudo mv ~ec2-user/todo.service /etc/systemd/system/",
      "sudo mv ~ec2-user/${var.application_jar} /todo",
      "sudo chmod +x /todo/${var.application_jar}",
      "sudo chown -R todo:todo /todo",
      "sudo systemctl enable todo",
      "sudo systemctl start todo",
    ]

    connection {
      user = "ec2-user"
    }
  }

 [..]

}
{{< / highlight >}}
<!-- /snippet:deploy_aws_instance_install -->

Now we are almost ready to start the instance apart from the fact that we will have no idea how to reach the instance as soon at is started as we do not know what ip it will have after start. Luckily terraform not only has variables that serve as input for terraform configurations, but also outputs that will show any data that is exposed by terraform resources. We ware interested in the public dns address of the created instance, so we expose this information with the output named `instance_fqdn`. The result will be a generic hostname in the format `${id}.eu-central-1.compute.amazonaws.com` in later chapter we will learn to hide this information behind proper domain names. 

<!-- snippet:deploy_aws_instance_output -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L104-L106" %}}todo.tf{{% /github %}}
{{< highlight go "" >}}
output "instance_fqdn" {
  value = "${aws_instance.todo_instance.public_dns}"
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_instance_output -->

Now that we finally have a definition for the application instance, lets again see what Terraform would do if would ask it to apply the configuration. Keep in mind that we now have to specify the name of the application jar on each run, as we did not provide a default for the variable.

```
$ terraform plan -var 'application_jar=todo-0.0.1.jar'

Refreshing Terraform state in-memory prior to plan...
The refreshed state will be used to calculate this plan, but will not be
persisted to local or remote state storage.

data.template_file.todo_systemd_service: Refreshing state...
data.aws_ami.amazon_linux2_ami: Refreshing state...

------------------------------------------------------------------------

An execution plan has been generated and is shown below.
Resource actions are indicated with the following symbols:
  + create

Terraform will perform the following actions:

  + aws_instance.todo_instance
      id:                                          <computed>
      ami:                                         "ami-1b2bb774"
      associate_public_ip_address:                 <computed>
      availability_zone:                           <computed>
      ebs_block_device.#:                          <computed>
      ephemeral_block_device.#:                    <computed>
      instance_state:                              <computed>
      instance_type:                               "t2.micro"
      ipv6_address_count:                          <computed>
      ipv6_addresses.#:                            <computed>
      key_name:                                    "${aws_key_pair.todo_keypair.id}"
      network_interface.#:                         <computed>
      network_interface_id:                        <computed>
      placement_group:                             <computed>
      primary_network_interface_id:                <computed>
      private_dns:                                 <computed>
      private_ip:                                  <computed>
      public_dns:                                  <computed>
      public_ip:                                   <computed>
      root_block_device.#:                         <computed>
      security_groups.#:                           <computed>
      source_dest_check:                           "true"
      subnet_id:                                   "${aws_subnet.public_subnet.id}"
      tenancy:                                     <computed>
      volume_tags.%:                               <computed>
      vpc_security_group_ids.#:                    <computed>

  + aws_internet_gateway.todo_internet_gateway
      id:                                          <computed>
      vpc_id:                                      "${aws_vpc.todo_vpc.id}"

  [...]

  + aws_vpc.todo_vpc
      id:                                          <computed>
      assign_generated_ipv6_cidr_block:            "false"
      cidr_block:                                  "10.0.0.0/16"
      default_network_acl_id:                      <computed>
      default_route_table_id:                      <computed>
      default_security_group_id:                   <computed>
      dhcp_options_id:                             <computed>
      enable_classiclink:                          <computed>
      enable_classiclink_dns_support:              <computed>
      enable_dns_hostnames:                        "true"
      enable_dns_support:                          "true"
      instance_tenancy:                            <computed>
      ipv6_association_id:                         <computed>
      ipv6_cidr_block:                             <computed>
      main_route_table_id:                         <computed>


Plan: 9 to add, 0 to change, 0 to destroy.

------------------------------------------------------------------------

Note: You didn't specify an "-out" parameter to save this plan, so Terraform
can't guarantee that exactly these actions will be performed if
"terraform apply" is subsequently run.

```

We see that terraform wants to create all the resources we just defined, so we move forward and let terraform do its work by applying the configuration:

```
$ terraform apply -var 'application_jar=todo-0.0.1.jar'

[...]

aws_instance.todo_instance (remote-exec): Complete!
aws_instance.todo_instance (remote-exec): Created symlink from /etc/systemd/system/multi-user.target.wants/todo.service to /etc/systemd/system/todo.service.
aws_instance.todo_instance: Creation complete after 1m10s (ID: i-0d11d1515b77d6557)

Apply complete! Resources: 9 added, 0 changed, 0 destroyed.

Outputs:

instance_fqdn = ec2-35-158-106-80.eu-central-1.compute.amazonaws.com
```

Et voila, that configuration is applied and as we can see from the output `instance_fqdn` our application is happily humming along at http://${instance_fqdn}:9090.


{{< figure src="../first_deployment.png" title="First deployment" >}}


## Cleanup

Congratulations you made your first deployment, please do not forget to delete the resources we just created, because otherwise you may have to pay for them if you exceed the AWS free tier offerings.

```
$ terraform destroy --force -var 'application_jar=todo-0.0.1.jar'

[...]

An execution plan has been generated and is shown below.
Resource actions are indicated with the following symbols:
  - destroy

Terraform will perform the following actions:

  - aws_instance.todo_instance

  - aws_internet_gateway.todo_internet_gateway

  - aws_key_pair.todo_keypair

  - aws_main_route_table_association.todo_main_route_table_association

  - aws_route_table.todo_main_route_table

  - aws_security_group.todo_instance_http_security_group

  - aws_security_group.todo_instance_ssh_security_group

  - aws_subnet.public_subnet

  - aws_vpc.todo_vpc


Plan: 0 to add, 0 to change, 9 to destroy.

[...]

aws_main_route_table_association.todo_main_route_table_association: Destroying... (ID: rtbassoc-13e08a79)
aws_instance.todo_instance: Destroying... (ID: i-0a7b3ee83b5f1be70)
aws_main_route_table_association.todo_main_route_table_association: Destruction complete after 0s
aws_route_table.todo_main_route_table: Destroying... (ID: rtb-2f5c0e44)
aws_route_table.todo_main_route_table: Destruction complete after 1s
aws_internet_gateway.todo_internet_gateway: Destroying... (ID: igw-1a83e772)
aws_instance.todo_instance: Still destroying... (ID: i-0a7b3ee83b5f1be70, 10s elapsed)
aws_internet_gateway.todo_internet_gateway: Still destroying... (ID: igw-1a83e772, 10s elapsed)
aws_instance.todo_instance: Still destroying... (ID: i-0a7b3ee83b5f1be70, 20s elapsed)
aws_internet_gateway.todo_internet_gateway: Still destroying... (ID: igw-1a83e772, 20s elapsed)

[...]

aws_vpc.todo_vpc: Destroying... (ID: vpc-0a0aa161)
aws_vpc.todo_vpc: Destruction complete after 0s

Destroy complete! Resources: 9 destroyed.
```

Now everything we created is destroyed, but as we have the code we can create it again anytime we want.