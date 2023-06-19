// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

variable "region" {
  description = "AWS region"
  type        = string
}

variable "deployment_id" {
  description = "Deployment id"
  type        = string
}

variable "owner" {
  description = "Resource owner identified using an email address"
  type        = string
}

variable "ttl" {
  description = "Resource TTL (time-to-live)"
  type        = number
}

variable "key_pair_key_name" {
  description = "Key pair name"
  type        = string
}

variable "datacenter_config" {
  description = "List of VPCs"
  type        = map
}

variable "consul_serf_lan_port" {
  description = "Consul serf lan port"
  type        = number
}

variable "ami_fake_service" {
  description = "AMI of fake-service"
  type        = string
}

variable "gossip_encrypt_key" {
  description = "Consul gossip encryption key"
  type        = string
}

variable "license" {
  description = "Consul license content"
  type        = string
}

variable "agent_secret_arn" {
  description = "Secret ARN of agent token"
  type        = string
}

variable "license_secret_arn" {
  description = "Secret ARN of license"
  type        = string
}

variable "kms_policy" {
  description = "KMS Policy"
  type        = string
}

variable "log_bucket" {
  type        = string
  description = "Target bucket for access logs (optional). If not provided, bucket will store log in itself"
  default     = null
}

