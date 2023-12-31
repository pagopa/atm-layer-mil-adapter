package it.gov.pagopa.miladapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

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

import it.gov.pagopa.miladapter.engine.task.impl.DefinitionIdRestTaskHandler;
import it.gov.pagopa.miladapter.enums.RequiredProcessVariables;
import it.gov.pagopa.miladapter.model.Configuration;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import it.gov.pagopa.miladapter.services.DefinitionIdRestService;
import it.gov.pagopa.miladapter.util.EngineVariablesToHTTPConfigurationUtils;

@ExtendWith(SpringExtension.class)
public class DefinitionIdRestTaskHandlerTest {

    @InjectMocks
    private DefinitionIdRestTaskHandler definitionIdRestTaskHandler;

    @Mock
    private DefinitionIdRestService definitionIdRestService;

    @Mock
    RestConfigurationProperties restConfigurationProperties;

    EasyRandom easyRandom;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        easyRandom = new EasyRandom();

    }

    @Test
    public void testExecuteOk() {
        ExternalTask externalTask = mock(ExternalTask.class);
        ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
        VariableMap variableMap = mock(VariableMap.class);
        Map<String, Object> variables = prepareInputVariables();

        when(restConfigurationProperties.isLogEngineInputVariablesEnabled()).thenReturn(true);
        when(definitionIdRestService.executeRestCall(any())).thenReturn(variableMap);
        when(externalTask.getAllVariables()).thenReturn(variables);
        when(externalTask.getId()).thenReturn("yourExternalTaskId");
        definitionIdRestTaskHandler.execute(externalTask, externalTaskService);

        verify(definitionIdRestService, times(1)).executeRestCall(any());
        verify(externalTaskService, times(1)).complete(externalTask, variableMap);
    }

    @Test
    public void testExecuteKo() {
        ExternalTask externalTask = mock(ExternalTask.class);
        ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
        Map<String, Object> variables = prepareInputVariables();
        Configuration configuration = EngineVariablesToHTTPConfigurationUtils
                .getHttpConfigurationInternalCall(variables, true);

        when(restConfigurationProperties.isLogEngineInputVariablesEnabled()).thenReturn(false);
        when(definitionIdRestService.executeRestCall(any())).thenThrow(new RuntimeException("exception"));
        when(externalTask.getAllVariables()).thenReturn(variables);
        when(externalTask.getId()).thenReturn("yourExternalTaskId");

        definitionIdRestTaskHandler.execute(externalTask, externalTaskService);

        verify(definitionIdRestService, times(1)).executeRestCall(any());
        verify(externalTaskService, times(1)).handleFailure(eq(externalTask), eq("exception"), any(), eq(0), eq(0L));
    }

    private static Map<String, Object> prepareInputVariables() {
        Map<String, Object> variables = new CaseInsensitiveMap<>();
        variables.put(RequiredProcessVariables.ACQUIRER_ID.getEngineValue(), "ACQUIRER_ID");
        variables.put(RequiredProcessVariables.TERMINAL_ID.getEngineValue(), "TERMINAL_ID");
        variables.put(RequiredProcessVariables.BRANCH_ID.getEngineValue(), "BRANCH_ID");
        variables.put(RequiredProcessVariables.CODE.getEngineValue(), "CODE");
        variables.put(RequiredProcessVariables.FUNCTION_ID.getEngineValue(), "FUNCTION_ID");
        return variables;
    }
}
