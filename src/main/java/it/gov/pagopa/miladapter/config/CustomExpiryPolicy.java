package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.model.Token;
import org.ehcache.expiry.ExpiryPolicy;

import java.time.Duration;
import java.util.function.Supplier;

public class CustomExpiryPolicy implements ExpiryPolicy<String, Token> {

    @Override
    public Duration getExpiryForCreation(String string, Token token) {
        return Duration.ofSeconds(token.getExpires_in());
    }

    @Override
    public Duration getExpiryForAccess(String string, Supplier<? extends Token> supplier) {
        return null;
    }

    @Override
    public Duration getExpiryForUpdate(String string, Supplier<? extends Token> supplier, Token token) {
        return Duration.ofSeconds(token.getExpires_in());
    }
}