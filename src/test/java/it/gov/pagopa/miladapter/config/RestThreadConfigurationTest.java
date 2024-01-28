package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.properties.AdapterPoolConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestThreadConfigurationTest {
    @Mock
    AdapterPoolConfigurationProperties adapterPoolConfigurationProperties;
    @InjectMocks
    RestThreadPoolConfig restThreadPoolConfig = new RestThreadPoolConfig();

    AdapterPoolConfigurationProperties.PoolConfig poolConfig;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        poolConfig = mock(AdapterPoolConfigurationProperties.PoolConfig.class);
        when(adapterPoolConfigurationProperties.getRest()).thenReturn(poolConfig);
        when(adapterPoolConfigurationProperties.getRest().getThreadName()).thenReturn("thread-name");
        when(adapterPoolConfigurationProperties.getRest().getName()).thenReturn("name");
        when(adapterPoolConfigurationProperties.getRest().getCorePoolSize()).thenReturn(1);
        when(adapterPoolConfigurationProperties.getRest().getMaxPoolSize()).thenReturn(1);
    }

    @Test
    void customPoolTest() {
        TaskExecutor taskExecutor = restThreadPoolConfig.customThreadPool();
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) taskExecutor;
        assertEquals(1, executor.getCorePoolSize());
        assertEquals(1, executor.getMaxPoolSize());
        assertEquals("thread-name", executor.getThreadNamePrefix());

    }
}
