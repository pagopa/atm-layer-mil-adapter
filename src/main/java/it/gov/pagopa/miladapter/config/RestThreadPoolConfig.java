package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.properties.AdapterPoolConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RestThreadPoolConfig {

    @Autowired
    AdapterPoolConfigurationProperties adapterPoolConfigurationProperties;

    @Bean(name = "#{adapter-pool-configuration.rest.name}")
    public TaskExecutor customThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(adapterPoolConfigurationProperties.getRest().getCorePoolSize());
        executor.setMaxPoolSize(adapterPoolConfigurationProperties.getRest().getMaxPoolSize());
        executor.setThreadNamePrefix(adapterPoolConfigurationProperties.getRest().getThreadName());
        executor.initialize();
        return executor;
    }
}
