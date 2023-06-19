// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

module "tgw" {
  source  = "terraform-aws-modules/transit-gateway/aws"
  version = "2.8.0"

  name        = var.deployment_id

  enable_auto_accept_shared_attachments  = true
  ram_allow_external_principals          = false
  share_tgw                              = false
  

  vpc_attachments = {
    vpc1 = {
      vpc_id       = module.vpc["dc1"].vpc_id
      subnet_ids   = module.vpc["dc1"].private_subnets
    },
    vpc2 = {
      vpc_id       = module.vpc["dc2"].vpc_id
      subnet_ids   = module.vpc["dc2"].private_subnets
    }
  }
}

resource "aws_route" "dc1_private_tgw_route_a" {
  for_each                  = var.datacenter_config

  route_table_id            = module.vpc["dc1"].private_route_table_ids[0]
  destination_cidr_block    = "10.200.0.0/16"
  transit_gateway_id        = module.tgw.ec2_transit_gateway_id
}

resource "aws_route" "dc2_private_tgw_route_a" {
  for_each                  = var.datacenter_config

  route_table_id            = module.vpc["dc2"].private_route_table_ids[0]
  destination_cidr_block    = "10.100.0.0/16"
  transit_gateway_id        = module.tgw.ec2_transit_gateway_id
}

resource "aws_route" "dc1_private_tgw_route_b" {
  for_each                  = var.datacenter_config

  route_table_id            = module.vpc["dc1"].private_route_table_ids[1]
  destination_cidr_block    = "10.200.0.0/16"
  transit_gateway_id        = module.tgw.ec2_transit_gateway_id
}

resource "aws_route" "dc2_private_tgw_route_b" {
  for_each                  = var.datacenter_config

  route_table_id            = module.vpc["dc2"].private_route_table_ids[1]
  destination_cidr_block    = "10.100.0.0/16"
  transit_gateway_id        = module.tgw.ec2_transit_gateway_id
}

resource "aws_route" "dc1_private_tgw_route_c" {
  for_each                  = var.datacenter_config

  route_table_id            = module.vpc["dc1"].private_route_table_ids[2]
  destination_cidr_block    = "10.200.0.0/16"
  transit_gateway_id        = module.tgw.ec2_transit_gateway_id
}

resource "aws_route" "dc2_private_tgw_route_c" {
  for_each                  = var.datacenter_config

  route_table_id            = module.vpc["dc2"].private_route_table_ids[2]
  destination_cidr_block    = "10.100.0.0/16"
  transit_gateway_id        = module.tgw.ec2_transit_gateway_id
}