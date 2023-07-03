// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0


package com.amazonaws.example.library.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Read Metadata from environment variables prefixes with <b>service.metadata</b>
 */
public class EnvironmentConsulMetadataProvider implements ConsulMetaDataProvider {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConsulMetadataProvider.class);
    private static final String METADATA_KEY = "service.metadata";

    Map<String, String> metadata = new HashMap<>();

    public EnvironmentConsulMetadataProvider() {
        logger.debug("Loading Metadata from Environment");

        Map<String, String> env = System.getenv();
        env.forEach((k, v) -> {
            if (k.startsWith(METADATA_KEY)) {
                logger.debug("Updating metadata with {}", k);
                metadata.put(sanitizeKey(k), v);
            }
        });
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    private String sanitizeKey(String key) {
        return key.substring(METADATA_KEY.length() + 1);
    }
}
