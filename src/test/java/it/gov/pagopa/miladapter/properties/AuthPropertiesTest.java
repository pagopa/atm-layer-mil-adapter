package it.gov.pagopa.miladapter.properties;

import org.junit.Test;
import static org.junit.Assert.*;

public class AuthPropertiesTest {

    @Test
    public void testGetSetMilAuthPath() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setMilAuthPath("testPath");
        assertEquals("testPath", authProperties.getMilAuthPath());
    }

    @Test
    public void testGetSetClientId() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setClientId("testClientId");
        assertEquals("testClientId", authProperties.getClientId());
    }

    @Test
    public void testGetSetClientSecret() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setClientSecret("testClientSecret");
        assertEquals("testClientSecret", authProperties.getClientSecret());
    }

    @Test
    public void testGetSetGrantType() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setGrantType("testGrantType");
        assertEquals("testGrantType", authProperties.getGrantType());
    }
}

