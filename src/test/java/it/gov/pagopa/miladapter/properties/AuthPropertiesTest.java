package it.gov.pagopa.miladapter.properties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testToString() {
        AuthProperties authProperties = generateAuthPropertiesForTests();
        assertEquals("AuthProperties(" +
                "milAuthPath=milAuthPath, " +
                "clientId=clientId, " +
                "clientSecret=clientSecret, " +
                "grantType=grantType)", authProperties.toString());
    }

    @Test
    public void testHashCode() {
        AuthProperties authProperties = generateAuthPropertiesForTests();
        AuthProperties authProperties1 = generateAuthPropertiesForTests();
        assertEquals(authProperties.hashCode(), authProperties1.hashCode());
    }

    @Test
    public void testEquals() {
        AuthProperties authProperties = generateAuthPropertiesForTests();
        AuthProperties authProperties1 = generateAuthPropertiesForTests();
        assertEquals(authProperties1,authProperties);
    }
/*
    @Test
    public void testCanEqual() {
        AuthProperties authProperties = generateAuthPropertiesForTests();
        AuthProperties authProperties1 = generateAuthPropertiesForTests();
        authProperties1.setMilAuthPath("different value");
        assertTrue(authProperties.canEqual(authProperties));
        assertTrue(authProperties.canEqual(authProperties1));
    }

 */

    private static AuthProperties generateAuthPropertiesForTests() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setMilAuthPath("milAuthPath");
        authProperties.setClientId("clientId");
        authProperties.setClientSecret("clientSecret");
        authProperties.setGrantType("grantType");
        return authProperties;
    }
}

