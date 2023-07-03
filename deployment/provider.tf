// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

provider "aws" {
  # Update desired region
  region                  = var.target_region
  # Update account IDs
  allowed_account_ids = ["${var.target_account}"]
}
