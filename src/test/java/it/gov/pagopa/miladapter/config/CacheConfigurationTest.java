package it.gov.pagopa.miladapter.config;

import it.gov.pagopa.miladapter.properties.CacheConfigurationProperties;
import org.ehcache.CacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class CacheConfigurationTest {
    @Mock
    CacheConfigurationProperties cacheConfigurationProperties;
    @InjectMocks
    CacheConfiguration cacheConfiguration = new CacheConfiguration();

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(cacheConfigurationProperties.getCacheName()).thenReturn("cache name");
        when(cacheConfigurationProperties.getMaxEntries()).thenReturn(1);
    }

    @Test
    public void EhcacheManagerTest() {
        CacheManager result = cacheConfiguration.EhcacheManager();
        verify(cacheConfigurationProperties, times(1)).getCacheName();
        verify(cacheConfigurationProperties, times(1)).getMaxEntries();
        assert (result.getRuntimeConfiguration().getCacheConfigurations().containsKey("cache name"));
    }
}
