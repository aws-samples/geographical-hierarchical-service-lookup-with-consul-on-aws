// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0


package com.anybank.efx.controller;

import com.amazonaws.example.library.ConsulDiscoveryLibrary;
import com.amazonaws.example.library.metadata.ConsulMetaDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SampleController {
    private static final Logger logger = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ConsulDiscoveryLibrary consulDiscoveryLibrary;

    @Autowired
    private ConsulMetaDataProvider consulMetaDataProvider;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/v1/discover/service/{serviceName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ServiceInstance> getServiceInstances(@PathVariable String serviceName) {
        return consulDiscoveryLibrary.getServices(serviceName);
    }

    @GetMapping(value = "/v1/query/service/{serviceName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ServiceInstance> executeQuery(@PathVariable String serviceName) {
        return consulDiscoveryLibrary.executeQuery(serviceName);
    }

    @GetMapping(value = "/v1/hello", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> sayHello(ServletRequest servletRequest) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            resultMap.put("Server Address", InetAddress.getLocalHost().getHostAddress() + ":" + servletRequest.getLocalPort());

        } catch (UnknownHostException e) {
            logger.error("Unknown Host Exception", e);
            resultMap.put("Server Address", e.getMessage());
        }

        resultMap.put("Server Metadata", consulMetaDataProvider.getMetadata());

        return ResponseEntity.ok(resultMap);
    }

    @GetMapping(value = "/v1/service/{serviceName}/hello", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> sayHelloTo(ServletRequest servletRequest, @PathVariable String serviceName) {
        List<ServiceInstance> serviceInstanceList = getServiceInstances(serviceName);

        logger.debug("Found {} instance available", serviceInstanceList.size());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.putAll(sayHello(servletRequest).getBody());
        resultMap.put("Matched Count", serviceInstanceList.size());

        for (ServiceInstance serviceInstance : serviceInstanceList) {
            // Saying hi to every one of them
            logger.debug("Saying Hello to {}", serviceInstance.getServiceId());

            String urlString = serviceInstance.getUri().resolve("/v1/hello").toString();

            logger.error("invoking URL: {}", urlString);

            ResponseEntity<Map<String, Object>> restResult = restTemplate.exchange(urlString,
                    HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {
            });

            if (restResult.getStatusCode() == HttpStatus.OK)
                resultMap.put(serviceInstance.getInstanceId(), restResult.getBody());
            else {
                resultMap.put(serviceInstance.getInstanceId(), restResult.getStatusCode());
            }
        }

        return ResponseEntity.ok(resultMap);
    }

}
