// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

data "aws_availability_zones" "available" {}

module "vpc" {
  source                = "terraform-aws-modules/vpc/aws"

  for_each              = var.datacenter_config

  name                  = "${var.deployment_id}-${each.key}"
  cidr                  = each.value.vpc_cidr
  azs                   = data.aws_availability_zones.available.names
  private_subnets       = each.value.private_subnets
  enable_nat_gateway    = false
  single_nat_gateway    = false
  enable_dns_hostnames  = true
}