---
title: "Terraform"
date: 2017-12-11T21:35:04+01:00
weight: 30
disableToc: true
---

## Terraform
We now have to choose which infrastructure as code software to use. When it comes to the sheer number of supported cloud providers [Terraform](https://www.terraform.io/) is the tool of our choiche. All major cloud providers contribute up-to-date plugins that abstract their apis into the text based declarative Terraform configuration language which is based on the text based [HashiCorp configuration language (HCL)](https://github.com/hashicorp/hcl).

## Initial Setup
First we have to tell Terraform which provider we are going to use, in our case the AWS provider using `eu-central-1` as the default zone for alle created resources:

<!--snippet:deploy_aws_provider-->
{{% github href="10_basic/30_deployment/deploy/providers.tf#L1-L3" %}}providers.tf{{% /github %}}
{{< highlight go "linenos=table,linenostart=1,hl_lines=" >}}
provider "aws" { 
  region = "eu-central-1"
} 
{{< / highlight >}}
<!--eos:deploy_aws_provider-->

Now we can initialize the terraform working directory by running `terraform init` which will initialize the working directory and download any plugins needed to execute our terraform configuration files.

```
$ cd deploy
$ terraform init

Initializing provider plugins...
- Checking for available provider plugins on https://releases.hashicorp.com...
- Downloading plugin for provider "aws" (1.7.0)...
- Downloading plugin for provider "template" (1.0.0)...

The following providers do not have any version constraints in configuration,
so the latest version was installed.

To prevent automatic upgrades to new major versions that may contain breaking
changes, it is recommended to add version = "..." constraints to the
corresponding provider blocks in configuration, with the constraint strings
suggested below.

* provider.aws: version = "~> 1.7"
* provider.template: version = "~> 1.0"

Terraform has been successfully initialized!

You may now begin working with Terraform. Try running "terraform plan" to see
any changes that are required for your infrastructure. All Terraform commands
should now work.

If you ever set or change modules or backend configuration for Terraform,
rerun this command to reinitialize your working directory. If you forget, other
commands will detect it and remind you to do so if necessary.
```

As terraform needs to interact with the AWS api it obviously needs credentials. Like other AWS SDK based software the terraform [AWS provider](https://www.terraform.io/docs/providers/aws/) has several methods to retrieve the needed credentials. We will inject the credentials via environment variables to avoid adding the secrets the the source file and accidentially commiting them.

For all following steps we always need the AWS secrets, have a look [here](https://docs.aws.amazon.com/general/latest/gr/managing-aws-access-keys.html) on how to create them for your AWS account.

## Preview

```
$ export AWS_ACCESS_KEY_ID="${access_key_id}"
$ export AWS_SECRET_ACCESS_KEY="${secret_access_key}"
$ terraform plan

Refreshing Terraform state in-memory prior to plan...
The refreshed state will be used to calculate this plan, but will not be
persisted to local or remote state storage.


------------------------------------------------------------------------

No changes. Infrastructure is up-to-date.

This means that Terraform did not detect any differences between your
configuration and real physical resources that exist. As a result, no
actions need to be performed.
```
