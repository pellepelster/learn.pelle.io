---
title: "Terraform"
date: 2017-12-11T21:35:04+01:00
weight: 30
disableToc: true
showHeaderLink: true 
---

We now have to choose which infrastructure as code software to use. When it comes to the sheer number of supported cloud providers [Terraform](https://www.terraform.io/) is the tool of our choice. All major cloud providers contribute up-to-date plugins that abstract their apis into the declarative Terraform configuration language which is based on the text based [HashiCorp configuration language (HCL)](https://github.com/hashicorp/hcl).

## Initial Setup
First we have to tell Terraform which provider we are going to use, in our case the AWS provider using `eu-central-1` as the default region for alle created resources. In AWS terms a region translate to a datacenter where our resources are launched. It is important to keep in mind that not all AWS services are always available in all regions, also the prices may differ depending on the region, have a look [here](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html) for more in dpeth information about regions

<!-- snippet:deploy_aws_provider -->
{{% github href="10_basic/30_deployment/deploy/providers.tf#L1-L3" %}}providers.tf{{% /github %}}
{{< highlight go "" >}}
provider "aws" {
  region = "eu-central-1"
}
{{< / highlight >}}
<!-- /snippet:deploy_aws_provider -->

Now we can initialize the working directory by running `terraform init` which will scan all Terraform files for referenced plugins and download them as needed.

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
[...]
```
{{% notice tip %}}
`terraform init` by default loads the latest version of a plugin. This may lead to problems if for example plugins introduce breaking changes, in that case it is possible to pin the version of a plugin by specifying a version number:
`version = "~> 1.0`
{{% /notice %}}

## Testing the setup
As terraform needs to interact with the AWS api it obviously needs credentials. Like other AWS SDK based software the Terraform [AWS provider](https://www.terraform.io/docs/providers/aws/) has several methods to retrieve the needed credentials. We will inject the credentials via environment variables to avoid adding the secrets the the source file and accidentally committing them.

For all following steps we always need the AWS secrets, have a look [here](https://docs.aws.amazon.com/general/latest/gr/managing-aws-access-keys.html) on how to create them for your AWS account.

To check if the secret key setup was successful lets try to run Terraform. Luckily for us Terraform provides a non-destructive way to preview the changes that it would apply to the infrastructure without actually changing anything: the plan command:

> This command is a convenient way to check whether the execution plan for a set of changes matches your expectations without making any changes to real resources or to the state. For example, Terraform plan might be run before committing a change to version control, to create confidence that it will behave as expected.

```
$ export AWS_ACCESS_KEY_ID="${access_key_id}"
$ export AWS_SECRET_ACCESS_KEY="${secret_access_key}"
$ terraform plan

Refreshing Terraform state in-memory prior to plan...
The refreshed state will be used to calculate this plan, but will not be
persisted to local or remote state storage.


------------------------------------------------------------------------

No changes. Infrastructure is up-to-date.
[...]
```

What happened is that Terraform compared the desired state against the actual running state in the cloud. As we don't have any resources defined yet Terraform didn't detect anything it could to and came up with no changes.
