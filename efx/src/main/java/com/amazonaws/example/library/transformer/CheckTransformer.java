// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0


package com.amazonaws.example.library.transformer;

import com.ecwid.consul.v1.query.model.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckTransformer implements ObjectTransformer<List<Check>, List<com.ecwid.consul.v1.health.model.Check>> {
    @Override
    public List<com.ecwid.consul.v1.health.model.Check> transform(List<Check> fromObject) {
        if (fromObject == null) {
            return Collections.emptyList();
        }

        List<com.ecwid.consul.v1.health.model.Check> resultList = new ArrayList<>();

        for (com.ecwid.consul.v1.query.model.Check queryCheck : fromObject) {
            com.ecwid.consul.v1.health.model.Check healthCheck = new com.ecwid.consul.v1.health.model.Check();
            healthCheck.setNode(queryCheck.getNode());
            healthCheck.setCheckId(queryCheck.getCheckId());
            healthCheck.setName(queryCheck.getName());
            healthCheck.setStatus(com.ecwid.consul.v1.health.model.Check.CheckStatus.valueOf(queryCheck.getStatus().name()));
            healthCheck.setNode(queryCheck.getNotes());
            healthCheck.setOutput(queryCheck.getOutput());
            healthCheck.setServiceId(queryCheck.getServiceId());
            healthCheck.setServiceName(queryCheck.getServiceName());
            healthCheck.setServiceTags(queryCheck.getServiceTags());
            healthCheck.setCreateIndex(queryCheck.getCreateIndex());
            healthCheck.setModifyIndex(queryCheck.getModifyIndex());

            resultList.add(healthCheck);
        }

        return resultList;
    }


}
