package it.gov.pagopa.miladapter;

import it.gov.pagopa.miladapter.engine.task.impl.MILRestTaskHandler;
import it.gov.pagopa.miladapter.enums.HttpVariablesEnum;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.MILRestService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class MILRestTaskHandlerTest {

    @InjectMocks
    private MILRestTaskHandler MILRestTaskHandler;

    @Mock
    private MILRestService milRestService;

    @Mock
    RestConfigurationProperties restConfigurationProperties;

    EasyRandom easyRandom;

    @Mock
    private TaskExecutor taskRestExecutor;

    @Mock
    private TaskExecutor taskComplExecutor;

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
        when(milRestService.executeRestCall(any(Configuration.class))).thenReturn(variableMap);
        Map<String, Object> variables = prepareInputVariables();
        when(restConfigurationProperties.isLogEngineInputVariablesEnabled()).thenReturn(true);
        when(externalTask.getAllVariables()).thenReturn(variables);
        doNothing().when(taskRestExecutor).execute(any());

        MILRestTaskHandler.execute(externalTask, externalTaskService);

        verify(taskRestExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    public void testExecuteKo() {

        VariableMap variableMap = mock(VariableMap.class);
        ExternalTask externalTask = mock(ExternalTask.class);
        ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
        when(milRestService.executeRestCall(any(Configuration.class))).thenReturn(variableMap);
        Map<String, Object> variables = prepareInputVariables();
        when(restConfigurationProperties.isLogEngineInputVariablesEnabled()).thenReturn(true);
        when(externalTask.getAllVariables()).thenReturn(variables);
        doThrow(new RuntimeException("exception")).when(taskRestExecutor).execute(any());

        MILRestTaskHandler.execute(externalTask, externalTaskService);

        verify(externalTaskService, times(1)).handleFailure(eq(externalTask), eq("exception"), any(), eq(0), eq(0L));
    }

    private static Map<String, Object> prepareInputVariables() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        variables.put(RequiredProcessVariables.CHANNEL.getEngineValue(), "ATM");
        variables.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "ACQUIRER_ID");
        variables.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "TERMINAL_ID");
        variables.put(HttpVariablesEnum.URL.name(), "URL");
        variables.put(HttpVariablesEnum.METHOD.name(), "GET");
        return variables;
    }
}
