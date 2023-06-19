// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0


package com.amazonaws.example.library.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;
import software.amazon.awssdk.regions.internal.util.Ec2MetadataConfigProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Read Metadata from EC2 Metadata
 */
public class EC2ConsulMetadataProvider implements ConsulMetaDataProvider {
    private static final Logger logger = LoggerFactory.getLogger(EC2ConsulMetadataProvider.class);

    private static final String METADATA_PATH = "/latest/meta-data/tags/instance";

    Map<String, String> metadata;

    public EC2ConsulMetadataProvider() {
        this.metadata = loadMetadata();
    }

    private Map<String, String> loadMetadata() {
        String metadataString;
        try {
            metadataString = EC2MetadataUtils.getData(METADATA_PATH);

            if (StringUtils.hasText(metadataString)) {
                // The Metadata is returned as plain text, need to split based on the new line
                String[] metadataArray = metadataString.split("[\\r\\n]+");
                logger.debug("Found {} metadata", metadataArray.length);

                Map<String, String> resultMap = new HashMap<>();
                for (String meta : metadataArray) {
                    String value = EC2MetadataUtils.getData(METADATA_PATH + "/" + meta);
                    if (StringUtils.hasText(value)) {
                        resultMap.put(meta, value);
                    } else {
                        // Somehow we didn't have value for the Metadata
                        logger.error("metadata {} has no value", meta);
                    }
                }

                // We are done here
                return resultMap;

            } else {
                // If we can't find the Metadata, possibly the EC2 didn't enable the Metadata URL
                // See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Using_Tags.html#work-with-tags-in-IMDS
                logger.warn("Unable to retrieve Metadata from EC2 Instance. Please ensure \"Allow tags in instance metadata\" is enabled");
            }
        } catch (SdkClientException e) {
            logger.warn("Metadata Endpoint: {}, not running in EC2 Instance", Ec2MetadataConfigProvider.builder().build().getEndpoint());

        }

        // Shouldn't reach here
        logger.warn("EC2 Metadata Provider has no result");
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.metadata;
    }
}
