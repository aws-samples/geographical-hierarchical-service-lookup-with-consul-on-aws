// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amazonaws.example.library.metadata;

import java.util.Map;

/**
 * Generic interface to fetch Metadata from Metadata Source
 */
public interface ConsulMetaDataProvider {
    Map<String, String> getMetadata();
}
