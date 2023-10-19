package it.gov.pagopa.miladapter.services;

import it.gov.pagopa.miladapter.model.KeyToken;
import it.gov.pagopa.miladapter.model.Token;
import it.gov.pagopa.miladapter.properties.CacheConfigurationProperties;
import it.gov.pagopa.miladapter.services.impl.CacheServiceImpl;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CacheServiceImplTest {

    @Mock
    CacheConfigurationProperties cacheConfigurationProperties;
    @Mock
    CacheManager cacheManager;
    @Mock
    KeyToken key;
    @Mock
    Token token;
    @InjectMocks
    CacheServiceImpl cacheServiceimpl;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(cacheConfigurationProperties.getCacheName()).thenReturn("cache name");
        Cache<String,Token> cache = Mockito.mock(Cache.class);
        when(cacheManager.getCache("cache name",String.class,Token.class)).thenReturn(cache);
        when(key.toString()).thenReturn("CHANNEL_ACQUIRERID_TERMINALID");
    }

    @Test
    public void getTokenTest() {
        cacheServiceimpl.getToken(key);
        verify(cacheManager,times(1)).getCache("cache name",String.class,Token.class);
        verify(cacheConfigurationProperties,times(1)).getCacheName();
    }

    @Test
    public void insertToken(){
        cacheServiceimpl.insertToken(key,token);
        verify(cacheManager,times(1)).getCache("cache name",String.class,Token.class);
        verify(cacheConfigurationProperties,times(1)).getCacheName();
    }

    @Test
    public void calculateTokenDurationInSecondsOKTest(){
        when(cacheConfigurationProperties.getTokenSecurityThreshold()).thenReturn(1);
        int result=cacheServiceimpl.calculateTokenDurationInSeconds(2);
        assertEquals(1,result);
    }

    @Test
    public void calculateTokenDurationInSecondsNegativeMilDurationTest(){
        when(cacheConfigurationProperties.getTokenSecurityThreshold()).thenReturn(1);
        int result=cacheServiceimpl.calculateTokenDurationInSeconds(-1);
        assertEquals(0,result);
    }

    @Test
    public void calculateTokenDurationInSecondsMilDurationNotHigherThanThresholdTest(){
        when(cacheConfigurationProperties.getTokenSecurityThreshold()).thenReturn(1);
        int result=cacheServiceimpl.calculateTokenDurationInSeconds(1);
        assertEquals(0,result);
    }

}
