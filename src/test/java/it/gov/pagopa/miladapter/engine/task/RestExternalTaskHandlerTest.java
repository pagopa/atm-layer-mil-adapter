package it.gov.pagopa.miladapter.engine.task;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
public class RestExternalTaskHandlerTest {

    EasyRandom easyRandom;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        easyRandom = new EasyRandom();
    }
    @Test
    void testExecute() {
        ExternalTask externalTask = mock(ExternalTask.class);
        ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
        Executor restExecutor = mock(Executor.class);
        Executor complExecutor = mock(Executor.class);
        CompletableFuture<VariableMap> resultAsync = CompletableFuture.completedFuture(mock(VariableMap.class));

    }

}

