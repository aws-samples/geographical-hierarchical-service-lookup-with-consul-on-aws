// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

resource "aws_vpc_endpoint" "s3" {
  for_each        = var.datacenter_config

  vpc_id          = module.vpc[each.key].vpc_id
  service_name    = "com.amazonaws.${var.region}.s3"
  route_table_ids = module.vpc[each.key].private_route_table_ids
}

resource "aws_vpc_endpoint" "ssm" {
  for_each            = var.datacenter_config

  vpc_id              = module.vpc[each.key].vpc_id
  vpc_endpoint_type   = "Interface"
  service_name        = "com.amazonaws.${var.region}.ssm"
  subnet_ids          = [module.vpc[each.key].private_subnets[0]]
  private_dns_enabled = true
  security_group_ids  = [module.sg-endpoint[each.key].security_group_id]
}

resource "aws_vpc_endpoint" "ec2messages" {
  for_each            = var.datacenter_config

  vpc_id              = module.vpc[each.key].vpc_id
  vpc_endpoint_type   = "Interface"
  service_name        = "com.amazonaws.${var.region}.ec2messages"
  subnet_ids          = [module.vpc[each.key].private_subnets[0]]
  private_dns_enabled = true
  security_group_ids  = [module.sg-endpoint[each.key].security_group_id]
}

resource "aws_vpc_endpoint" "ssmmessages" {
  for_each            = var.datacenter_config

  vpc_id              = module.vpc[each.key].vpc_id
  vpc_endpoint_type   = "Interface"
  service_name        = "com.amazonaws.${var.region}.ssmmessages"
  subnet_ids          = [module.vpc[each.key].private_subnets[0]]
  private_dns_enabled = true
  security_group_ids  = [module.sg-endpoint[each.key].security_group_id]
}

resource "aws_vpc_endpoint" "kms" {
  for_each            = var.datacenter_config

  vpc_id              = module.vpc[each.key].vpc_id
  service_name        = "com.amazonaws.${var.region}.kms"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [module.vpc[each.key].private_subnets[0]]
  private_dns_enabled = true
  security_group_ids  = [module.sg-endpoint[each.key].security_group_id]
}

resource "aws_vpc_endpoint" "secretsmanager" {
  for_each            = var.datacenter_config

  vpc_id              = module.vpc[each.key].vpc_id
  service_name        = "com.amazonaws.${var.region}.secretsmanager"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [module.vpc[each.key].private_subnets[0]]
  private_dns_enabled = true
  security_group_ids  = [module.sg-endpoint[each.key].security_group_id]
}

resource "aws_vpc_endpoint" "ec2" {
  for_each            = var.datacenter_config

  vpc_id              = module.vpc[each.key].vpc_id
  service_name        = "com.amazonaws.${var.region}.ec2"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [module.vpc[each.key].private_subnets[0]]
  private_dns_enabled = true
  security_group_ids  = [module.sg-endpoint[each.key].security_group_id]
}

resource "aws_vpc_endpoint" "asg" {
  for_each            = var.datacenter_config

  vpc_id              = module.vpc[each.key].vpc_id
  service_name        = "com.amazonaws.${var.region}.autoscaling"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [module.vpc[each.key].private_subnets[0]]
  private_dns_enabled = true
  security_group_ids  = [module.sg-endpoint[each.key].security_group_id]
}

