package it.gov.pagopa.miladapter;

import it.gov.pagopa.miladapter.engine.task.impl.IDPayRestTaskHandler;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.IDPayRestService;
import org.h2.util.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpMethod;

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
    @Mock
    TaskExecutor taskRestExecutor;
    @Mock
    TaskExecutor taskComplExecutor;
    @InjectMocks
    IDPayRestTaskHandler iDpayRestTaskHandler;
    private int maxTaskConfig;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTaskRestExecutorTest(){
        assertEquals(taskRestExecutor,iDpayRestTaskHandler.getTaskRestExecutor());
    }

    @Test
    void getTaskComplExecutorTest(){
        assertEquals(taskComplExecutor,iDpayRestTaskHandler.getTaskComplExecutor());
    }

    @Test
    void getMaxTasks(){
        assertEquals(maxTaskConfig,iDpayRestTaskHandler.getMaxTasks());
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
        variables.put("bankId","expectedAcquirer");
        variables.put("channel","expectedChannel");
        variables.put("terminalId","expectedTerminal");
        variables.put("transactionId","expectedTransaction");
        variables.put("url","expectedEndpoint");
        variables.put("method","GET");
        Configuration result=iDpayRestTaskHandler.getHttpConfiguration(variables);
        assertEquals(HttpMethod.GET, result.getHttpMethod());
        assertEquals("expectedEndpoint", result.getEndpoint());
        assertEquals("expectedTransaction",result.getAuthParameters().getTransactionId());
    }
}