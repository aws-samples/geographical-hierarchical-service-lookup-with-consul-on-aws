// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0


package com.amazonaws.example.library.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;

import java.util.Map;

public class ConsulMetadataRegistrationCustomizer implements ConsulRegistrationCustomizer {
    private static final Logger logger = LoggerFactory.getLogger(ConsulMetadataRegistrationCustomizer.class);

    private final ConsulMetaDataProvider consulMetadataProvider;

    public ConsulMetadataRegistrationCustomizer(ConsulMetaDataProvider consulMetadataProvider) {
        this.consulMetadataProvider = consulMetadataProvider;
    }

    @Override
    public void customize(ConsulRegistration registration) {
        Map<String, String> metadata = registration.getMetadata();

        logger.info("Customizing Consul Registration for Service: {}", registration.getInstanceId());
        if (consulMetadataProvider != null) {
            logger.info("Updating the Metadata");
            metadata.putAll(consulMetadataProvider.getMetadata());
        }
    }
}
