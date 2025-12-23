package it.gov.pagopa.miladapter.config;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.pagopa_api.nodeforpsp.NodeForPsp;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CxfClientConfigTest {

  private CxfClientConfig cxfClientConfig;
  private static final String TEST_ENDPOINT_URL = "http://localhost:8080/node-for-psp";

  @BeforeEach
  void setUp() throws Exception {
    cxfClientConfig = new CxfClientConfig();
    setPrivateField(cxfClientConfig, "nodeEndpointUrl", TEST_ENDPOINT_URL);
  }

  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void testNodeForPsp_BeanCreation() {
    // Act
    NodeForPsp nodeForPsp = cxfClientConfig.nodeForPsp();

    // Assert
    assertNotNull(nodeForPsp, "NodeForPsp bean should not be null");
  }

  @Test
  void testNodeForPsp_IsProxy() {
    // Act
    NodeForPsp nodeForPsp = cxfClientConfig.nodeForPsp();

    // Assert
    assertNotNull(nodeForPsp);
    assertTrue(
        nodeForPsp.getClass().getName().contains("Proxy"),
        "NodeForPsp should be a proxy instance");
  }

  @Test
  void testNodeForPsp_WithDifferentEndpoint() throws Exception {
    // Arrange
    String customEndpoint = "http://custom-host:9090/custom-path";
    setPrivateField(cxfClientConfig, "nodeEndpointUrl", customEndpoint);

    // Act
    NodeForPsp nodeForPsp = cxfClientConfig.nodeForPsp();

    // Assert
    assertNotNull(nodeForPsp, "NodeForPsp bean should be created with custom endpoint");
  }

  @Test
  void testNodeForPsp_MultipleInvocations() {
    // Act
    NodeForPsp nodeForPsp1 = cxfClientConfig.nodeForPsp();
    NodeForPsp nodeForPsp2 = cxfClientConfig.nodeForPsp();

    // Assert
    assertNotNull(nodeForPsp1);
    assertNotNull(nodeForPsp2);
    assertNotSame(
        nodeForPsp1,
        nodeForPsp2,
        "Each invocation should create a new proxy instance (not singleton by default)");
  }

  @Test
  void testNodeForPsp_WithNullEndpoint() throws Exception {
    // Arrange
    setPrivateField(cxfClientConfig, "nodeEndpointUrl", null);

    // Act & Assert
    assertDoesNotThrow(
        () -> cxfClientConfig.nodeForPsp(),
        "Should handle null endpoint gracefully during bean creation");
  }

  @Test
  void testNodeForPsp_WithEmptyEndpoint() throws Exception {
    // Arrange
    setPrivateField(cxfClientConfig, "nodeEndpointUrl", "");

    // Act
    NodeForPsp nodeForPsp = cxfClientConfig.nodeForPsp();

    // Assert
    assertNotNull(nodeForPsp, "NodeForPsp bean should be created even with empty endpoint");
  }

  @Test
  void testNodeForPsp_InterfaceImplementation() {
    // Act
    NodeForPsp nodeForPsp = cxfClientConfig.nodeForPsp();

    // Assert
    assertNotNull(nodeForPsp);
    assertTrue(
        NodeForPsp.class.isAssignableFrom(nodeForPsp.getClass()),
        "Created instance should implement NodeForPsp interface");
  }
}

