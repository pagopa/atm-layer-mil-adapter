package it.gov.pagopa.miladapter.client;

import it.gov.pagopa.miladapter.client.model.ADAccessToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AzureADRestClient {

  private final WebClient webClient;

  @Value("${azure-auth-api.version}")
  private String apiVersion;

  public AzureADRestClient(WebClient.Builder webClientBuilder, @Value("${azure-auth-api.base-url}") String baseUrl) {
    this.webClient = webClientBuilder.baseUrl(baseUrl).build();
  }

  /**
   * Retrieves an access token from Azure AD
   *
   * @param identity the identity header value
   * @param scope the resource scope
   * @return a {@link Mono} emitting the access token
   */
  public Mono<ADAccessToken> getAccessToken(String identity, String scope) {
    log.debug("Requesting Azure AD access token for identity: {}, resource: {}", identity, scope);

    return webClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .queryParam("api-version", apiVersion)
                    .queryParam("resource", scope)
                    .build())
        .header("x-identity-header", identity)
        .retrieve()
        .bodyToMono(ADAccessToken.class)
        .doOnSuccess(token -> log.debug("Successfully retrieved Azure AD access token"))
        .doOnError(error -> log.error("Error retrieving Azure AD access token", error));
  }
}
