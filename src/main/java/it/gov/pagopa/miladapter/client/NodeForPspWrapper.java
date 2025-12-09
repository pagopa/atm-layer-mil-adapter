package it.gov.pagopa.miladapter.client;

import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.nodeforpsp.NodeForPsp;
import jakarta.annotation.PostConstruct;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Bean wrapping the interfaces of the generated CXF SOAP client
 */
@Service
@Slf4j
public class NodeForPspWrapper {

    private final NodeForPsp nodeForPsp;

    /**
     * Connect timeout vs the node SOAP services
     */
    @Value("${node.soap-client.connect-timeout:30000}")
    private long soapClientConnectTimeout;

    /**
     * Read timeout vs the node SOAP services
     */
    @Value("${node.soap-client.read-timeout:30000}")
    private long soapClientReadTimeout;

    /**
     * APIM subscription key to be passed as a header in the request to the node endpoint
     */
    @Value("${node.soap-client.apim-subscription-key}")
    private String apimSubscriptionKey;

    public NodeForPspWrapper(NodeForPsp nodeForPsp) {
        this.nodeForPsp = nodeForPsp;
    }

    /**
     * Wrapper method of verifyPaymentNotice interface
     * @param verifyPaymentNoticeReq the request to be serialized and passed to the node
     * @return a {@link VerifyPaymentNoticeRes} containing the response of the SOAP service
     */
    public VerifyPaymentNoticeRes verifyPaymentNotice(VerifyPaymentNoticeReq verifyPaymentNoticeReq) {
        return nodeForPsp.verifyPaymentNotice(verifyPaymentNoticeReq);
    }

    /**
     * Wrapper method of the activatePaymentNotice interface
     * @param activatePaymentNoticeV2Request the request to be serialized and passed to the node
     * @return a {@link ActivatePaymentNoticeV2Response} containing the response of the SOAP service
     */
    public ActivatePaymentNoticeV2Response activatePaymentNoticeV2(ActivatePaymentNoticeV2Request activatePaymentNoticeV2Request) {
        return nodeForPsp.activatePaymentNoticeV2(activatePaymentNoticeV2Request);
    }
    
    /**
     * Wrapper method of the sendPaymentOutcome interface
     * @param req the request to be serialized and passed to the node
     * @return a {@link SendPaymentOutcomeV2Response} containing the response of the SOAP service
     */
    public SendPaymentOutcomeV2Response sendPaymentOutcomeV2(SendPaymentOutcomeV2Request req) {
        return nodeForPsp.sendPaymentOutcomeV2(req);
    }

    /**
     * Configures the socket and connection timeout for the CXF SOAP client and
     * the API Management subscription key to be passed in the request
     */
    @PostConstruct
    void configureSoapClient() {
        Client client = ClientProxy.getClient(nodeForPsp);

        final var httpConduit = (HTTPConduit)client.getConduit();
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout(soapClientConnectTimeout);
        httpClientPolicy.setReceiveTimeout(soapClientReadTimeout);
        httpConduit.setClient(httpClientPolicy);

        if (apimSubscriptionKey != null && !apimSubscriptionKey.isEmpty()) {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Ocp-Apim-Subscription-Key", List.of(apimSubscriptionKey));
            client.getRequestContext().put(Message.PROTOCOL_HEADERS, headers);
        }

        // Disabilita verifica SSL solo in locale
        // disableSSLVerificationForDevelopment(httpConduit);
    }

    private void disableSSLVerificationForDevelopment(HTTPConduit httpConduit) {
        try {
            TLSClientParameters tlsParams = new TLSClientParameters();
            tlsParams.setDisableCNCheck(true);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            tlsParams.setSSLSocketFactory(sslContext.getSocketFactory());

            httpConduit.setTlsClientParameters(tlsParams);

            log.warn("SSL verification DISABLED - use only in development!");
        } catch (Exception e) {
            log.error("Error disabling SSL verification", e);
        }
    }
    
}
