---
title: "Terraform"
date: 2017-12-11T21:35:04+01:00
weight: 30
disableToc: true
---

## Terraform
We now have to choose which infrastructure as code software to use. When it comes to the sheer number of supported cloud providers [Terraform](https://www.terraform.io/) is the tool of our choiche. All major cloud providers contribute up-to-date plugins that abstract their apis into the text based declarative Terraform configuration language which is based on the text based [HashiCorp configuration language (HCL)](https://github.com/hashicorp/hcl).

## Initial Setup
First we need to initialize the terraform working directory by running `terraform init` which will initialize the working directory and also download any needed plugins needed to execute our terraform configuration files.
To keep everything together create a directory `deploy`

```
cd deploy
terraform init
```

As terraform needs to interact with the AWS api it obviously needs credentials. Like other AWS SDK based software the terraform [AWS provider](https://www.terraform.io/docs/providers/aws/) has several methods to retrieve the needed credentials. We will inject the credentials via environment variables to avoid adding the secrets the the source file and accidentially commiting them.

For all following steps we always need the AWS secrets, have a look [here](https://docs.aws.amazon.com/general/latest/gr/managing-aws-access-keys.html) on how to create them for your AWS account.

## Preview

```
export AWS_ACCESS_KEY_ID="${access_key_id}"
export AWS_SECRET_ACCESS_KEY="${secret_access_key}"
terraform plan
```
