package it.gov.pagopa.miladapter.engine.task.impl;

import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.GenericRestServiceNoAuth;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class GenericRestNoAuthTaskHandlerTest {
    @InjectMocks
    private GenericRestNoAuthTaskHandler genericRestNoAuthTaskHandler;

    @Mock
    private GenericRestServiceNoAuth genericRestServiceNoAuth;

    @Mock
    RestConfigurationProperties restConfigurationProperties;

    @Mock
    private TaskExecutor taskRestExecutor;

    @Mock
    private TaskExecutor taskComplExecutor;


    EasyRandom easyRandom;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        easyRandom = new EasyRandom();

    }

    @Test
    public void testExecuteOk() {

        VariableMap variableMap = mock(VariableMap.class);
        ExternalTask externalTask = mock(ExternalTask.class);
        ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
        when(genericRestServiceNoAuth.executeRestCall(any(Configuration.class))).thenReturn(variableMap);
        Map<String, Object> variables = prepareInputVariables();
        when(restConfigurationProperties.isLogEngineInputVariablesEnabled()).thenReturn(true);
        when(externalTask.getAllVariables()).thenReturn(variables);
        doNothing().when(taskRestExecutor).execute(any());

        genericRestNoAuthTaskHandler.execute(externalTask, externalTaskService);

        verify(taskRestExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    public void testExecuteKo() {

        VariableMap variableMap = mock(VariableMap.class);
        ExternalTask externalTask = mock(ExternalTask.class);
        ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
        when(genericRestServiceNoAuth.executeRestCall(any(Configuration.class))).thenReturn(variableMap);
        Map<String, Object> variables = prepareInputVariables();
        when(restConfigurationProperties.isLogEngineInputVariablesEnabled()).thenReturn(true);
        when(externalTask.getAllVariables()).thenReturn(variables);
        doThrow(new RuntimeException("exception")).when(taskRestExecutor).execute(any());

        genericRestNoAuthTaskHandler.execute(externalTask, externalTaskService);

        verify(externalTaskService, times(1)).handleFailure(eq(externalTask), eq("exception"), any(), eq(0), eq(0L));
    }

    private static Map<String, Object> prepareInputVariables() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        variables.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "ACQUIRER_ID");
        variables.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "TERMINAL_ID");
        variables.put(RequiredProcessVariables.BRANCH_ID.getEngineValue(), "BRANCH_ID");
        variables.put(RequiredProcessVariables.CODE.getEngineValue(), "CODE");
        variables.put(RequiredProcessVariables.FUNCTION_ID.getEngineValue(), "FUNCTION_ID");
        variables.put(HttpVariablesEnum.URL.getValue(), "http://localhost:8080");
        variables.put(HttpVariablesEnum.METHOD.getValue(), "GET");
        return variables;
    }


}
