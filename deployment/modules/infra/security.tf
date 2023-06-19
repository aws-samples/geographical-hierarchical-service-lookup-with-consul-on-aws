// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

module "sg-ssh" {
  source = "terraform-aws-modules/security-group/aws"
  version     = "4.9.0"

  for_each    = module.vpc

  name        = "${var.deployment_id}-ssh"
  vpc_id      = each.value.vpc_id

  ingress_with_cidr_blocks = [
    {
      rule        = "ssh-tcp"
      cidr_blocks = "${each.value.vpc_cidr_block}"
    }
  ]

  egress_with_cidr_blocks = [
    {
      rule        = "ssh-tcp"
      cidr_blocks = "${each.value.vpc_cidr_block}"
    }
  ]
}

module "sg-consul" {
  source = "terraform-aws-modules/security-group/aws"
  version     = "4.9.0"

  for_each    = module.vpc

  name        = "${var.deployment_id}-consul"
  vpc_id      = each.value.vpc_id

  ingress_with_cidr_blocks = [
    {
      from_port   = 8300
      to_port     = 8301
      protocol    = "tcp"
      description = "consul-rpc-lan-serf-gosspip-tcp"
      cidr_blocks = "${each.value.vpc_cidr_block}"
    },
    {
      from_port   = 8300
      to_port     = 8301
      protocol    = "udp"
      description = "consul-lan-serf-gosspip-udp"
      cidr_blocks = "${each.value.vpc_cidr_block}"
    },
    {
      from_port   = 8500
      to_port     = 8502
      protocol    = "tcp"
      description = "consul-http-https-api-tcp"
      cidr_blocks = "${each.value.vpc_cidr_block}"
    },
    {
      from_port   = 8600
      to_port     = 8600
      protocol    = "tcp"
      description = "consul-dns-tcp"
      cidr_blocks = "${each.value.vpc_cidr_block}"
    },
    {
      from_port   = 8600
      to_port     = 8600
      protocol    = "udp"
      description = "consul-dns-udp"
      cidr_blocks = "${each.value.vpc_cidr_block}"
    },
    {
      from_port   = var.consul_serf_lan_port
      to_port     = var.consul_serf_lan_port
      protocol    = "tcp"
      description = "consul-lan-serf-gosspip-tcp"
      cidr_blocks = "${each.value.vpc_cidr_block}"
    },
    {
      from_port   = var.consul_serf_lan_port
      to_port     = var.consul_serf_lan_port
      protocol    = "udp"
      description = "consul-lan-serf-gosspip-udp"
      cidr_blocks = "${each.value.vpc_cidr_block}"
    }
  ]

  egress_with_cidr_blocks = [
    {
      from_port   = 0
      to_port     = 0
      protocol    = "-1"
      description = "any-any"
      cidr_blocks = "0.0.0.0/0"
    }
  ]
}

module "sg-consul-wan" {
  source = "terraform-aws-modules/security-group/aws"
  version     = "4.9.0"

  for_each    = module.vpc

  name        = "${var.deployment_id}-consul-wan"
  vpc_id      = each.value.vpc_id

  ingress_with_cidr_blocks = [
    {
      from_port   = 8300
      to_port     = 8300
      protocol    = "tcp"
      description = "consul-server-rpc-tcp"
      cidr_blocks = "10.0.0.0/8"
    },
    {
      from_port   = 8302
      to_port     = 8302
      protocol    = "tcp"
      description = "consul-wan-serf-gosspip-tcp"
      cidr_blocks = "10.0.0.0/8"
    },
    {
      from_port   = 8302
      to_port     = 8302
      protocol    = "udp"
      description = "consul-wan-serf-gosspip-udp"
      cidr_blocks = "10.0.0.0/8"
    }
  ]

  egress_with_cidr_blocks = [
    {
      from_port   = 0
      to_port     = 0
      protocol    = "-1"
      description = "any-any"
      cidr_blocks = "0.0.0.0/0"
    }
  ]
}

module "sg-fake-service" {
  source = "terraform-aws-modules/security-group/aws"
  version     = "4.9.0"

  for_each    = module.vpc

  name        = "${var.deployment_id}-fake-service"
  vpc_id      = each.value.vpc_id

  ingress_with_cidr_blocks = [
    {
      from_port   = 9090
      to_port     = 9090
      protocol    = "tcp"
      description = "fake-service-tcp"
      cidr_blocks = "10.0.0.0/8"
    }
  ]

  egress_with_cidr_blocks = [
    {
      from_port   = 0
      to_port     = 0
      protocol    = "-1"
      description = "any-any"
      cidr_blocks = "0.0.0.0/0"
    }
  ]
}

module "sg-endpoint" {
  source = "terraform-aws-modules/security-group/aws"
  version     = "4.9.0"

  for_each    = module.vpc

  name        = "${var.deployment_id}-sg-endpoint"
  vpc_id      = each.value.vpc_id

  ingress_with_cidr_blocks = [
    {
      from_port   = 443
      to_port     = 443
      protocol    = "tcp"
      description = "tls-endpoint"
      cidr_blocks = "10.0.0.0/8"
    }
  ]

  egress_with_cidr_blocks = [
    {
      from_port   = 0
      to_port     = 0
      protocol    = "-1"
      description = "any-any"
      cidr_blocks = "0.0.0.0/0"
    }
  ]
}

