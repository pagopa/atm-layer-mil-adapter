package it.gov.pagopa.miladapter.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.gov.pagopa.pagopa_api.node.nodeforpsp.*;
import it.gov.pagopa.pagopa_api.nodeforpsp.NodeForPsp;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NodeForPspWrapperTest {

  @Mock private NodeForPsp nodeForPsp;

  @Mock private Client client;

  @Mock private HTTPConduit httpConduit;

  private NodeForPspWrapper nodeForPspWrapper;

  @BeforeEach
  void setUp() throws Exception {
    nodeForPspWrapper = new NodeForPspWrapper(nodeForPsp);

    // Set private fields for timeout configuration
    setPrivateField(nodeForPspWrapper, "soapClientConnectTimeout", 30000L);
    setPrivateField(nodeForPspWrapper, "soapClientReadTimeout", 30000L);
    setPrivateField(nodeForPspWrapper, "apimSubscriptionKey", "test-subscription-key");
  }

  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void testVerifyPaymentNotice_Success() {
    // Arrange
    VerifyPaymentNoticeReq request = new VerifyPaymentNoticeReq();
    request.setIdPSP("PSP123");
    request.setIdBrokerPSP("BROKER123");
    request.setIdChannel("CHANNEL123");

    VerifyPaymentNoticeRes expectedResponse = new VerifyPaymentNoticeRes();
    expectedResponse.setOutcome(StOutcome.OK);

    when(nodeForPsp.verifyPaymentNotice(request)).thenReturn(expectedResponse);

    // Act
    VerifyPaymentNoticeRes result = nodeForPspWrapper.verifyPaymentNotice(request);

    // Assert
    assertNotNull(result);
    assertEquals(StOutcome.OK, result.getOutcome());
    verify(nodeForPsp).verifyPaymentNotice(request);
  }

  @Test
  void testActivatePaymentNoticeV2_Success() {
    // Arrange
    ActivatePaymentNoticeV2Request request = new ActivatePaymentNoticeV2Request();
    request.setIdPSP("PSP123");
    request.setIdBrokerPSP("BROKER123");
    request.setIdChannel("CHANNEL123");

    ActivatePaymentNoticeV2Response expectedResponse = new ActivatePaymentNoticeV2Response();
    expectedResponse.setOutcome(StOutcome.OK);

    when(nodeForPsp.activatePaymentNoticeV2(request)).thenReturn(expectedResponse);

    // Act
    ActivatePaymentNoticeV2Response result = nodeForPspWrapper.activatePaymentNoticeV2(request);

    // Assert
    assertNotNull(result);
    assertEquals(StOutcome.OK, result.getOutcome());
    verify(nodeForPsp).activatePaymentNoticeV2(request);
  }

  @Test
  void testSendPaymentOutcomeV2_Success() {
    // Arrange
    SendPaymentOutcomeV2Request request = new SendPaymentOutcomeV2Request();
    request.setIdPSP("PSP123");
    request.setIdBrokerPSP("BROKER123");
    request.setIdChannel("CHANNEL123");

    SendPaymentOutcomeV2Response expectedResponse = new SendPaymentOutcomeV2Response();
    expectedResponse.setOutcome(StOutcome.OK);

    when(nodeForPsp.sendPaymentOutcomeV2(request)).thenReturn(expectedResponse);

    // Act
    SendPaymentOutcomeV2Response result = nodeForPspWrapper.sendPaymentOutcomeV2(request);

    // Assert
    assertNotNull(result);
    assertEquals(StOutcome.OK, result.getOutcome());
    verify(nodeForPsp).sendPaymentOutcomeV2(request);
  }

  @Test
  void testVerifyPaymentNotice_WithError() {
    // Arrange
    VerifyPaymentNoticeReq request = new VerifyPaymentNoticeReq();

    VerifyPaymentNoticeRes expectedResponse = new VerifyPaymentNoticeRes();
    expectedResponse.setOutcome(StOutcome.KO);
    CtFaultBean fault = new CtFaultBean();
    fault.setFaultCode("PAA_PAYMENT_UNKNOWN");
    fault.setDescription("Payment notice not found");
    expectedResponse.setFault(fault);

    when(nodeForPsp.verifyPaymentNotice(request)).thenReturn(expectedResponse);

    // Act
    VerifyPaymentNoticeRes result = nodeForPspWrapper.verifyPaymentNotice(request);

    // Assert
    assertNotNull(result);
    assertEquals(StOutcome.KO, result.getOutcome());
    assertNotNull(result.getFault());
    assertEquals("PAA_PAYMENT_UNKNOWN", result.getFault().getFaultCode());
    verify(nodeForPsp).verifyPaymentNotice(request);
  }

  @Test
  void testActivatePaymentNoticeV2_WithError() {
    // Arrange
    ActivatePaymentNoticeV2Request request = new ActivatePaymentNoticeV2Request();

    ActivatePaymentNoticeV2Response expectedResponse = new ActivatePaymentNoticeV2Response();
    expectedResponse.setOutcome(StOutcome.KO);
    CtFaultBean fault = new CtFaultBean();
    fault.setFaultCode("PAA_PAYMENT_DUPLICATED");
    fault.setDescription("Payment already processed");
    expectedResponse.setFault(fault);

    when(nodeForPsp.activatePaymentNoticeV2(request)).thenReturn(expectedResponse);

    // Act
    ActivatePaymentNoticeV2Response result = nodeForPspWrapper.activatePaymentNoticeV2(request);

    // Assert
    assertNotNull(result);
    assertEquals(StOutcome.KO, result.getOutcome());
    assertNotNull(result.getFault());
    assertEquals("PAA_PAYMENT_DUPLICATED", result.getFault().getFaultCode());
    verify(nodeForPsp).activatePaymentNoticeV2(request);
  }

  @Test
  void testSendPaymentOutcomeV2_WithError() {
    // Arrange
    SendPaymentOutcomeV2Request request = new SendPaymentOutcomeV2Request();

    SendPaymentOutcomeV2Response expectedResponse = new SendPaymentOutcomeV2Response();
    expectedResponse.setOutcome(StOutcome.KO);
    CtFaultBean fault = new CtFaultBean();
    fault.setFaultCode("PAA_SINTASSI_EXTRAXSD");
    fault.setDescription("Syntax error");
    expectedResponse.setFault(fault);

    when(nodeForPsp.sendPaymentOutcomeV2(request)).thenReturn(expectedResponse);

    // Act
    SendPaymentOutcomeV2Response result = nodeForPspWrapper.sendPaymentOutcomeV2(request);

    // Assert
    assertNotNull(result);
    assertEquals(StOutcome.KO, result.getOutcome());
    assertNotNull(result.getFault());
    assertEquals("PAA_SINTASSI_EXTRAXSD", result.getFault().getFaultCode());
    verify(nodeForPsp).sendPaymentOutcomeV2(request);
  }

  @Test
  void testConfigureSoapClient_WithSubscriptionKey() throws Exception {
    // Arrange
    when(client.getConduit()).thenReturn(httpConduit);
    when(client.getRequestContext()).thenReturn(new java.util.HashMap<>());

    try (MockedStatic<ClientProxy> clientProxyMock = mockStatic(ClientProxy.class)) {
      clientProxyMock.when(() -> ClientProxy.getClient(nodeForPsp)).thenReturn(client);

      // Act
      nodeForPspWrapper.configureSoapClient();

      // Assert
      ArgumentCaptor<HTTPClientPolicy> policyCaptor = ArgumentCaptor.forClass(HTTPClientPolicy.class);
      verify(httpConduit).setClient(policyCaptor.capture());

      HTTPClientPolicy capturedPolicy = policyCaptor.getValue();
      assertEquals(30000L, capturedPolicy.getConnectionTimeout());
      assertEquals(30000L, capturedPolicy.getReceiveTimeout());

      // Verify subscription key header was added
      Map<String, Object> requestContext = client.getRequestContext();
      assertTrue(requestContext.containsKey(Message.PROTOCOL_HEADERS));

      @SuppressWarnings("unchecked")
      Map<String, List<String>> headers = (Map<String, List<String>>) requestContext.get(Message.PROTOCOL_HEADERS);
      assertTrue(headers.containsKey("Ocp-Apim-Subscription-Key"));
      assertEquals(List.of("test-subscription-key"), headers.get("Ocp-Apim-Subscription-Key"));
    }
  }

  @Test
  void testConfigureSoapClient_WithNullSubscriptionKey() throws Exception {
    // Arrange
    setPrivateField(nodeForPspWrapper, "apimSubscriptionKey", null);
    when(client.getConduit()).thenReturn(httpConduit);
    when(client.getRequestContext()).thenReturn(new java.util.HashMap<>());

    try (MockedStatic<ClientProxy> clientProxyMock = mockStatic(ClientProxy.class)) {
      clientProxyMock.when(() -> ClientProxy.getClient(nodeForPsp)).thenReturn(client);

      // Act
      nodeForPspWrapper.configureSoapClient();

      // Assert
      ArgumentCaptor<HTTPClientPolicy> policyCaptor = ArgumentCaptor.forClass(HTTPClientPolicy.class);
      verify(httpConduit).setClient(policyCaptor.capture());

      HTTPClientPolicy capturedPolicy = policyCaptor.getValue();
      assertEquals(30000L, capturedPolicy.getConnectionTimeout());
      assertEquals(30000L, capturedPolicy.getReceiveTimeout());

      // Verify subscription key header was NOT added
      Map<String, Object> requestContext = client.getRequestContext();
      assertFalse(requestContext.containsKey(Message.PROTOCOL_HEADERS));
    }
  }

  @Test
  void testConfigureSoapClient_WithEmptySubscriptionKey() throws Exception {
    // Arrange
    setPrivateField(nodeForPspWrapper, "apimSubscriptionKey", "");
    when(client.getConduit()).thenReturn(httpConduit);
    when(client.getRequestContext()).thenReturn(new java.util.HashMap<>());

    try (MockedStatic<ClientProxy> clientProxyMock = mockStatic(ClientProxy.class)) {
      clientProxyMock.when(() -> ClientProxy.getClient(nodeForPsp)).thenReturn(client);

      // Act
      nodeForPspWrapper.configureSoapClient();

      // Assert
      ArgumentCaptor<HTTPClientPolicy> policyCaptor = ArgumentCaptor.forClass(HTTPClientPolicy.class);
      verify(httpConduit).setClient(policyCaptor.capture());

      HTTPClientPolicy capturedPolicy = policyCaptor.getValue();
      assertEquals(30000L, capturedPolicy.getConnectionTimeout());
      assertEquals(30000L, capturedPolicy.getReceiveTimeout());

      // Verify subscription key header was NOT added (empty string)
      Map<String, Object> requestContext = client.getRequestContext();
      assertFalse(requestContext.containsKey(Message.PROTOCOL_HEADERS));
    }
  }

  @Test
  void testConfigureSoapClient_WithCustomTimeouts() throws Exception {
    // Arrange
    setPrivateField(nodeForPspWrapper, "soapClientConnectTimeout", 60000L);
    setPrivateField(nodeForPspWrapper, "soapClientReadTimeout", 90000L);
    when(client.getConduit()).thenReturn(httpConduit);
    when(client.getRequestContext()).thenReturn(new java.util.HashMap<>());

    try (MockedStatic<ClientProxy> clientProxyMock = mockStatic(ClientProxy.class)) {
      clientProxyMock.when(() -> ClientProxy.getClient(nodeForPsp)).thenReturn(client);

      // Act
      nodeForPspWrapper.configureSoapClient();

      // Assert
      ArgumentCaptor<HTTPClientPolicy> policyCaptor = ArgumentCaptor.forClass(HTTPClientPolicy.class);
      verify(httpConduit).setClient(policyCaptor.capture());

      HTTPClientPolicy capturedPolicy = policyCaptor.getValue();
      assertEquals(60000L, capturedPolicy.getConnectionTimeout());
      assertEquals(90000L, capturedPolicy.getReceiveTimeout());
    }
  }
}
