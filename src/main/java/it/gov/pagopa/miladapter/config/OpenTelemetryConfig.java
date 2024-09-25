package it.gov.pagopa.miladapter.config;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration
public class OpenTelemetryConfig {

    @Bean
    OtlpGrpcSpanExporter otlpGrpcSpanExporter(@Value("${tracing.url}") String url) {
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(url)
                .build();
    }
}