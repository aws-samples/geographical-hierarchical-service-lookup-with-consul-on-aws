// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

output "vpc_ids" {
  value = {for k, v in module.vpc : k => v.vpc_id}
}

output "vpc_private_subnet_ids" {
  value = {for k, v in module.vpc : k => v.private_subnets}
}

output "sg_ssh_ids" {
  value = {for k, v in module.sg-ssh : k => v.security_group_id}
}

output "sg_consul_ids" {
  value = {for k, v in module.sg-consul : k => v.security_group_id}
}

output "sg_consul_wan_ids" {
  value = {for k, v in module.sg-consul-wan : k => v.security_group_id}
}

output "sg_fake_service_ids" {
  value = {for k, v in module.sg-fake-service : k => v.security_group_id}
}

output "client_iam" {
  value = aws_iam_role.client_role.name
}
