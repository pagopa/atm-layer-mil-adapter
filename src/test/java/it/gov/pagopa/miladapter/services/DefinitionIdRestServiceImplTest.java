package it.gov.pagopa.miladapter.services;

import io.opentelemetry.api.trace.Tracer;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.resttemplate.RestTemplateGenerator;
import it.gov.pagopa.miladapter.services.impl.DefinitionIdRestServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

class DefinitionIdRestServiceImplTest {

    @Mock
    private RestConfigurationProperties restConfigurationProperties;
    @Mock
    private RestTemplate restTemplate;

    @Mock
    Tracer tracer;

    @Mock
    private RestTemplateGenerator restTemplateGenerator;
    @InjectMocks
    DefinitionIdRestServiceImpl definitionIdRestService;
    private Configuration configuration;


}
