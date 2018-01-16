provider "aws" {
  region = "eu-central-1" //snippet:deploy_aws_provider
} //eos:deploy_aws_provider

variable "application_jar" {
  type = "string"
}
