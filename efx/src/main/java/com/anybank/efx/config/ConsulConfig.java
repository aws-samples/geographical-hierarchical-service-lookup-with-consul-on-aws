// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.anybank.efx.config;

import com.amazonaws.example.library.ConsulDiscoveryLibrary;
import com.amazonaws.example.library.metadata.ConsulMetaDataProvider;
import com.amazonaws.example.library.metadata.ConsulMetadataRegistrationCustomizer;
import com.amazonaws.example.library.metadata.DefaultConsulMetadataProvider;
import com.amazonaws.example.library.transformer.CheckTransformer;
import com.amazonaws.example.library.transformer.HealthServiceTransformer;
import com.amazonaws.example.library.transformer.ObjectTransformer;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.query.model.Check;
import com.ecwid.consul.v1.query.model.QueryNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ConsulConfig {
    /**
     * Beans that responsible for gathering Service Metadata from EC2 Instance
     */
    @Bean
    public ConsulMetaDataProvider consulMetaDataProvider() {
        return new DefaultConsulMetadataProvider();
    }

    /**
     * Hook into SpringCloudConsul Library Service Registration, to customize service metadata
     */
    @Bean
    public ConsulMetadataRegistrationCustomizer consulMetadataRegistrationCustomizer(ConsulMetaDataProvider consulMetaDataProvider) {
        return new ConsulMetadataRegistrationCustomizer(consulMetaDataProvider);
    }

    @Bean
    public ConsulDiscoveryLibrary consulDiscoveryLibrary(ConsulDiscoveryClient consulDiscoveryClient,
                                                         ConsulDiscoveryProperties properties,
                                                         ConsulClient consulClient,
                                                         @Qualifier("HealthServiceTransformer") ObjectTransformer<QueryNode, HealthService> healthServiceTransformer,
                                                         ConsulMetaDataProvider consulMetaDataProvider) {
        return new ConsulDiscoveryLibrary(consulDiscoveryClient, properties, consulClient, healthServiceTransformer, consulMetaDataProvider);
    }

    @Bean(name = "HealthServiceTransformer")
    public ObjectTransformer<QueryNode, HealthService> healthServiceTransformer(@Qualifier("CheckTransformer") ObjectTransformer<List<Check>, List<com.ecwid.consul.v1.health.model.Check>> checkTransformer) {
        return new HealthServiceTransformer(checkTransformer);
    }

    @Bean(name = "CheckTransformer")
    public ObjectTransformer<List<Check>, List<com.ecwid.consul.v1.health.model.Check>> checkTransformer() {
        return new CheckTransformer();
    }
}
