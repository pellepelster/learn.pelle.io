---
title: "VPC"
date: 2017-12-11T21:35:04+01:00
weight: 40
disableToc: true
---

## VPC
Before we start the first server to host the application we have to lay some groundwork for the instance. In the physical world we would start by setting up a network, adding a connection to the internet so the servers can be reached from the outside and then finally set up a server and connect it to the network.
The same applies for most cloud environments: Before any instances can be launched we have to create a virtual network to associate our server with.

In AWS terms this would be a VPC:

> A virtual private cloud (VPC) is a virtual network dedicated to your AWS account. It is logically isolated from other virtual networks in the AWS Cloud. You can launch your AWS resources, such as Amazon EC2 instances, into your VPC. You can configure your VPC by modifying its IP address range, create subnets, and configure route tables, network gateways, and security settings.
