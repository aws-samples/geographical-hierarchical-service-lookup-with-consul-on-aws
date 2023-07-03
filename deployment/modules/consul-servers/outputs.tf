// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

output "asg_iam" {
  value = aws_iam_role.instance_role.name
}