package it.gov.pagopa.miladapter.properties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefinitionIdPropertiesTest {
    @Test
    public void testGetSetUrl() {
        DefinitionIdProperties definitionIdProperties = new DefinitionIdProperties();
        definitionIdProperties.setUrl("testUrl");
        assertEquals("testUrl", definitionIdProperties.getUrl());
    }

    @Test
    public void testGetSetMethod() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setClientId("testClientId");
        assertEquals("testClientId", authProperties.getClientId());
    }

    @Test
    public void testGetSetClientSecret() {
        DefinitionIdProperties definitionIdProperties = new DefinitionIdProperties();
        definitionIdProperties.setMethod("GET");
        assertEquals("GET", definitionIdProperties.getMethod());
    }

    @Test
    public void testHashCode() {
        DefinitionIdProperties authProperties = generateDefinitionIdPropertiesForTests();
        DefinitionIdProperties authProperties1 = generateDefinitionIdPropertiesForTests();
        assertEquals(authProperties.hashCode(), authProperties1.hashCode());
    }

    @Test
    public void testEquals() {
        DefinitionIdProperties authProperties = generateDefinitionIdPropertiesForTests();
        DefinitionIdProperties authProperties1 = generateDefinitionIdPropertiesForTests();
        assertEquals(authProperties1, authProperties);
    }

    private static DefinitionIdProperties generateDefinitionIdPropertiesForTests() {
        DefinitionIdProperties definitionIdProperties = new DefinitionIdProperties();
        definitionIdProperties.setMethod("method");
        definitionIdProperties.setUrl("url");
        return definitionIdProperties;
    }
}
