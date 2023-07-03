package com.amazonaws.example.library

import com.amazonaws.example.library.metadata.ConsulMetaDataProvider
import com.amazonaws.example.library.transformer.CheckTransformer
import com.amazonaws.example.library.transformer.HealthServiceTransformer
import com.amazonaws.example.library.transformer.ObjectTransformer
import com.ecwid.consul.transport.HttpResponse
import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.Response
import com.ecwid.consul.v1.coordinate.model.Datacenter
import com.ecwid.consul.v1.health.model.HealthService
import com.ecwid.consul.v1.query.model.QueryExecution
import com.ecwid.consul.v1.query.model.QueryNode
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ConsulDiscoveryLibrarySpec extends Specification {
    @Subject
    ConsulDiscoveryLibrary consulDiscoveryLibrary

    ConsulDiscoveryClient consulDiscoveryClient = Stub()
    ConsulDiscoveryProperties consulDiscoveryProperties = Mock()
    ConsulClient consulClient = Stub()
    ObjectTransformer<QueryNode, HealthService> healthServiceTransformer = new HealthServiceTransformer(new CheckTransformer())
    ConsulMetaDataProvider consulMetaDataProvider = Stub()

    private static final String CUSTOMER = "customer"
    private static final String CLUSTER = "cluster"
    private static final String LOCATION = "location"
    private static final String ENV = "environment"

    @Unroll
    def "TradingEngine with #tradingEngineMD discover #serviceName with NO expected #result"() {
        setup:
        consulMetaDataProvider.getMetadata() >> tradingEngineMD
        consulDiscoveryLibrary = new ConsulDiscoveryLibrary(consulDiscoveryClient,
                consulDiscoveryProperties,
                consulClient,
                healthServiceTransformer,
                consulMetaDataProvider)

        consulClient.getDatacenters() >> new Response(datacenters, new HttpResponse(200, "OK", "", 0L, true, 1L));
        consulDiscoveryClient.getInstances(serviceName, _ as QueryParams) >>> serviceInstancesPerDC
        consulMetaDataProvider.getMetadata() >> getTradingEngineMetadata()

        expect:
        def fi = consulDiscoveryLibrary.getServices(serviceName)
        fi.size() == result.size()

        where:
        tradingEngineMD                                                           | serviceName  | datacenters                  | serviceInstancesPerDC                        | result
        [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"] | "staticData" | getDatacenters()             | [getStaticDataDC1_3(), getStaticDataDC2_5()] | []
        [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"] | "staticData" | getNoDatacenters()           | []                                           | []
        [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"] | "staticData" | getNullResponseDatacenters() | []                                           | []
    }

    @Unroll
    def "TradingEngine with #tradingEngineMD discover #serviceName with ONE expected #result"() {
        setup:
        consulMetaDataProvider.getMetadata() >> tradingEngineMD
        consulDiscoveryLibrary = new ConsulDiscoveryLibrary(consulDiscoveryClient,
                consulDiscoveryProperties,
                consulClient,
                healthServiceTransformer,
                consulMetaDataProvider)

        consulClient.getDatacenters() >> new Response(datacenters, new HttpResponse(200, "OK", "", 0L, true, 1L));
        consulDiscoveryClient.getInstances(serviceName, _ as QueryParams) >>> serviceInstancesPerDC
        consulMetaDataProvider.getMetadata() >> getTradingEngineMetadata()

        expect:
        def fi = consulDiscoveryLibrary.getServices(serviceName)
        fi.size() == result.size()

        fi[0].getServiceId() == serviceName
        fi[0].getMetadata().get((CUSTOMER)) == result[0].get(CUSTOMER)
        fi[0].getMetadata().get((CLUSTER)) == result[0].get(CLUSTER)
        fi[0].getMetadata().get((LOCATION)) == result[0].get(LOCATION)
        fi[0].getMetadata().get((ENV)) == result[0].get(ENV)
        fi[0].getMetadata().get((ENV)) != "STAGING"
        fi[0].getMetadata().get((ENV)) != "PROD"

        where:
        tradingEngineMD                                                           | serviceName | datacenters      | serviceInstancesPerDC                | result
        [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"] | "pricer"    | getDatacenters() | [getPricerDC1_1(), getPricerDC2_1()] | [[(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]]
        [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"] | "pricer"    | getDatacenters() | [getPricerDC1_2(), getPricerDC2_1()] | [[(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC2", (ENV): "DEV"]]
    }

    @Unroll
    def "TradingEngine with #tradingEngineMD discover #serviceName with TWO expected #result"() {
        setup:
        consulMetaDataProvider.getMetadata() >> tradingEngineMD
        consulDiscoveryLibrary = new ConsulDiscoveryLibrary(consulDiscoveryClient,
                consulDiscoveryProperties,
                consulClient,
                healthServiceTransformer,
                consulMetaDataProvider)

        consulClient.getDatacenters() >> new Response(datacenters, new HttpResponse(200, "OK", "", 0L, true, 1L));
        consulDiscoveryClient.getInstances(serviceName, _ as QueryParams) >>> serviceInstancesPerDC
        consulMetaDataProvider.getMetadata() >> getTradingEngineMetadata()

        expect:
        def fi = consulDiscoveryLibrary.getServices(serviceName)
        fi.size() == result.size()

        fi[0].getServiceId() == serviceName
        fi[0].getMetadata().get((CUSTOMER)) == result[0].get(CUSTOMER)
        fi[0].getMetadata().get((CLUSTER)) == result[0].get(CLUSTER)
        fi[0].getMetadata().get((LOCATION)) == result[0].get(LOCATION)
        fi[0].getMetadata().get((ENV)) == result[0].get(ENV)
        fi[0].getMetadata().get((ENV)) != "STAGING"
        fi[0].getMetadata().get((ENV)) != "PROD"

        fi[1].getServiceId() == serviceName
        fi[1].getMetadata().get((CUSTOMER)) == result[1].get(CUSTOMER)
        fi[1].getMetadata().get((CLUSTER)) == result[1].get(CLUSTER)
        fi[1].getMetadata().get((LOCATION)) == result[1].get(LOCATION)
        fi[1].getMetadata().get((ENV)) == result[1].get(ENV)
        fi[1].getMetadata().get((ENV)) != "STAGING"
        fi[1].getMetadata().get((ENV)) != "PROD"

        where:
        tradingEngineMD                                                             | serviceName  | datacenters      | serviceInstancesPerDC                        | result
        [(CUSTOMER): "BDSI", (CLUSTER): "CHARLIE", (LOCATION): "DC1", (ENV): "DEV"] | "pricer"     | getDatacenters() | [getPricerDC1_2(), getPricerDC2_4()]         | [[(CUSTOMER): "BDSI", (CLUSTER): "BETA", (LOCATION): "DC2", (ENV): "DEV"], [(CUSTOMER): "BDSI", (CLUSTER): "ALPHA", (LOCATION): "DC2", (ENV): "DEV"]]
        [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]   | "staticData" | getDatacenters() | [getStaticDataDC1_3(), getStaticDataDC2_4()] | [[(CUSTOMER): "SHARED", (CLUSTER): "BETA", (LOCATION): "DC2", (ENV): "DEV"], [(CUSTOMER): "SHARED", (CLUSTER): "SHARED", (LOCATION): "DC2", (ENV): "DEV"]]
    }

    def "TradingEngine with #tradingEngineMD executeQuery will discover #serviceName with TWO expected #result"() {
        setup:
        consulMetaDataProvider.getMetadata() >> tradingEngineMD
        consulDiscoveryLibrary = new ConsulDiscoveryLibrary(consulDiscoveryClient,
                consulDiscoveryProperties,
                consulClient,
                healthServiceTransformer,
                consulMetaDataProvider)

        consulClient.executePreparedQuery(serviceName, _ as QueryParams) >> new Response(queryExecution, new HttpResponse(200, "OK", "", 0L, true, 1L));

        expect:
        def si = consulDiscoveryLibrary.executeQuery(serviceName)

        si[0].getServiceId() == serviceName
        si[0].getMetadata().get((CUSTOMER)) == result[0].get(CUSTOMER)
        si[0].getMetadata().get((CLUSTER)) == result[0].get(CLUSTER)
        si[0].getMetadata().get((LOCATION)) == result[0].get(LOCATION)
        si[0].getMetadata().get((ENV)) == result[0].get(ENV)
        si[0].getMetadata().get((ENV)) != "STAGING"
        si[0].getMetadata().get((ENV)) != "PROD"

        si[1].getServiceId() == serviceName
        si[1].getMetadata().get((CUSTOMER)) == result[1].get(CUSTOMER)
        si[1].getMetadata().get((CLUSTER)) == result[1].get(CLUSTER)
        si[1].getMetadata().get((LOCATION)) == result[1].get(LOCATION)
        si[1].getMetadata().get((ENV)) == result[1].get(ENV)
        si[1].getMetadata().get((ENV)) != "STAGING"
        si[1].getMetadata().get((ENV)) != "PROD"


        where:
        tradingEngineMD                                                           | serviceName | queryExecution      | result
        [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"] | "pricer"    | getQueryExecution() | [[(CUSTOMER): "ACME", (CLUSTER): "BETA", (LOCATION): "DC1", (ENV): "DEV"], [(CUSTOMER): "SHARED", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]]
    }

    private static Map<String, String> getTradingEngineMetadata() {
        return [(CUSTOMER): "ACME",
                (CLUSTER) : "ALPHA",
                (LOCATION): "DC1",
                (ENV)     : "DEV"] as Map<String, String>
    }

    private static List<Datacenter> getDatacenters() {
        def dc1 = new Datacenter()
        dc1.setDatacenter("dc1")
        dc1.setAreaId("dc1")
        def dc2 = new Datacenter()
        dc2.setDatacenter("dc2")
        dc2.setAreaId("dc2")

        return [dc1, dc2] as ArrayList<Datacenter>
    }

    private static QueryExecution getQueryExecution() {
        def mdPricer1 = [(CUSTOMER): "ACME", (CLUSTER): "BETA", (LOCATION): "DC1", (ENV): "DEV"]
        def mdPricer2 = [(CUSTOMER): "SHARED", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]

        QueryNode.Node node1 = new QueryNode.Node()
        node1.setId("pricer")
        node1.setMeta(mdPricer1)

        QueryNode.Service service1 = new QueryNode.Service()
        service1.setId("pricer")
        service1.setMeta(mdPricer1)
        service1.setAddress("127.0.0.1")
        service1.setPort(8080)

        QueryNode qn1 = new QueryNode()
        qn1.setNode(node1)
        qn1.setService(service1)

        QueryNode.Node node2 = new QueryNode.Node()
        node2.setId("pricer")
        node2.setMeta(mdPricer2)

        QueryNode.Service service2 = new QueryNode.Service()
        service2.setId("pricer")
        service2.setMeta(mdPricer2)
        service2.setAddress("127.0.0.2")
        service2.setPort(8080)

        QueryNode qn2 = new QueryNode()
        qn2.setNode(node2)
        qn2.setService(service2)

        QueryExecution qe = new QueryExecution()
        qe.setService("pricer")
        qe.setNodes([qn1, qn2] as ArrayList<QueryNode>)

        return qe
    }

    private static List<Datacenter> getNoDatacenters() {
        return [] as ArrayList<Datacenter>
    }

    private static List<Datacenter> getNullResponseDatacenters() {
        return null
    }

    private static List<ServiceInstance> getPricerDC1_1() {
        def mdPricer1 = [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]
        def mdPricer2 = [(CUSTOMER): "ACME", (CLUSTER): "BETA", (LOCATION): "DC1", (ENV): "DEV"]
        def mdPricer3 = [(CUSTOMER): "SHARED", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]

        ServiceInstance pricer1 = new ConsulServiceInstance("pricer", "pricer", "pricer1.host", 8080, true, mdPricer1, null)
        ServiceInstance pricer2 = new ConsulServiceInstance("pricer", "pricer", "pricer2.host", 8080, true, mdPricer2, null)
        ServiceInstance pricer3 = new ConsulServiceInstance("pricer", "pricer", "pricer3.host", 8080, true, mdPricer3, null)

        return [pricer1, pricer2, pricer3] as ArrayList<ServiceInstance>
    }

    private static List<ServiceInstance> getStaticDataDC1_1() {
        def mdStaticData1 = [(CUSTOMER): "SHARED", (CLUSTER): "BETA", (LOCATION): "DC1", (ENV): "DEV"]
        def mdStaticData2 = [(CUSTOMER): "STARK", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]

        ServiceInstance staticData1 = new ConsulServiceInstance("staticData", "staticData", "staticData1.host", 8080, true, mdStaticData1, null)
        ServiceInstance staticData2 = new ConsulServiceInstance("staticData", "staticData", "staticData2.host", 8080, true, mdStaticData2, null)

        return [staticData1, staticData2] as ArrayList<ServiceInstance>
    }

    private static List<ServiceInstance> getPricerDC1_2() {
        def mdPricer2 = [(CUSTOMER): "ACME", (CLUSTER): "BETA", (LOCATION): "DC1", (ENV): "DEV"]
        def mdPricer3 = [(CUSTOMER): "SHARED", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]

        ServiceInstance pricer2 = new ConsulServiceInstance("pricer", "pricer", "pricer2.host", 8080, true, mdPricer2, null)
        ServiceInstance pricer3 = new ConsulServiceInstance("pricer", "pricer", "pricer3.host", 8080, true, mdPricer3, null)

        return [pricer2, pricer3] as ArrayList<ServiceInstance>
    }

    private static List<ServiceInstance> getPricerDC1_3() {
        def mdPricer2 = [(CUSTOMER): "SHARED", (CLUSTER): "BETA", (LOCATION): "DC1", (ENV): "DEV"]
        def mdPricer3 = [(CUSTOMER): "SHARED", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]

        ServiceInstance pricer2 = new ConsulServiceInstance("pricer", "pricer", "pricer2.host", 8080, true, mdPricer2, null)
        ServiceInstance pricer3 = new ConsulServiceInstance("pricer", "pricer", "pricer3.host", 8080, true, mdPricer3, null)

        return [pricer2, pricer3] as ArrayList<ServiceInstance>
    }

    private static List<ServiceInstance> getStaticDataDC2_2() {
        def mdStaticData1 = [(CUSTOMER): "SHARED", (CLUSTER): "BETA", (LOCATION): "DC1", (ENV): "DEV"]
        def mdStaticData2 = [(CUSTOMER): "STARK", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "DEV"]

        ServiceInstance staticData1 = new ConsulServiceInstance("staticData", "staticData", "staticData1.host", 8080, true, mdStaticData1, null)
        ServiceInstance staticData2 = new ConsulServiceInstance("staticData", "staticData", "staticData2.host", 8080, true, mdStaticData2, null)

        return [staticData1, staticData2] as ArrayList<ServiceInstance>
    }

    private static List<ServiceInstance> getPricerDC2_1() {
        def mdPricer1 = [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC2", (ENV): "DEV"]
        def mdPricer2 = [(CUSTOMER): "ACME", (CLUSTER): "BETA", (LOCATION): "DC2", (ENV): "DEV"]
        def mdPricer3 = [(CUSTOMER): "SHARED", (CLUSTER): "ALPHA", (LOCATION): "DC2", (ENV): "DEV"]

        ServiceInstance pricer1 = new ConsulServiceInstance("pricer", "pricer", "pricer1.host", 8080, true, mdPricer1, null)
        ServiceInstance pricer2 = new ConsulServiceInstance("pricer", "pricer", "pricer2.host", 8080, true, mdPricer2, null)
        ServiceInstance pricer3 = new ConsulServiceInstance("pricer", "pricer", "pricer3.host", 8080, true, mdPricer3, null)

        return [pricer1, pricer2, pricer3] as ArrayList<ServiceInstance>
    }

    private static List<ServiceInstance> getPricerDC2_4() {
        def mdPricer1 = [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC2", (ENV): "DEV"]
        def mdPricer2 = [(CUSTOMER): "BDSI", (CLUSTER): "BETA", (LOCATION): "DC2", (ENV): "DEV"]
        def mdPricer3 = [(CUSTOMER): "BDSI", (CLUSTER): "ALPHA", (LOCATION): "DC2", (ENV): "DEV"]

        ServiceInstance pricer1 = new ConsulServiceInstance("pricer", "pricer", "pricer1.host", 8080, true, mdPricer1, null)
        ServiceInstance pricer2 = new ConsulServiceInstance("pricer", "pricer", "pricer2.host", 8080, true, mdPricer2, null)
        ServiceInstance pricer3 = new ConsulServiceInstance("pricer", "pricer", "pricer3.host", 8080, true, mdPricer3, null)

        return [pricer1, pricer2, pricer3] as ArrayList<ServiceInstance>

    }

    private static List<ServiceInstance> getStaticDataDC2_1() {
        def mdStaticData1 = [(CUSTOMER): "SHARED", (CLUSTER): "SHARED", (LOCATION): "DC2", (ENV): "DEV"]
        def mdStaticData3 = [(CUSTOMER): "ACME", (CLUSTER): "BETA", (LOCATION): "DC2", (ENV): "PROD"]
        def mdStaticData2 = [(CUSTOMER): "SHARED", (CLUSTER): "BETA", (LOCATION): "DC2", (ENV): "DEV"]

        ServiceInstance staticData1 = new ConsulServiceInstance("staticData", "staticData", "staticDat1a.host", 8080, true, mdStaticData1, null)
        ServiceInstance staticData2 = new ConsulServiceInstance("staticData", "staticData", "staticData2.host", 8080, true, mdStaticData2, null)
        ServiceInstance staticData3 = new ConsulServiceInstance("staticData", "staticData", "staticData3.host", 8080, true, mdStaticData3, null)

        return [staticData1, staticData2, staticData3] as ArrayList<ServiceInstance>
    }

    private static List<ServiceInstance> getStaticDataDC1_3() {
        def mdStaticData1 = [(CUSTOMER): "ACME", (CLUSTER): "SHARED", (LOCATION): "DC1", (ENV): "PROD"]
        def mdStaticData2 = [(CUSTOMER): "SHARED", (CLUSTER): "SHARED", (LOCATION): "DC1", (ENV): "PROD"]

        ServiceInstance staticData1 = new ConsulServiceInstance("staticData", "staticData", "staticData2.host", 8080, true, mdStaticData1, null)
        ServiceInstance staticData2 = new ConsulServiceInstance("staticData", "staticData", "staticData3.host", 8080, true, mdStaticData2, null)

        return [staticData1, staticData2] as ArrayList<ServiceInstance>
    }

    private static List<ServiceInstance> getStaticDataDC2_5() {
        def mdStaticData1 = [(CUSTOMER): "ACME", (CLUSTER): "SHARED", (LOCATION): "DC1", (ENV): "PROD"]
        def mdStaticData2 = [(CUSTOMER): "ACME", (CLUSTER): "ALPHA", (LOCATION): "DC1", (ENV): "PROD"]

        ServiceInstance staticData1 = new ConsulServiceInstance("staticData", "staticData", "staticData2.host", 8080, true, mdStaticData1, null)
        ServiceInstance staticData2 = new ConsulServiceInstance("staticData", "staticData", "staticData3.host", 8080, true, mdStaticData2, null)

        return [staticData1, staticData2] as ArrayList<ServiceInstance>
    }

    private static List<ServiceInstance> getStaticDataDC2_4() {
        def mdStaticData1 = [(CUSTOMER): "ACME", (CLUSTER): "SHARED", (LOCATION): "DC2", (ENV): "PROD"]
        def mdStaticData2 = [(CUSTOMER): "SHARED", (CLUSTER): "BETA", (LOCATION): "DC2", (ENV): "DEV"]
        def mdStaticData3 = [(CUSTOMER): "SHARED", (CLUSTER): "SHARED", (LOCATION): "DC2", (ENV): "DEV"]

        ServiceInstance staticData1 = new ConsulServiceInstance("staticData", "staticData", "staticData2.host", 8080, true, mdStaticData1, null)
        ServiceInstance staticData2 = new ConsulServiceInstance("staticData", "staticData", "staticData3.host", 8080, true, mdStaticData2, null)
        ServiceInstance staticData3 = new ConsulServiceInstance("staticData", "staticData", "staticData3.host", 8080, true, mdStaticData3, null)

        return [staticData1, staticData2, staticData3] as ArrayList<ServiceInstance>
    }
}