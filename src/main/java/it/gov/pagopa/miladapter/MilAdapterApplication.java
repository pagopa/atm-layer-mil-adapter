package it.gov.pagopa.miladapter;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import it.gov.pagopa.miladapter.properties.RestConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(RestConfigurationProperties.class)
public class MilAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilAdapterApplication.class, args);
    }

}
