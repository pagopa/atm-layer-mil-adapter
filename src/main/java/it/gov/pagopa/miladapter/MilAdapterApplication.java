package it.gov.pagopa.miladapter;

import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableConfigurationProperties(RestConfigurationProperties.class)
@EnableCaching
public class MilAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilAdapterApplication.class, args);
    }

}
