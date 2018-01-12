---
title: "VPC"
date: 2017-12-11T21:35:04+01:00
weight: 40
disableToc: true
---

## VPC

Before we start the first server for the application we have to lay some groundwork for the instance to run in. In the physical world we would start by setting up a network, adding a connection to the internet so the servers can be reached from the outside and then finally set up a server and connect it to the network.
The same applies for most cloud environments: Before any instances can be launched we have to create a virtual network to associate our server with.

In AWS terms this would be a VPC:

> A virtual private cloud (VPC) is a virtual network dedicated to your AWS account. It is logically isolated from other virtual networks in the AWS Cloud. You can launch your AWS resources, such as Amazon EC2 instances, into your VPC. You can configure your VPC by modifying its IP address range, create subnets, and configure route tables, network gateways, and security settings.



## Terraform setup

First we have to initialize the terraform working directory by running `terraform init` which will initialize the working directory and also download any needed plugins needed to execute our terraform configuration files.

```
terraform init
```

As terraform need to interact with the AWS api in order to create the needed resources it obviously needs credentials. Like other AWS SDK based software the terraform [AWS provider](https://www.terraform.io/docs/providers/aws/) has several methods to retrieve the needed credentials. We will inject the credentials via environment variables to avoid adding the secrets the the source file and accidentially commiting them.

For the next steps we always the AWS secrets, have a look [here](https://docs.aws.amazon.com/general/latest/gr/managing-aws-access-keys.html) on how to create them for your AWS account.

## Preview

```
export AWS_ACCESS_KEY_ID="${access_key_id}"
export AWS_SECRET_ACCESS_KEY="${secret_access_key}"
terraform plan
```
