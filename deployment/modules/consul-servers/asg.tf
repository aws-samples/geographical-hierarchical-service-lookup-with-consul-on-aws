// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

resource "aws_launch_configuration" "launch_configuration" {
  # checkov:skip=CKV_AWS_79: "Ensure Instance Metadata Service Version 1 is not enabled"
  for_each = var.datacenter_config

  name_prefix   = "${var.deployment_id}-${each.key}"
  image_id      = var.ami_consul_server_asg
  instance_type = var.instance_type
  iam_instance_profile = aws_iam_instance_profile.instance_profile.name
  key_name      = var.key_pair_key_name
  security_groups = [var.security_group_ssh_id["${each.key}"], var.security_group_consul_id["${each.key}"], var.security_group_consul_wan_id["${each.key}"]]
  user_data     = <<-EOF
                  #!/bin/bash
                  sudo cp /var/tmp/consul-ent-license.hclic /opt/consul/config/consul-ent-license-empty.hclic
                  sudo cp /var/tmp/consul-config.json /opt/consul/config/consul-config-empty.json
                  cd /opt/consul/config/
                  sudo touch .env
                  sudo chmod 744 .env
                  sudo echo -e 'export mgmt_token=$(aws --output text --region ${var.region} secretsmanager get-secret-value --secret-id ${var.mgmt_secret_arn} --query SecretString)
                  \nexport license=$(aws --output text --region ${var.region} secretsmanager get-secret-value --secret-id ${var.license_secret_arn} --query SecretString)
                  \nexport agent_token=$(aws --output text --region ${var.region} secretsmanager get-secret-value --secret-id ${var.agent_secret_arn} --query SecretString)' > .env 
                  sudo chmod 766 consul-ent-license-empty.hclic
                  sudo echo '$license' > consul-ent-license-empty.hclic
                  sudo touch consul-config.json
                  sudo chmod 766 consul-config.json
                  sudo touch consul-ent-license.hclic
                  sudo chmod 766 consul-ent-license.hclic
                  source .env
                  envsubst < consul-config-empty.json > consul-config.json
                  envsubst < consul-ent-license-empty.hclic > consul-ent-license.hclic
                  sudo rm consul-ent-license-empty.hclic
                  sudo rm consul-config-empty.json
                  sudo /opt/consul/bin/run-consul --server --datacenter ${each.key} --cluster-tag-key consul-cluster --cluster-tag-value ${var.deployment_id}-${each.key} --enable-gossip-encryption  --gossip-encryption-key ${var.gossip_encrypt_key}
                  EOF

  lifecycle {
    create_before_destroy = true
  }

  root_block_device {
    encrypted             = true
  }
}

resource "aws_autoscaling_group" "autoscaling_group" {
  for_each = aws_launch_configuration.launch_configuration

  name_prefix               = "cluster${var.deployment_id}-${each.key}"
  min_size                  = var.asg_min_size
  max_size                  = var.asg_max_size
  launch_configuration      = each.value.name
  vpc_zone_identifier       = var.private_subnet_ids["${each.key}"]

  tag {
      key                 = "consul-cluster"
      value               = "${var.deployment_id}-${each.key}"
      propagate_at_launch = true
    }
  tag {
      key                 = "consul-datacenter"
      value               = "${each.key}"
      propagate_at_launch = true
    }
  tag {
      key                 = "consul-federation-id"
      value               = var.datacenter_config[each.key].consul_federation_id
      propagate_at_launch = true
    }
  tag {
      key                 = "Name"
      value               = "consul-server-${each.key}"
      propagate_at_launch = true
    }

  lifecycle {
    ignore_changes = [load_balancers, target_group_arns]
  }
}