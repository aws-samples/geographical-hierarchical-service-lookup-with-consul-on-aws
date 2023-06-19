// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0


package com.amazonaws.example.library.transformer;

import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.query.model.QueryNode;

import java.util.List;

public class HealthServiceTransformer implements ObjectTransformer<QueryNode, HealthService> {

    private final ObjectTransformer<List<com.ecwid.consul.v1.query.model.Check>, List<Check>> checkObjectTransformer;

    public HealthServiceTransformer(ObjectTransformer<List<com.ecwid.consul.v1.query.model.Check>, List<Check>> checkObjectTransformer) {
        this.checkObjectTransformer = checkObjectTransformer;
    }

    @Override
    public HealthService transform(QueryNode fromObject) {
        HealthService healthService = new HealthService();

        HealthService.Node healthServiceNode = new HealthService.Node();
        healthServiceNode.setId(fromObject.getNode().getId());
        healthServiceNode.setNode(fromObject.getNode().getNode());
        healthServiceNode.setAddress(fromObject.getNode().getAddress());
        healthServiceNode.setDatacenter(fromObject.getNode().getDatacenter());
        healthServiceNode.setMeta(fromObject.getNode().getMeta());
        healthServiceNode.setCreateIndex(fromObject.getNode().getCreateIndex());
        healthServiceNode.setModifyIndex(fromObject.getNode().getModifyIndex());
        healthServiceNode.setTaggedAddresses(fromObject.getNode().getTaggedAddresses());

        HealthService.Service healthServiceService = new HealthService.Service();
        healthServiceService.setId(fromObject.getService().getId());
        healthServiceService.setService(fromObject.getService().getService());
        healthServiceService.setTags(fromObject.getService().getTags());
        healthServiceService.setAddress(fromObject.getService().getAddress());
        healthServiceService.setMeta(fromObject.getService().getMeta());
        healthServiceService.setPort(fromObject.getService().getPort());
        healthServiceService.setEnableTagOverride(fromObject.getService().getEnableTagOverride());
        healthServiceService.setCreateIndex(fromObject.getService().getCreateIndex());
        healthServiceService.setModifyIndex(fromObject.getService().getModifyIndex());

        healthService.setNode(healthServiceNode);
        healthService.setService(healthServiceService);
        healthService.setChecks(checkObjectTransformer.transform(fromObject.getChecks()));

        return healthService;
    }
}
