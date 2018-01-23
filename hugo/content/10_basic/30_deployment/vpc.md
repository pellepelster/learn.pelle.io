---
title: "VPC"
date: 2017-12-11T21:35:04+01:00
weight: 40
disableToc: true
showHeaderLink: true 
---

Before we start the first server instance to host the application we have to lay some groundwork for the instance. In the physical world we would start by setting up a network, adding a connection to the internet so the servers can be reached from the outside and then finally set up a server and connect it to the network.
The same applies for most cloud environments: Before any instances can be launched we have to create a virtual network to associate our server with.

In AWS terms this would be a [VPC](https://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/VPC_Subnets.html):

> A virtual private cloud (VPC) is a virtual network dedicated to your AWS account. It is logically isolated from other virtual networks in the AWS Cloud. You can launch your AWS resources, such as Amazon EC2 instances, into your VPC. You can configure your VPC by modifying its IP address range, create subnets, and configure route tables, network gateways, and security settings.

Lets start with the VPC definition, we pick a private non-routed ip subnet from [RFC1918](https://tools.ietf.org/html/rfc1918) to avoid interference with any other public networks.

<!-- snippet:deploy_aws_vpc -->
{{% github href="10_basic/30_deployment/deploy/vpc.tf#L1-L4" %}}vpc.tf{{% /github %}}
{{< highlight go "" >}}
resource "aws_vpc" "todo_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_vpc -->

Now we assign a public subnet to our VPC, this is where the servers we are about to will create live in. The attachment to our existing VPC is created via the attribute `vpc_id`, where the id of the previously defined VPC is referenced. The syntax for referencing resources in Terraform is `${TYPE.NAME.ATTRIBUTE}`, where `TYPE` is the resource type, in this case `aws_vpc`, `NAME` the resource name (here `todo_vpc`) and attribute specifies the VPCs id.
Of course those private networks are not available from outside AWS so we configure `map_public_ip_on_launch` to `true` which means that a publicly reachable ip address gets assigned to all new instances that are launched ins this subnet.

<!-- snippet:deploy_aws_public_subnet -->
{{% github href="10_basic/30_deployment/deploy/vpc.tf#L6-L10" %}}vpc.tf{{% /github %}}
{{< highlight go "" >}}
resource "aws_subnet" "public_subnet" {
  vpc_id                  = "${aws_vpc.todo_vpc.id}"
  cidr_block              = "10.0.0.1/24"
  map_public_ip_on_launch = true
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_public_subnet -->

The last thing we need to enable internet connectivity for instances in our VPC is an [internet gateway](https://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/VPC_Internet_Gateway.html) that enables access to the internet for instances with an public ip address. Like in the real world we simply add a default route pointing to this gateway so all instances in our VPC know how to reach addresses outside of the VPC.

<!-- snippet:deploy_aws_routing -->
{{% github href="10_basic/30_deployment/deploy/vpc.tf#L12-L28" %}}vpc.tf{{% /github %}}
{{< highlight go "" >}}
resource "aws_internet_gateway" "todo_internet_gateway" {
  vpc_id = "${aws_vpc.todo_vpc.id}"
}

resource "aws_route_table" "todo_main_route_table" {
  vpc_id = "${aws_vpc.todo_vpc.id}"

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.todo_internet_gateway.id}"
  }
}

resource "aws_main_route_table_association" "todo_main_route_table_association" {
  vpc_id         = "${aws_vpc.todo_vpc.id}"
  route_table_id = "${aws_route_table.todo_main_route_table.id}"
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_routing -->

Now that we have a complete configuration for the VPC lets see what Terraform makes out of it. First we call `terraform plan' again to see what terraform would do with our configuration.

```
$ export AWS_ACCESS_KEY_ID="${access_key_id}"
$ export AWS_SECRET_ACCESS_KEY="${secret_access_key}"
$ terraform plan

XXX insert plan output XXX
```

Terraform just compared it internal state that is stored in the `terraform.tfstate*` files against the running configuration in AWS. As nothing is yet available in AWS Terraform show that it would create new resources for every item in our configuration.

{{% notice tip %}}
Terraform uses its state to keep track of the mappings between our configuration and the actual resources. It is also used to keep track of additional metadata needed by Terraform (like for example resource dependencies). The state also helps to boost performance as it would not be feasible to gather the state from AWS on each run.
Due to its nature the state may contain sensitive information, so be careful when handling the state files and never commit them to a public repository.
Terraform provides multiple methods to store state apart from the local storage, we will visit them in later chapters.
{{% /notice %}}

Now that we checked the plan lets finally apply it and create our first AWS resources.

```
$ export AWS_ACCESS_KEY_ID="${access_key_id}"
$ export AWS_SECRET_ACCESS_KEY="${secret_access_key}"
$ terraform plan

XXX insert apply output XXX
```

Terraform now created an AWS VPC according to our configuration, when we log in into the AWS console we can see the VPC, the associated network and its routing configuration including the internet gateway.

![Screenshot](http://via.placeholder.com/800x600)

{{% notice tip %}}
It is a good habit to always do a `terraform plan` before executing a `terraform apply`. Working this way makes it easy to do quick sanity check and spot mistakes that would result in catastrophic results when applied.  
{{% /notice %}}
