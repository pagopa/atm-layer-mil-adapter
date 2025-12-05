package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.model.AuthParameters;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.DefinitionIdProperties;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.impl.DefinitionIdRestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DefinitionIdRestServiceImplTest {

    @Mock
    private RestConfigurationProperties restConfigurationProperties;

    @InjectMocks
    private DefinitionIdRestServiceImpl definitionIdRestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPrepareUri_AllParameters() {

        Configuration configuration = new Configuration();
        AuthParameters authParameters = new AuthParameters();
        authParameters.setAcquirerId("ACQ123");
        authParameters.setBranchId("BR123");
        authParameters.setTerminalId("TERM123");
        authParameters.setCode("CODE123");
        configuration.setAuthParameters(authParameters);
        configuration.setFunction("FUNC123");

        DefinitionIdProperties definitionIdProperties = new DefinitionIdProperties();
        definitionIdProperties.setMethod("GET");
        definitionIdProperties.setUrl("/definition/{acquirerId}/{branchId}/{terminalId}/{functionType}");

        when(restConfigurationProperties.getModelBasePath()).thenReturn("http://basepath.com");
        when(restConfigurationProperties.getDefinitionIdProperties()).thenReturn(definitionIdProperties);

        URI result = definitionIdRestService.prepareUri(configuration,"flow");

        assertEquals("http://basepath.com/definition/ACQ123/BR123/TERM123/FUNC123", result.toString());
    }

    @Test
    void testPrepareUri_NoBranchId() {

        Configuration configuration = new Configuration();
        AuthParameters authParameters = new AuthParameters();
        authParameters.setAcquirerId("ACQ123");
        authParameters.setBranchId(null);
        authParameters.setTerminalId("TERM123");
        authParameters.setCode("CODE123");
        configuration.setAuthParameters(authParameters);
        configuration.setFunction("FUNC123");

        DefinitionIdProperties definitionIdProperties = new DefinitionIdProperties();
        definitionIdProperties.setMethod("GET");
        definitionIdProperties.setUrl("/definition/{acquirerId}/{branchId}/{terminalId}/{functionType}");

        when(restConfigurationProperties.getModelBasePath()).thenReturn("http://basepath.com");
        when(restConfigurationProperties.getDefinitionIdProperties()).thenReturn(definitionIdProperties);

        URI result = definitionIdRestService.prepareUri(configuration, "flow");

        assertEquals("http://basepath.com/definition/ACQ123/_/TERM123/FUNC123", result.toString());
    }

    @Test
    void testPrepareUri_NoTerminalId() {

        Configuration configuration = new Configuration();
        AuthParameters authParameters = new AuthParameters();
        authParameters.setAcquirerId("ACQ123");
        authParameters.setBranchId("BR123");
        authParameters.setTerminalId(null);
        authParameters.setCode("CODE123");
        configuration.setAuthParameters(authParameters);
        configuration.setFunction("FUNC123");

        DefinitionIdProperties definitionIdProperties = new DefinitionIdProperties();
        definitionIdProperties.setMethod("GET");
        definitionIdProperties.setUrl("/definition/{acquirerId}/{branchId}/{terminalId}/{functionType}");

        when(restConfigurationProperties.getModelBasePath()).thenReturn("http://basepath.com");
        when(restConfigurationProperties.getDefinitionIdProperties()).thenReturn(definitionIdProperties);

        URI result = definitionIdRestService.prepareUri(configuration, "flow");

        assertEquals("http://basepath.com/definition/ACQ123/BR123/ACQ123CODE123/FUNC123", result.toString());
    }

    @Test
    void testPrepareUri_NoBranchIdAndTerminalId() {

        Configuration configuration = new Configuration();
        AuthParameters authParameters = new AuthParameters();
        authParameters.setAcquirerId("ACQ123");
        authParameters.setBranchId(null);
        authParameters.setTerminalId(null);
        authParameters.setCode("CODE123");
        configuration.setAuthParameters(authParameters);
        configuration.setFunction("FUNC123");

        DefinitionIdProperties definitionIdProperties = new DefinitionIdProperties();
        definitionIdProperties.setMethod("GET");
        definitionIdProperties.setUrl("/definition/{acquirerId}/{branchId}/{terminalId}/{functionType}");

        when(restConfigurationProperties.getModelBasePath()).thenReturn("http://basepath.com");
        when(restConfigurationProperties.getDefinitionIdProperties()).thenReturn(definitionIdProperties);

        URI result = definitionIdRestService.prepareUri(configuration, "flow");

        assertEquals("http://basepath.com/definition/ACQ123/_/ACQ123CODE123/FUNC123", result.toString());
    }
}
