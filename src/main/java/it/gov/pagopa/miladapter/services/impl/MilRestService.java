package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.client.MilRestClient;
import it.gov.pagopa.miladapter.client.model.AcquirerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MilRestService {

    private final MilRestClient milRestClient;

    @Cacheable(value = "cache-role", key = "{#authorization, #acquirerId}")
    public Mono<AcquirerConfiguration> getPspConfiguration(String authorization, String acquirerId) {
        log.debug("Calling MilRestClient to get PSP configuration for acquirerId: {}", acquirerId);
        return milRestClient.getPspConfiguration(authorization, acquirerId);
    }
}
