package it.gov.pagopa.miladapter;

import it.gov.pagopa.miladapter.engine.task.impl.MILRestTaskHandler;
import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.MILRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MILRestTaskHandlerTest {

    @InjectMocks
    private MILRestTaskHandler MILRestTaskHandler;

    @Mock
    private MILRestService milRestService;

    @Mock
    RestConfigurationProperties restConfigurationProperties;

    EasyRandom easyRandom;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        easyRandom = new EasyRandom();

    }

    @Test
    void testExecuteOk() {
        ExternalTask externalTask = mock(ExternalTask.class);
        ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
        VariableMap variableMap = mock(VariableMap.class);
        Map<String, Object> variables = prepareInputVariables();

        when(restConfigurationProperties.isLogEngineInputVariablesEnabled()).thenReturn(true);
        when(milRestService.executeRestCall(any())).thenReturn(variableMap);
        when(externalTask.getAllVariables()).thenReturn(variables);
        when(externalTask.getId()).thenReturn("yourExternalTaskId");
        MILRestTaskHandler.execute(externalTask, externalTaskService);

        verify(milRestService, times(1)).executeRestCall(any());
        verify(externalTaskService, times(1)).complete(externalTask, variableMap);
    }

    @Test
    void testExecuteKo() {
        ExternalTask externalTask = mock(ExternalTask.class);
        ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
        Map<String, Object> variables = prepareInputVariables();
        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationExternalCall(variables, true);

        when(restConfigurationProperties.isLogEngineInputVariablesEnabled()).thenReturn(false);
        when(milRestService.executeRestCall(any())).thenThrow(new RuntimeException("exception"));
        when(externalTask.getAllVariables()).thenReturn(variables);
        when(externalTask.getId()).thenReturn("yourExternalTaskId");

        MILRestTaskHandler.execute(externalTask, externalTaskService);

        verify(milRestService, times(1)).executeRestCall(any());
        verify(externalTaskService, times(1)).handleFailure(eq(externalTask), eq("exception"), any(), eq(0), eq(0L));
    }

    private static Map<String, Object> prepareInputVariables() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        variables.put(RequiredProcessVariables.CHANNEL.getEngineValue(), "ATM");
        variables.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "ACQUIRER_ID");
        variables.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "TERMINAL_ID");
        variables.put(RequiredProcessVariables.TRANSACTION_ID.getEngineValue(), "TRANSACTION_ID");
        variables.put(HttpVariablesEnum.URL.name(), "URL");
        variables.put(HttpVariablesEnum.METHOD.name(), "GET");
        return variables;
    }
}