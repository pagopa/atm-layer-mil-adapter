package it.gov.pagopa.miladapter.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PspConfigurationTest {

  @Test
  void testToString_WithPassword() {
    // Arrange
    PspConfiguration config = new PspConfiguration();
    config.setPsp("PSP123");
    config.setBroker("BROKER456");
    config.setChannel("CHANNEL789");
    config.setPassword("secret-password");

    // Act
    String result = config.toString();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("psp='PSP123'"));
    assertTrue(result.contains("broker='BROKER456'"));
    assertTrue(result.contains("channel='CHANNEL789'"));
    assertTrue(result.contains("password='***'"));
    assertFalse(result.contains("secret-password"), "Password should be masked");
  }

  @Test
  void testToString_WithNullPassword() {
    // Arrange
    PspConfiguration config = new PspConfiguration();
    config.setPsp("PSP123");
    config.setBroker("BROKER456");
    config.setChannel("CHANNEL789");
    config.setPassword(null);

    // Act
    String result = config.toString();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("psp='PSP123'"));
    assertTrue(result.contains("broker='BROKER456'"));
    assertTrue(result.contains("channel='CHANNEL789'"));
    assertTrue(result.contains("password='null'"));
  }

  @Test
  void testToString_WithAllNullFields() {
    // Arrange
    PspConfiguration config = new PspConfiguration();

    // Act
    String result = config.toString();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("psp='null'"));
    assertTrue(result.contains("broker='null'"));
    assertTrue(result.contains("channel='null'"));
    assertTrue(result.contains("password='null'"));
  }

  @Test
  void testGettersAndSetters() {
    // Arrange
    PspConfiguration config = new PspConfiguration();
    String psp = "PSP123";
    String broker = "BROKER456";
    String channel = "CHANNEL789";
    String password = "secret-password";

    // Act
    config.setPsp(psp);
    config.setBroker(broker);
    config.setChannel(channel);
    config.setPassword(password);

    // Assert
    assertEquals(psp, config.getPsp());
    assertEquals(broker, config.getBroker());
    assertEquals(channel, config.getChannel());
    assertEquals(password, config.getPassword());
  }

  @Test
  void testToString_WithEmptyPassword() {
    // Arrange
    PspConfiguration config = new PspConfiguration();
    config.setPsp("PSP123");
    config.setBroker("BROKER456");
    config.setChannel("CHANNEL789");
    config.setPassword("");

    // Act
    String result = config.toString();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("password='***'"));
    assertFalse(result.contains("password=''"));
  }

  @Test
  void testToString_Format() {
    // Arrange
    PspConfiguration config = new PspConfiguration();
    config.setPsp("PSP123");
    config.setBroker("BROKER456");
    config.setChannel("CHANNEL789");
    config.setPassword("secret");

    // Act
    String result = config.toString();

    // Assert
    assertTrue(result.startsWith("PspConfiguration{"));
    assertTrue(result.endsWith("}"));
    assertTrue(result.contains("psp="));
    assertTrue(result.contains("broker="));
    assertTrue(result.contains("channel="));
    assertTrue(result.contains("password="));
  }
}

