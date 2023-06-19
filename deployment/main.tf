// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

locals {
  deployment_id = lower("${var.deployment_name}-${random_string.suffix.result}")
}

resource "random_string" "suffix" {
  length  = 8
  special = false
}


resource "random_id" "consul_gossip_encrypt_key" {
  byte_length = 32
}

resource "random_uuid" "mgmt_token" {
}

resource "random_uuid" "agent_token" {
}

# NOTE: In the policy below the resource is specified as star as per AWS documentation
# https://docs.aws.amazon.com/kms/latest/developerguide/key-policies.html
data "aws_iam_policy_document" "kms_policy" {
# checkov:skip=CKV_AWS_111: "Ensure IAM policies does not allow write access without constraints"
# checkov:skip=CKV_AWS_109: "Ensure IAM policies does not allow permissions management / resource exposure without constraints"
  statement {
    sid       = "1"
    effect    = "Allow"
    actions   = ["kms:*"]
    resources = ["*"]
    principals {
      type        = "AWS"
      identifiers = [
        "arn:aws:iam::${data.aws_caller_identity.current.id}:root",
        "arn:aws:iam::${var.target_account}:role/${var.deployment_role}",
        "arn:aws:iam::${var.target_account}:role/${module.infra-aws.client_iam}",
        "arn:aws:iam::${var.target_account}:role/${module.asg-consul-server.asg_iam}"
      ]
    }
  }
  statement {
    sid    = "2"
    effect = "Allow"
    actions = [
      "kms:Encrypt*",
      "kms:Decrypt*",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:Describe*"
    ]
    resources = ["*"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_kms_key" "secretsmanager_server_kms" {
  description             = "secrets-server-kmskey"
  deletion_window_in_days = 10
  enable_key_rotation     = true
  policy                  = data.aws_iam_policy_document.kms_policy.json
}

resource "aws_kms_key" "secretsmanager_client_kms" {
  description             = "secrets-client-kmskey"
  deletion_window_in_days = 10
  enable_key_rotation     = true
  policy                  = data.aws_iam_policy_document.kms_policy.json
}

resource "aws_secretsmanager_secret" "mgmt_secret" {
  name        = "consul-mgmt-token"
  kms_key_id  = aws_kms_key.secretsmanager_server_kms.arn
}

resource "aws_secretsmanager_secret_version" "mgmt_value" {
  secret_id     = aws_secretsmanager_secret.mgmt_secret.id
  secret_string = random_uuid.mgmt_token.id
}

resource "aws_secretsmanager_secret" "license_secret" {
  name        = "consul-license"
  kms_key_id  = aws_kms_key.secretsmanager_client_kms.arn
}

resource "aws_secretsmanager_secret_version" "license_value" {
  secret_id     = aws_secretsmanager_secret.license_secret.id
  secret_string = "${var.license}"
}

module "infra-aws" {
  source  = "./modules/infra"

  region                 = var.target_region
  deployment_id          = local.deployment_id
  owner                  = var.owner
  ttl                    = var.ttl
  key_pair_key_name      = var.aws_key_pair_key_name
  datacenter_config      = var.datacenter_config
  consul_serf_lan_port   = var.consul_serf_lan_port
  ami_fake_service       = var.ami_consul_client
  gossip_encrypt_key     = random_id.consul_gossip_encrypt_key.b64_std
  license                = var.license 
  license_secret_arn     = aws_secretsmanager_secret.license_secret.arn
  agent_secret_arn       = aws_secretsmanager_secret.mgmt_secret.arn
  kms_policy             = data.aws_iam_policy_document.kms_policy.json
}


module "asg-consul-server" {
  source = "./modules/consul-servers"

  region                       = var.target_region
  deployment_id                = local.deployment_id
  owner                        = var.owner
  ttl                          = var.ttl
  key_pair_key_name            = var.aws_key_pair_key_name
  datacenter_config            = var.datacenter_config
  private_subnet_ids           = module.infra-aws.vpc_private_subnet_ids
  security_group_ssh_id        = module.infra-aws.sg_ssh_ids
  security_group_consul_id     = module.infra-aws.sg_consul_ids
  security_group_consul_wan_id = module.infra-aws.sg_consul_wan_ids
  ami_consul_server_asg        = var.ami_consul_server
  gossip_encrypt_key           = random_id.consul_gossip_encrypt_key.b64_std
  license                      = var.license 
  license_secret_arn           = aws_secretsmanager_secret.license_secret.arn
  agent_secret_arn             = aws_secretsmanager_secret.mgmt_secret.arn
  mgmt_secret_arn              = aws_secretsmanager_secret.mgmt_secret.arn
}