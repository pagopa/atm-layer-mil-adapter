package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.client.NodeForPspWrapper;
import it.gov.pagopa.miladapter.client.model.CbillAbiFederazioneDto;
import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.properties.NodeErrorMappingProperties;
import it.gov.pagopa.miladapter.services.model.CommonHeader;
import it.gov.pagopa.miladapter.services.model.Fault;
import it.gov.pagopa.miladapter.util.NodeApi;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtFaultBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class BasePaymentService {

	private final NodeErrorMappingProperties nodeErrorMappingProperties;
	private final NodeForPspWrapper nodeWrapper;
    private final RestTemplate restTemplate;

    @Value("${reporting-service.base-url}")
    private String reportingServiceBaseUrl;

    @Value("${reporting-service.cbill-abi-federazione.path}")
    private String cbillAbiFederazionePath;

	public BasePaymentService(NodeErrorMappingProperties nodeErrorMappingProperties,
                              NodeForPspWrapper nodeWrapper,
                              RestTemplate restTemplate) {
		this.nodeErrorMappingProperties = nodeErrorMappingProperties;
		this.nodeWrapper = nodeWrapper;
        this.restTemplate = restTemplate;
	}

	/**
	 * Delegates the call to verifyPaymentNotice to the NodeForPspWrapper
	 *
	 * @param verifyPaymentNoticeReq the request to be sent to the node
	 * @return a {@link VerifyPaymentNoticeRes} with the response from the node
	 */
	public VerifyPaymentNoticeRes verifyPaymentNotice(VerifyPaymentNoticeReq verifyPaymentNoticeReq) {
		return nodeWrapper.verifyPaymentNotice(verifyPaymentNoticeReq);
	}

	/**
	 * Delegates the call to activatePaymentNoticeV2 to the NodeForPspWrapper
	 *
	 * @param activatePaymentNoticeV2Request the request to be sent to the node
	 * @return an {@link ActivatePaymentNoticeV2Response} with the response from the node
	 */
	public ActivatePaymentNoticeV2Response activatePaymentNoticeV2(ActivatePaymentNoticeV2Request activatePaymentNoticeV2Request) {
		return nodeWrapper.activatePaymentNoticeV2(activatePaymentNoticeV2Request);
	}

	/**
	 * Delegates the call to sendPaymentOutcomeV2 to the NodeForPspWrapper
	 *
	 * @param sendPaymentOutcomeV2Request the request to be sent to the node
	 * @return a {@link SendPaymentOutcomeV2Response} with the response from the node
	 */
	public SendPaymentOutcomeV2Response sendPaymentOutcomeV2(SendPaymentOutcomeV2Request sendPaymentOutcomeV2Request) {
		return nodeWrapper.sendPaymentOutcomeV2(sendPaymentOutcomeV2Request);
	}

	/**
	 * Retrieves the PSP configuration for the given acquirer and API type
	 *
	 * @param acquirerId the acquirer ID
	 * @param api the type of API (VERIFY, ACTIVATE, CLOSE, FEE)
	 * @return the {@link PspConfiguration} for the given acquirer
	 */
	public PspConfiguration retrievePSPConfiguration(String acquirerId, NodeApi api) {
		log.debug("retrievePSPConfiguration - acquirerId: {} ", acquirerId);
        //TODO integrare chiamata al reporting service per recuperare i dati reali e adattarli a PspConfiguration
        String url = UriComponentsBuilder
                .fromUriString(reportingServiceBaseUrl)
                .path(cbillAbiFederazionePath)
                .buildAndExpand(acquirerId)
                .toUriString();
        // CbillAbiFederazioneDto cbillAbiFederazione = restTemplate.getForObject(url, CbillAbiFederazioneDto.class);

        PspConfiguration pspConf = new PspConfiguration();
        pspConf.setPsp("AGID_01");
        pspConf.setBroker("97735020584");
        pspConf.setChannel("97735020584_03");
        pspConf.setPassword("pwd_AgID");
        return pspConf;
	}

	public String remapNodeFaultToOutcome(String faultCode, String originalFaultCode) {
		Integer outcomeErrorId = nodeErrorMappingProperties.getMap().
				get(Stream.of(faultCode, originalFaultCode)
						.filter(s -> s != null && !s.isEmpty())
						.collect(Collectors.joining("-")));
		if (outcomeErrorId == null) {
			log.error("Could not find configured mapping for faultCode {} originalFaultCode {}, defaulting to UNEXPECTED_ERROR",
					faultCode, originalFaultCode);
			outcomeErrorId = 0;
		}
		return nodeErrorMappingProperties.getOutcomes().get(outcomeErrorId);
	}

    /**
     * Generates the current timestamp (UTC time) in the uuuu-MM-dd'T'HH:mm:ss format
     * @return the timestamp
     */
    protected static String getTimestamp() {
        return LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Generates the deviceId to be passed as query param to the node in the close payment API
     *
     * @param commonHeader the object containing all the common headers used by the mil services
     * @return the deviceId value
     */
    protected String getDeviceId(CommonHeader commonHeader) {
        return StringUtils.join(List.of(commonHeader.getAcquirerId(), commonHeader.getTerminalId()), "|");
    }

    public Fault setFaultDetails(CtFaultBean faultBean) {
        Fault fault = new Fault();
        fault.setId(faultBean.getId());
        fault.setFaultCode(faultBean.getFaultCode());
        fault.setFaultString(faultBean.getOriginalFaultString());
        fault.setDescription(faultBean.getDescription());
        fault.setSerial(faultBean.getSerial());
        fault.setOriginalFaultCode(faultBean.getOriginalFaultCode());
        fault.setOriginalFaultString(faultBean.getOriginalFaultString());
        fault.setOriginalDescription(faultBean.getOriginalDescription());
        return fault;
    }
}
