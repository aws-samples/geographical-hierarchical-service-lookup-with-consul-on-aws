// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "instance_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

# NOTE: In the policy below the resource is specified as star as per AWS documentation, since these types of actions are not resources based.
# https://docs.aws.amazon.com/service-authorization/latest/reference/list_amazonec2.html#amazonec2-actions-as-permissions

data "aws_iam_policy_document" "auto_discover_cluster" {
  statement {
    effect = "Allow"

    actions = [
      "ec2:DescribeInstances",
      "ec2:DescribeTags",
      "autoscaling:Describe*",
    ]

    resources = ["*"]
  }
}

data "aws_iam_policy" "ssm" {
  name = "AmazonSSMManagedInstanceCore"
}

resource "aws_iam_policy" "secretsmanager_policy" {
  name        = "consul-asg-secretsmanager-policy"
  description = "Policy to retrieve secrets manager secrets string."

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Resource": ["${var.mgmt_secret_arn}","${var.agent_secret_arn}","${var.license_secret_arn}"],
      "Action": ["secretsmanager:GetResourcePolicy","secretsmanager:GetSecretValue","secretsmanager:DescribeSecret","secretsmanager:ListSecretVersionIds","secretsmanager:ListSecrets"],        
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_iam_instance_profile" "instance_profile" {
  name_prefix = "${var.deployment_id}-dc1"
  path        = "/"
  role        = element(concat(aws_iam_role.instance_role.*.name, [""]), 0)

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_iam_role" "instance_role" {
  name_prefix        = "${var.deployment_id}-dc1"
  assume_role_policy = data.aws_iam_policy_document.instance_role.json

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_iam_role_policy" "auto_discover_cluster" {
  name   = "auto-discover-cluster"
  role   = aws_iam_role.instance_role.id
  policy = data.aws_iam_policy_document.auto_discover_cluster.json
}

resource "aws_iam_role_policy_attachment" "ssm-cluster" {
  role       = aws_iam_role.instance_role.name
  policy_arn = data.aws_iam_policy.ssm.arn
}

resource "aws_iam_role_policy_attachment" "secretsmanager" {
  role       = aws_iam_role.instance_role.name
  policy_arn = aws_iam_policy.secretsmanager_policy.arn
}