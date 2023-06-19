// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

terraform {
  required_version = "~> 1.0"

  required_providers {
    local = {
      source = "hashicorp/local"
    }
    null = {
      source = "hashicorp/null"
      version = "3.1.1"
    }
    consul = {
      source = "hashicorp/consul"
      version = "2.16.2"
    }
  }
}

