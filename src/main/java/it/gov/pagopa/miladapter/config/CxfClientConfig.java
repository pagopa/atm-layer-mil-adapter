package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.pagopa_api.nodeforpsp.NodeForPsp;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CxfClientConfig {

  @Value("${node.soap-client.endpoint-url}")
  private String nodeEndpointUrl;

  @Bean
  public NodeForPsp nodeForPsp() {
    JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(NodeForPsp.class);
    factory.setAddress(nodeEndpointUrl);
    return (NodeForPsp) factory.create();
  }
}
