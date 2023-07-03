// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0


package com.amazonaws.example.library.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consul Metadata Provider chain that looks for metadata in this order:
 *
 * <ol>
 *     <li>Environment Variables</li>
 *     <li>EC2 Metadata</li>
 * </ol>
 */
public class DefaultConsulMetadataProvider implements ConsulMetaDataProvider {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConsulMetadataProvider.class);

    private final Map<String, String> metadata;

    public DefaultConsulMetadataProvider() {
        //Create the Provider List. Can be refactored to look from Configuration or Properties file in the future
        List<ConsulMetaDataProvider> providerList = new ArrayList<>();
        providerList.add(new EnvironmentConsulMetadataProvider());
        providerList.add(new EC2ConsulMetadataProvider());

        logger.debug("Consolidating Metadata...");

        metadata = new HashMap<>();
        for (ConsulMetaDataProvider provider : providerList) {
            metadata.putAll(provider.getMetadata());
        }
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.metadata;
    }
}
