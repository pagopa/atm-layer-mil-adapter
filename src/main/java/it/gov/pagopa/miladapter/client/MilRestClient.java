package it.gov.pagopa.miladapter.client;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import it.gov.pagopa.miladapter.client.model.AcquirerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Reactive rest client for the REST APIs exposed by the MIL APIM
 */
@Component
@Slf4j
public class MilRestClient {

    private final WebClient webClient;

    @Value("${azure-storage-api.version}")
    private String apiVersion;

    public MilRestClient(WebClient.Builder webClientBuilder,
                         @Value("${mil-rest-api.base-url}") String baseUrl) {
        // TODO remove insecure trust manager
        HttpClient httpClient = HttpClient.create()
                .secure(spec -> spec.sslContext(
                        SslContextBuilder.forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                ));

        this.webClient =
                webClientBuilder
                        .clientConnector(new ReactorClientHttpConnector(httpClient))
                        .baseUrl(baseUrl)
                        .build();
    }

    /**
     * Retrieves the psp configuration
     *
     * @param authorization the authorization token (Bearer token)
     * @param acquirerId the acquirer id passed in request
     * @return a {@link Mono} emitting the psp configuration for the acquirer id
     */
    public Mono<AcquirerConfiguration> getPspConfiguration(String authorization, String acquirerId) {
        log.debug("Requesting PSP configuration for acquirerId: {}", acquirerId);

        return webClient.get()
                .uri("/acquirers/{acquirerId}.json", acquirerId)
                .header("Authorization", authorization)
                .header("x-ms-version", apiVersion)
                .retrieve()
                .bodyToMono(AcquirerConfiguration.class)
                .doOnSuccess(config -> log.debug("Successfully retrieved PSP configuration for acquirerId: {}", acquirerId))
                .doOnError(error -> log.error("Error retrieving PSP configuration for acquirerId: {}", acquirerId, error));
    }
}
