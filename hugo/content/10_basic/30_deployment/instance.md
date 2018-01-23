---
title: "Instance"
date: 2017-12-11T21:35:04+01:00
weight: 50
disableToc: true
---

Now that we have a working VPC with an appropiate ip subnet to dpeloy new instances in, we will start with our first EC2 instance. Before we start we only need a few prerequisites, the first one being a disk with an operating system image to boot the instance.

## AMI
In AWS terms those disks are called [AMIs](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html) (Amazon Machine Images) that apart from some metadata contain the acutal system image that is used as a template for the root disk of our instance when we start it.
For now we will use a prebuild AMI from Amazon based on Amazon Linux. Because those images are regulary updated we want to make sure to always use the latest version of this AMI.
Terraform offers the concept of data providers that allow us to fetch oder compute information that is only available outside our Terraform configuration. In our case we use the `aws_ami` data source that lets us filter the list of available AMIs. Because AMIs always belong to an AWS account we have to specify from which account we want to pull the AMI and also how the name of the AMI looks like `^amzn2-ami-hvm-`. Because the `name_regex` will match multiple AMIs (`amzn2-ami-hvm-2017-07-02`, `amzn2-ami-hvm-2017-07-03`, ...) we set `most_recent` to true so only the latest image is returned.

<!-- snippet:deploy_aws_ami -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L1-L5" %}}todo.tf{{% /github %}}
{{< highlight go "linenos=table,linenostart=1,hl_lines=" >}}
data "aws_ami" "amazon_linux2_ami" {
  most_recent = true
  name_regex  = "^amzn2-ami-hvm-"
  owners      = ["137112412989"]
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_ami -->

## SSH key
Of course we not only want to start an instance but we also may need to login to the running instance. AWS EC2 instances that are configured using [cloud-init](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/amazon-linux-ami-basics.html#amazon-linux-cloud-init) that lets us add arbritary actions that will be executed on instance boot. As the installation of SSH keys is a rather common task cloud-init provides an abstraction for this task, that is exposed via AWS and therefore usable in Terraform. We will use a local public ssh key and upload it to our AWS account (if you do net have yet an SSH keypair, have a look [here](https://www.ssh.com/ssh/keygen/), on how to create one).
As we have already seen, Terrsform offers a interpolation functionality, that lets us reference other Terraform resources. Furthermore it also provides various other functions, for example the `file` function that provides access to files from the local filesystem. For the SSH key upload we use the `file` function to read the content of the SSH public key and upload it to AWS.

<!-- snippet:deploy_aws_key -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L7-L10" %}}todo.tf{{% /github %}}
{{< highlight go "linenos=table,linenostart=7,hl_lines=" >}}
resource "aws_key_pair" "todo_keypair" {
  key_name   = "todo_keypair"
  public_key = "${file("~/.ssh/id_rsa.pub")}"
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_key -->

## Application Instance
Now we have finally reached the point where we are able to launch and EC2 instance and provision it with our application. For `ami`, `subnet_id` and `key_name` we use the already created resources and reference them with their id. As `instance_type` we start with `t2.micro` which translates to a single core machine with 1Gb of memory. Have a look [here](https://aws.amazon.com/ec2/instance-types/) for a complete list of available instance types.

<!-- snippet:deploy_aws_instance -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L60-L64" %}}todo.tf{{% /github %}}
{{< highlight go "linenos=table,linenostart=60,hl_lines=" >}}
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
{{< highlight go "linenos=table,linenostart=1,hl_lines=" >}}
variable "application_jar" {
  type = "string"
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_instance_var -->

This variable can now be used with the usual interpolation syntax `var.$variable_name`.

<!-- snippet:deploy_aws_instance_jar -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L66-L73" %}}todo.tf{{% /github %}}
{{< highlight go "linenos=table,linenostart=66,hl_lines=" >}}
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

Now that the application jar is uploaded we have to think how we start the application. We chose an Amazon Linux 2 based image for our instance that comes with [systemd](https://www.freedesktop.org/wiki/Software/systemd/) support by default that replaces the traditional way of starting services.

> systemd is an init system used in Linux distributions to bootstrap the user space and to manage system processes after booting. It is a replacement for the UNIX System V and Berkeley Software Distribution init systems.

Services (and any other resources that are managed by systemd) are called units and configured by unit files. The unit configuration tells systemd what program to start with which user context and also lets us define eventual dependencies on other services.
So as next step we upload a systemd unit to start and stop our application. As the creation of configuration files is a common task when provisioning infrastrucute, terraform includes a templating system supporting all interpolations that are available in HCL. First we have to create the template file.

<!-- file:10_basic/30_deployment/deploy/todo.service.tpl -->
{{% github href="/home/pelle/git/learn.pelle.io/artefacts/10_basic/30_deployment/deploy/todo.service.tpl" %}}todo.service.tpl{{% /github %}}
{{< highlight go "linenos=table,linenostart=,hl_lines=" >}}
[Unit]
Description=myapp
After=syslog.target

[Service]
User=todo
ExecStart=/todo/${var.application_jar}
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
{{< / highlight >}}
<!-- /file:10_basic/30_deployment/deploy/todo.service.tpl -->

And now create the according template datasource. All variables that are used in the template have to be passed via the `vars` block. The template itself can either be given inline or read from a file using the `file` function like in our case.

<!-- snippet:deploy_aws_instance_systemd_unit -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L50-L56" %}}todo.tf{{% /github %}}
{{< highlight go "linenos=table,linenostart=50,hl_lines=" >}}
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
{{< highlight go "linenos=table,linenostart=75,hl_lines=" >}}
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

The last step now is to install java on the machine, create a user to run the application and install and enable the systemd unit. To keep everything neat and tidy we move everything related to the application in a seperate folder that is owned by the same user that is also used to run the application. As a last step we enable the newly created systemd unit so that it started on every system boot and finally start the aplication right away.

<!-- snippet:deploy_aws_instance_install -->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L84-L100" %}}todo.tf{{% /github %}}
{{< highlight go "linenos=table,linenostart=84,hl_lines=" >}}
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

Now that we finally have a definition for the application instance, lets again see what terrsform would do if would ask it to apply the configuration.

```
terraform plan -var 'application_jar=todo-0.1.0.jar'

XXX plan output XXX
```
