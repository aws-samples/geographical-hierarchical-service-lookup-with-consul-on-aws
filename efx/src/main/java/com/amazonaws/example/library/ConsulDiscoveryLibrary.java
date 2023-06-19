// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0


package com.amazonaws.example.library;

import com.amazonaws.example.library.metadata.ConsulMetaDataProvider;
import com.amazonaws.example.library.transformer.ObjectTransformer;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.coordinate.model.Datacenter;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.query.model.QueryExecution;
import com.ecwid.consul.v1.query.model.QueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sample Consul Discovery Library
 */
public class ConsulDiscoveryLibrary {
    private static final Logger logger = LoggerFactory.getLogger(ConsulDiscoveryLibrary.class);

    private static final String CUSTOMER_KEY = "customer";
    private static final String CLUSTER_KEY = "cluster";
    private static final String ENV_KEY = "environment";
    private static final String SHARED_CUSTOMER = "shared";


    private final Map<String, String> serviceMetadata;

    private final ConsulDiscoveryClient consulDiscoveryClient;

    private final ConsulClient consulClient;
    private final ConsulDiscoveryProperties consulDiscoveryProperties;
    private final ObjectTransformer<QueryNode, HealthService> healthServiceTransformer;

    public ConsulDiscoveryLibrary(ConsulDiscoveryClient consulDiscoveryClient,
                                  ConsulDiscoveryProperties consulDiscoveryProperties,
                                  ConsulClient consulClient,
                                  ObjectTransformer<QueryNode, HealthService> healthServiceTransformer,
                                  ConsulMetaDataProvider consulMetaDataProvider) {
        this.consulDiscoveryClient = consulDiscoveryClient;
        this.consulDiscoveryProperties = consulDiscoveryProperties;
        this.consulClient = consulClient;
        this.healthServiceTransformer = healthServiceTransformer;

        this.serviceMetadata = consulMetaDataProvider.getMetadata();
        logger.debug("ConsulDiscoveryLibrary Metadata: {}", serviceMetadata);
    }


    public List<ServiceInstance> getServices(String serviceId) {
        // First find out how many DCs are available
        Response<List<Datacenter>> dataCenterResponse = consulClient.getDatacenters();
        if (dataCenterResponse == null || dataCenterResponse.getValue() == null) {
            logger.info("We have no datacenters");
            return Collections.emptyList();
        }
        List<Datacenter> datacenterList = dataCenterResponse.getValue();
        logger.debug("We have {} datacenters", datacenterList);

        // service matches all predicates in any DC
        List<ServiceInstance> fullMatch = new ArrayList<>();
        // service matches customer predicate in any DC
        List<ServiceInstance> customerMatch = new ArrayList<>();
        // service matches customer predicate on shared cluster
        List<ServiceInstance> sharedCudtomerMatch = new ArrayList<>();

        boolean customerFound = false;
        boolean sharedCustomerFound = false;
        // Look for the service within each Datacenter, in order of increasing TTL
        for (Datacenter datacenter : datacenterList) {
            logger.debug("Inspecting datacenter {} for service {}", datacenter.getDatacenter(), serviceId);

            QueryParams queryParam = new QueryParams(datacenter.getDatacenter());
            List<ServiceInstance> serviceInstanceList = consulDiscoveryClient.getInstances(serviceId, queryParam);

            for (ServiceInstance si : serviceInstanceList) {
                // Look for a full match
                if (isFullMatch(si)) {
                    fullMatch.add(si);
                }
                // Look for a customer match
                if (isCustomerMatch(customerFound, si)) {
                    logger.debug("{} matched shared in dc {}", serviceInstanceList.size(), datacenter.getDatacenter());
                    customerMatch.add(si);
                }
                // Look for a shared customer match
                if (isSharedCustomer(sharedCustomerFound, si)) {
                    logger.debug("{} matched shared in dc {}", serviceInstanceList.size(), datacenter.getDatacenter());
                    sharedCudtomerMatch.add(si);
                }

            }

            if (!fullMatch.isEmpty()) {
                logger.debug("{}/{} matched in dc {}", fullMatch.size(), serviceInstanceList.size(), datacenter.getDatacenter());
                return fullMatch;
            }
            if (!customerMatch.isEmpty()) {
                customerFound = true;
            }
            if (!sharedCudtomerMatch.isEmpty()) {
                sharedCustomerFound = true;
            }
        }
        // We are unable to find any match
        return !customerMatch.isEmpty() ? customerMatch : sharedCudtomerMatch;
    }

    /**
     * Checks if ServiceInstance is matching shared customer predicate, we also ensure that we match the environment
     * predicate
     * @param sharedCustomerFound if a shared customer ServiceInstance has been found in a previous DC
     * @param si the ServiceInstance
     * @return true if it's a shared customer match, else return false
     */
    private boolean isSharedCustomer(boolean sharedCustomerFound, ServiceInstance si) {
        return si.getMetadata().containsKey(ENV_KEY) && si.getMetadata().get(ENV_KEY).equalsIgnoreCase(serviceMetadata.get(ENV_KEY))
                && si.getMetadata().containsKey(CUSTOMER_KEY)
                && si.getMetadata().get(CUSTOMER_KEY).equalsIgnoreCase(SHARED_CUSTOMER)
                && !sharedCustomerFound;
    }

    /**
     * Checks if ServiceInstance is matching customer predicate, we also ensure that we match the environment
     * predicate
     * @param customerFound if a customer ServiceInstance has been found in a previous DC
     * @param si the ServiceInstance
     * @return true if it's a customer match, else return false
     */
    private boolean isCustomerMatch(boolean customerFound, ServiceInstance si) {
        return si.getMetadata().containsKey(ENV_KEY) && si.getMetadata().get(ENV_KEY).equalsIgnoreCase(serviceMetadata.get(ENV_KEY))
                && si.getMetadata().containsKey(CUSTOMER_KEY)
                && si.getMetadata().get(CUSTOMER_KEY).equalsIgnoreCase(serviceMetadata.get(CUSTOMER_KEY))
                && !customerFound;
    }

    /**
     * Checks if a ServiceInstance is matching all predicates, these are environment, customer and cluster
     * @param si the ServiceInstance
     * @return true if it's a full match, else return false
     */
    private boolean isFullMatch(ServiceInstance si) {
        return si.getMetadata().containsKey(ENV_KEY) && si.getMetadata().get(ENV_KEY).equalsIgnoreCase(serviceMetadata.get(ENV_KEY))
                && si.getMetadata().containsKey(CUSTOMER_KEY) && si.getMetadata().get(CUSTOMER_KEY).equalsIgnoreCase(serviceMetadata.get(CUSTOMER_KEY))
                && si.getMetadata().containsKey(CLUSTER_KEY) && si.getMetadata().get(CLUSTER_KEY).equalsIgnoreCase(serviceMetadata.get(CLUSTER_KEY));
    }

    /**
     * Demonstrate how to invoke Prepared Query using ConsulClient API
     */
    public List<ServiceInstance> executeQuery(String serviceName) {
        List<ServiceInstance> instances = new ArrayList<>();
        Response<QueryExecution> queryExecutionResponse = consulClient.executePreparedQuery(serviceName, new QueryParams(this.consulDiscoveryProperties.getConsistencyMode()));
        QueryExecution queryExecution = queryExecutionResponse.getValue();

        for (QueryNode node : queryExecution.getNodes()) {
            instances.add(new ConsulServiceInstance(healthServiceTransformer.transform(node), serviceName));
        }

        return instances;
    }

}
