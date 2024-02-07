package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.properties.AdapterPoolConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CompletionThreadPoolConfig {

    @Autowired
    AdapterPoolConfigurationProperties adapterPoolConfigurationProperties;

    @Bean(name = "#{adapter-pool-configuration.completion.name}")
    public TaskExecutor customThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(adapterPoolConfigurationProperties.getCompletion().getCorePoolSize());
        executor.setMaxPoolSize(adapterPoolConfigurationProperties.getCompletion().getMaxPoolSize());
        executor.setThreadNamePrefix(adapterPoolConfigurationProperties.getCompletion().getThreadName());
        executor.initialize();
        return executor;
    }
}
