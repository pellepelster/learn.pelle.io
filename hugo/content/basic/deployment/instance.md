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

<!--snippet:deploy_aws_ami-->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L1-L5" %}}todo.tf{{% /github %}}
{{< highlight go "linenos=table,linenostart=1,hl_lines=" >}}
data "aws_ami" "amazon_linux2_ami" {
  most_recent = true
  name_regex  = "^amzn2-ami-hvm-"
  owners      = ["137112412989"]
}
{{< / highlight >}}
<!--eos:deploy_aws_ami-->

## SSH key
Of course we not only want to start an instance but we also may need to login to the running instance.  AWS EC2 instances that are booted will use [cloud-init](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/amazon-linux-ami-basics.html#amazon-linux-cloud-init) that lets us add actions that will be executed on instance boot. As the installation of SSH keys is a rather common task cloud-init provides an abstraction for this task that is exposed via AWS and therefore usable in Terraform. We will use a local public ssh key and upload it to our AWS account. The Terraform interpolation syntax not only lets us reference other Terraform resources but also provides various functions we can use. For the SSH key upload we use the `file` function which reads the content of a local file.   

<!--snippet:deploy_aws_key-->
{{% github href="10_basic/30_deployment/deploy/todo.tf#L10-L9" %}}todo.tf{{% /github %}}
{{< highlight go "linenos=table,linenostart=10,hl_lines=" >}}
resource "aws_key_pair" "todo_keypair" {
  key_name   = "todo_keypair"
  public_key = "${file("~/.ssh/id_rsa.pub")}"
}
{{< / highlight >}}
