package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.model.KeyToken;
import it.gov.pagopa.miladapter.model.Token;
import it.gov.pagopa.miladapter.properties.CacheConfigurationProperties;
import it.gov.pagopa.miladapter.services.CacheService;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    CacheManager cacheManager;

    @Autowired
    CacheConfigurationProperties cacheConfigurationProperties;


    public Optional<Token> getToken(KeyToken key) {
        return Optional.ofNullable(cacheManager.getCache(cacheConfigurationProperties.getCacheName(), String.class, Token.class).get(key.toString()));
    }

    public void insertToken(KeyToken key, Token token) {
        cacheManager.getCache(cacheConfigurationProperties.getCacheName(), String.class, Token.class).put(key.toString(), token);
    }

    @Override
    public int calculateTokenDurationInSeconds(int milDuration) {
        if(milDuration > 0 && milDuration > cacheConfigurationProperties.getTokenSecurityThreshold()){
            return milDuration- cacheConfigurationProperties.getTokenSecurityThreshold();
        }
        return 0;
    }
}
