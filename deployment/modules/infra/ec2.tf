// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

data "aws_caller_identity" "current" {}

locals {
  instances = [
    {
      datacenter = "dc1"
      tags = {
        Name = "trading-acme-alpha-dev-dc1"
        customer = "acme"
        cluster = "alpha"
        location = "dc1"
        environment = "dev"
        service = "trading"
      }
    },
    {
      datacenter = "dc1"
      tags = {
        Name = "pricer-acme-alpha-dev-dc1"
        customer = "acme"
        cluster = "alpha"
        location = "dc1"
        environment = "dev"
        service = "pricer"
      }
    },
    {
      datacenter = "dc2"
      tags = {
        Name = "pricer-acme-alpha-dev-dc2"
        customer = "acme"
        cluster = "alpha"
        location = "dc2"
        environment = "dev"
        service = "pricer"
      }
    },
    {
      datacenter = "dc1"
      tags = {
        Name = "pricer-acme-beta-dev-dc1"
        customer = "acme"
        cluster = "beta"
        location = "dc1"
        environment = "dev"
        service = "pricer"
      }
    },
    {
      datacenter = "dc2"
      tags = {
        Name = "pricer-acme-beta-dev-dc2"
        customer = "acme"
        cluster = "beta"
        location = "dc2"
        environment = "dev"
        service = "pricer"
      }
    },
    {
      datacenter = "dc1"
      tags = {
        Name = "pricer-shared-alpha-dev-dc1"
        customer = "shared"
        cluster = "alpha"
        location = "dc1"
        environment = "dev"
        service = "pricer"
      }
    },
    {
      datacenter = "dc2"
      tags = {
        Name = "pricer-shared-alpha-dev-dc2"
        customer = "shared"
        cluster = "alpha"
        location = "dc2"
        environment = "dev"
        service = "pricer"
      }
    },
    {
      datacenter = "dc1"
      tags = {
        Name = "staticdata-shared-shared-dev-dc1"
        customer = "shared"
        cluster = "shared"
        location = "dc1"
        environment = "dev"
        service = "static-data"
      }
    },
    {
      datacenter = "dc2"
      tags = {
        Name = "staticdata-shared-shared-dev-dc2"
        customer = "shared"
        cluster = "shared"
        location = "dc2"
        environment = "dev"
        service = "static-data"
      }
    },
    {
      datacenter = "dc2"
      tags = {
        Name = "staticdata-shared-beta-dev-dc2"
        customer = "shared"
        cluster = "beta"
        location = "dc2"
        environment = "dev"
        service = "static-data"
      }
    },
    {
      datacenter = "dc2"
      tags = {
        Name = "staticdata-shared-gamma-dev-dc2"
        customer = "shared"
        cluster = "gamma"
        location = "dc2"
        environment = "dev"
        service = "static-data"
      }
    },
    {
      datacenter = "dc1"
      tags = {
        Name = "staticdata-stark-alpha-dev-dc1"
        customer = "stark"
        cluster = "alpha"
        location = "dc1"
        environment = "dev"
        service = "static-data"
      }
    },
    {
      datacenter = "dc2"
      tags = {
        Name = "staticdata-acme-beta-prod-dc2"
        customer = "acme"
        cluster = "beta"
        location = "dc2"
        environment = "prod"
        service = "static-data"
      }
    }
  ]
}

resource "aws_kms_key" "s3_kms" {
  description             = "s3-${var.deployment_id}-kmskey"
  deletion_window_in_days = 10
  enable_key_rotation     = true
  policy                  = var.kms_policy
}


resource "aws_s3_bucket" "files_bucket" {
  # checkov:skip=CKV_AWS_144: "Ensure that S3 bucket has cross-region replication enabled"
  acl    = "private"
  versioning {
    enabled    = true
  }
  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm = "aws:kms"
      }
    }
  }
}

resource "aws_s3_bucket_logging" "logging" {
  bucket        = aws_s3_bucket.files_bucket.id
  target_bucket = var.log_bucket != null ? var.log_bucket : aws_s3_bucket.files_bucket.id
  target_prefix = "log/"
}

resource "aws_s3_bucket_public_access_block" "files_public_access" {
  bucket = aws_s3_bucket.files_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_object" "service" {
  key                    = "application.jar"
  bucket                 = aws_s3_bucket.files_bucket.id
  source                 = "application.jar"
  kms_key_id             = aws_kms_key.s3_kms.arn
}

resource "aws_s3_bucket_object" "consul-config" {
  key                    = "consul-client-config.json"
  bucket                 = aws_s3_bucket.files_bucket.id
  source                 = "${path.root}/scripts/templates/consul-client-config.json"
  kms_key_id             = aws_kms_key.s3_kms.arn
}

resource "aws_iam_role" "client_role" {
  name = "client-iam-role"

  assume_role_policy = jsonencode({
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
    Version = "2012-10-17"
  })
}

