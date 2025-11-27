package it.gov.pagopa.miladapter.client;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import it.gov.pagopa.miladapter.client.model.GecGetFeesRequest;
import it.gov.pagopa.miladapter.client.model.GecGetFeesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
@Slf4j
public class FeeRestClient {

    private final WebClient webClient;

    @Value("${fees-api.max-occurrences}")
    private String maxOccurrences;

    @Value("${ocp.apim.subscription}")
    private String OcpApimSubscriptionKey;

    public FeeRestClient(WebClient.Builder webClientBuilder,
                         @Value("${fees-api.base-url}") String baseUrl) {
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
     * Client of the getFees API exposed by GEC
     *
     * @param requestId         the requestId passed by the client
     * @param gecGetFeesRequest the request to GEC
     * @return the response from GEC
     */
    public Mono<GecGetFeesResponse> getFees(String requestId, GecGetFeesRequest gecGetFeesRequest) {
        log.debug("Invoking GEC getFees API with requestId: {} and body: {}", requestId, gecGetFeesRequest);

        return webClient.post()
                .uri("/fees")
                .header("X-Request-Id", requestId)
                .header("maxOccurrences", maxOccurrences)
                .header("Ocp-Apim-Subscription-Key", OcpApimSubscriptionKey)
                .bodyValue(gecGetFeesRequest)
                .retrieve()
                .bodyToMono(GecGetFeesResponse.class)
                .doOnSuccess(response -> log.debug("Successfully retrieved fees response: {}", response))
                .doOnError(error -> log.error("Error retrieving fees", error));
    }

}
