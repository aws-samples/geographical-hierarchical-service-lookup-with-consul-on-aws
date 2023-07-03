// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

variable "deployment_role" {
  type = string
}

variable "target_account" {
  type = string
}

variable "target_region" {
  type = string
}
 
variable "deployment_name" {
  description = "Deployment name, used to prefix resources"
  type        = string
  default     = ""
}

variable "owner" {
  description = "Resource owner identified using an email address"
  type        = string
  default     = ""
}

variable "ttl" {
  description = "Resource TTL (time-to-live)"
  type        = number
  default     = 48
}

variable "datacenter_config" {
  description = "List of tenant configuration"
  type        = map
  default     = {
    dc1 = {
        vpc_cidr            = "10.100.0.0/16"
        public_subnets      = ["10.100.0.0/24", "10.100.1.0/24", "10.100.2.0/24"]
        private_subnets     = ["10.100.100.0/24", "10.100.101.0/24", "10.100.102.0/24"]
        consul_federation_id = "1"
    }
    dc2 = {
        vpc_cidr            = "10.200.0.0/16"
        public_subnets      = ["10.200.0.0/24", "10.200.1.0/24", "10.200.2.0/24"]
        private_subnets     = ["10.200.100.0/24", "10.200.101.0/24", "10.200.102.0/24"]
        consul_federation_id = "2"
    }
  }  
}

variable "aws_key_pair_key_name" {
  description = "Key pair name"
  type        = string
  default     = ""
}

variable "consul_version" {
  description = "Consul version"
  type        = string
  default     = "1.13.1-ent"
}

variable "consul_ent_license" {
  description = "Consul enterprise license"
  type        = string
  default     = ""
}

variable "consul_serf_lan_port" {
  description = "Consul serf lan port"
  type        = number
  default     = 9301
}

variable "ami_consul_server" {
  description = "AMI of Consul Server Autoscaling Group"
  type        = string
}

variable "ami_consul_client" {
  description = "AMI of Consul clients"
  type        = string
}

variable "license" {
  description = "Consul license content"
  type        = string
}


