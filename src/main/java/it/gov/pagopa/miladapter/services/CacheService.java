package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.model.KeyToken;
import it.gov.pagopa.miladapter.model.Token;

import java.util.Optional;

public interface CacheService {
    Optional<Token> getToken(KeyToken key);

    void insertToken(KeyToken key, Token token);

    int calculateTokenDurationInSeconds(int milDuration);
}
