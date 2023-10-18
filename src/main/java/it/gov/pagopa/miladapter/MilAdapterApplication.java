package it.gov.pagopa.miladapter;

import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RestConfigurationProperties.class)
public class MilAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilAdapterApplication.class, args);
    }

}
