# snippet:deploy_aws_vpc
resource "aws_vpc" "todo_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
}
# /snippet:deploy_aws_vpc

# snippet:deploy_aws_public_subnet
resource "aws_subnet" "public_subnet" {
  vpc_id                  = "${aws_vpc.todo_vpc.id}"
  cidr_block              = "10.0.0.1/24"
  map_public_ip_on_launch = true
}
# /snippet:deploy_aws_public_subnet

# snippet:deploy_aws_routing
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
# /snippet:deploy_aws_routing
