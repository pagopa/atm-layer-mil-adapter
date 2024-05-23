package it.gov.pagopa.miladapter.config;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenTelemtryConfigTests {
    @InjectMocks
    OpenTelemetryConfig openTelemetryConfig = new OpenTelemetryConfig();

    @Test
    void customPoolTest() {
        OtlpGrpcSpanExporter otlpGrpcSpanExporter = openTelemetryConfig.otlpGrpcSpanExporter("http://provaUrl");
        assertTrue(otlpGrpcSpanExporter != null);

    }
}
