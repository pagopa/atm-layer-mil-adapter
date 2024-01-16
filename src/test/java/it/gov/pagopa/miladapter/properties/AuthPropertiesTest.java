package it.gov.pagopa.miladapter.properties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthPropertiesTest {
    @Test
    public void testGetSetMilAuthPath() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setMilAuthenticatorPath("testPath");
        assertEquals("testPath", authProperties.getMilAuthenticatorPath());
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

    private static AuthProperties generateAuthPropertiesForTests() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setMilAuthenticatorPath("milAuthPath");
        authProperties.setClientId("clientId");
        authProperties.setClientSecret("clientSecret");
        authProperties.setGrantType("grantType");
        return authProperties;
    }
}

