package it.gov.pagopa.miladapter.properties;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CacheConfigurationPropertiesTest {

    @Test
    public void testGetSetCacheName() {
        CacheConfigurationProperties properties = new CacheConfigurationProperties();
        properties.setCacheName("testCacheName");
        assertEquals("testCacheName", properties.getCacheName());
    }

    @Test
    public void testGetSetMaxEntries() {
        CacheConfigurationProperties properties = new CacheConfigurationProperties();
        properties.setMaxEntries(100);
        assertEquals(100, properties.getMaxEntries());
    }
}
