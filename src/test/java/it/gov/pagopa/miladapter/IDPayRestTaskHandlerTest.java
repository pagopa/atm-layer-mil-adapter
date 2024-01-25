package it.gov.pagopa.miladapter;

import it.gov.pagopa.miladapter.engine.task.impl.IDPayRestTaskHandler;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.IDPayRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IDPayRestTaskHandlerTest {
    @Mock
    IDPayRestService iDpayRestService;
    @Mock
    RestConfigurationProperties restConfigurationProperties;
    @InjectMocks
    IDPayRestTaskHandler iDpayRestTaskHandler;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLoggerTest() {
        assertInstanceOf(Logger.class, iDpayRestTaskHandler.getLogger());
    }

    @Test
    void getRestConfigurationPropertiesTest() {
        assertEquals(restConfigurationProperties, iDpayRestTaskHandler.getRestConfigurationProperties());
    }

    @Test
    void getRestServiceTest() {
        assertEquals(iDpayRestService, iDpayRestTaskHandler.getRestService());
    }

    @Test
    void isMILFlowTest() {
        assertTrue(iDpayRestTaskHandler.isMILFlow());
    }

    @Test
    void getHttpConfigurationTest() {
        Map<String, Object> variables = new HashMap<>();
    }
}