data "aws_iam_policy" "ssm" {
  name = "AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ssm-ec2" {
  role       = aws_iam_role.client_role.name
  policy_arn = data.aws_iam_policy.ssm.arn
}

# NOTE: In the EC2 policy below the resource is specified as star as per AWS documentation, since these types of actions are not resources based.
# https://docs.aws.amazon.com/service-authorization/latest/reference/list_amazonec2.html#amazonec2-actions-as-permissions

resource "aws_iam_policy" "client_role_policy" {
  name        = "client-role-policy"
  description = "Policy to read and write from the files bucket and describe ec2 instances"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Resource": ["${aws_s3_bucket.files_bucket.arn}", "${aws_s3_bucket.files_bucket.arn}/*"],
      "Action": ["s3:Get*", "s3:List*", "s3:Put*"],
      "Effect": "Allow"
    },
    {
      "Resource": "*",
      "Action": ["ec2:DescribeTags","ec2:DescribeInstances"],
      "Effect": "Allow"
    },
    {
      "Resource": ["${var.agent_secret_arn}","${var.license_secret_arn}"],
      "Action": ["secretsmanager:GetResourcePolicy","secretsmanager:GetSecretValue","secretsmanager:DescribeSecret","secretsmanager:ListSecretVersionIds","secretsmanager:ListSecrets"],          
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_iam_policy_attachment" "s3-policy-attach" {
  name       = "s3-policy-attachment"
  roles      = [aws_iam_role.client_role.name]
  policy_arn = aws_iam_policy.client_role_policy.arn
}

resource "aws_iam_instance_profile" "client_profile" {
  name = "client_profile"
  role = aws_iam_role.client_role.name
}

resource "aws_instance" "client" {
  for_each = {for instance in local.instances : instance.tags["Name"] => instance}

  ami             = var.ami_fake_service
  instance_type   = "t3.micro"
  iam_instance_profile = aws_iam_instance_profile.client_profile.name
  key_name        = var.key_pair_key_name
  subnet_id = module.vpc["${each.value.datacenter}"].private_subnets[0]
  vpc_security_group_ids = [module.sg-ssh["${each.value.datacenter}"].security_group_id, module.sg-consul["${each.value.datacenter}"].security_group_id, module.sg-fake-service["${each.value.datacenter}"].security_group_id, module.sg-consul-wan["${each.value.datacenter}"].security_group_id]
  root_block_device {
    encrypted = true
  }
  tags = each.value.tags
  ebs_optimized = true
  monitoring = true 
  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
    instance_metadata_tags = "enabled"
  }
  user_data_replace_on_change = true
  user_data = <<-EOF
              Content-Type: multipart/mixed; boundary="//"
              MIME-Version: 1.0

              --//
              Content-Type: text/cloud-config; charset="us-ascii"
              MIME-Version: 1.0
              Content-Transfer-Encoding: 7bit
              Content-Disposition: attachment; filename="cloud-config.txt"

              #cloud-config
              cloud_final_modules:
              - [scripts-user, always]

              --//
              Content-Type: text/x-shellscript; charset="us-ascii"
              MIME-Version: 1.0
              Content-Transfer-Encoding: 7bit
              Content-Disposition: attachment; filename="userdata.txt"
              #!/bin/bash
              sudo aws s3 cp s3://${aws_s3_bucket.files_bucket.id}/${aws_s3_bucket_object.service.id} opt/${aws_s3_bucket_object.service.id} --region ${var.region}
              sudo aws s3 cp s3://${aws_s3_bucket.files_bucket.id}/${aws_s3_bucket_object.consul-config.id} opt/consul/config/default-empty.json --region ${var.region}
              sudo cp /var/tmp/consul-ent-license.hclic /opt/consul/config/consul-ent-license-empty.hclic
              sudo cp /var/tmp/consul-config.json /opt/consul/config/consul-config-empty.json
              cd /opt/consul/config/
              sudo touch .env
              sudo chmod 744 .env
              sudo echo -e 'export datacenter=${each.value.datacenter} \nexport region=${var.region} \nexport deployment_id=${var.deployment_id} \nexport serf_lan_port=${var.consul_serf_lan_port} \nexport gossip_encrypt_key=${var.gossip_encrypt_key}
              \nexport node_name=$(aws ec2 describe-instances --region ${var.region} --filters Name=tag:Name,Values=${each.value.tags["Name"]}  --query Reservations[*].Instances[*].NetworkInterfaces[0].PrivateIpAddresses[*].PrivateDnsName --output text)
              \nexport privateIP=$(aws ec2 describe-instances --region ${var.region} --filters Name=tag:Name,Values=${each.value.tags["Name"]}  --query Reservations[*].Instances[*].NetworkInterfaces[0].PrivateIpAddress --output text)
              \nexport license=$(aws --output text --region ${var.region} secretsmanager get-secret-value --secret-id ${var.license_secret_arn} --query SecretString)
              \nexport agent_token=$(aws --output text --region ${var.region} secretsmanager get-secret-value --secret-id ${var.agent_secret_arn} --query SecretString)' > .env 
              sudo chmod 766 consul-ent-license-empty.hclic
              sudo echo '$license' > consul-ent-license-empty.hclic
              sudo touch default.json
              sudo chmod 766 default.json
              sudo touch consul-config.json
              sudo chmod 766 consul-config.json
              sudo touch consul-ent-license.hclic
              sudo chmod 766 consul-ent-license.hclic
              source .env
              envsubst < default-empty.json > default.json
              envsubst < consul-config-empty.json > consul-config.json
              envsubst < consul-ent-license-empty.hclic > consul-ent-license.hclic
              sudo rm default-empty.json
              sudo rm consul-config-empty.json
              sudo rm consul-ent-license-empty.hclic
              sudo /opt/consul/bin/run-consul --client --skip-consul-config
              sudo nohup java -jar -Dspring.profiles.active=${each.value.tags["service"]} /opt/efx-0.0.1-SNAPSHOT.jar &
              --//
              EOF
}
