package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.client.AzureADRestClient;
import it.gov.pagopa.miladapter.client.FeeRestClient;
import it.gov.pagopa.miladapter.client.NodeForPspWrapper;
import it.gov.pagopa.miladapter.client.model.GecGetFeesRequest;
import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.properties.NodeErrorMappingProperties;
import it.gov.pagopa.miladapter.services.model.CommonHeader;
import it.gov.pagopa.miladapter.services.model.Errors;
import it.gov.pagopa.miladapter.services.model.GetFeeResponse;
import it.gov.pagopa.miladapter.util.ErrorCode;
import it.gov.pagopa.miladapter.util.FeeCalculatorErrorCode;
import it.gov.pagopa.miladapter.util.FeeSelector;
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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BasePaymentService {

	private final NodeErrorMappingProperties nodeErrorMappingProperties;
	private final AzureADRestClient azureADRestClient;
	private final MilRestService milRestService;
	private final NodeForPspWrapper nodeWrapper;
    private final FeeRestClient feeRestClient;

	@Value("${azure-auth-api.identity}")
	private String identity;

	public static final String STORAGE = "https://storage.azure.com";
	private static final String BEARER = "Bearer ";

	public BasePaymentService(NodeErrorMappingProperties nodeErrorMappingProperties,
                              MilRestService milRestService,
                              NodeForPspWrapper nodeWrapper,
                              AzureADRestClient azureADRestClient,
                              FeeRestClient feeRestClient) {
		this.nodeErrorMappingProperties = nodeErrorMappingProperties;
		this.nodeWrapper = nodeWrapper;
		this.milRestService = milRestService;
		this.azureADRestClient = azureADRestClient;
        this.feeRestClient = feeRestClient;
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
	 * Delegates the call to getFees to the FeeRestClient and processes the response
	 *
	 * @param requestId the requestId from headers
	 * @param gecGetFeesRequest the request to be sent to GEC
	 * @return a {@link GetFeeResponse} with the calculated fee
	 */
	public GetFeeResponse getFees(String requestId, GecGetFeesRequest gecGetFeesRequest) {
		return (GetFeeResponse) feeRestClient.getFees(requestId, gecGetFeesRequest)
				.onErrorMap(t -> {
					log.error("[{}] Error while calling Fee REST service", FeeCalculatorErrorCode.ERROR_RETRIEVING_FEES, t);
					return new ResponseStatusException(
							HttpStatus.INTERNAL_SERVER_ERROR,
							new Errors(List.of(FeeCalculatorErrorCode.ERROR_RETRIEVING_FEES)).toString());
				})
				.handle((getFeesResponse, sink) -> {
					log.debug("Received GEC response: {}", getFeesResponse);
					long fee;
					try {
						fee = FeeSelector.getFirstFee(getFeesResponse.getBundleOptions());
					} catch (NoSuchElementException e) {
						log.error("[{}] No fee found for data in request", FeeCalculatorErrorCode.NO_FEE_FOUND);
						sink.error(new ResponseStatusException(
								HttpStatus.INTERNAL_SERVER_ERROR,
								new Errors(List.of(FeeCalculatorErrorCode.NO_FEE_FOUND)).toString()
						));
						return;
					}
					GetFeeResponse response = new GetFeeResponse();
					response.setFee(fee);
					log.debug("Fee calculation completed: {}", response);
					sink.next(response);
				})
				.block();
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
        PspConfiguration pspConf = new PspConfiguration();
        pspConf.setPsp("AGID_01");
        pspConf.setBroker("97735020584");
        pspConf.setChannel("97735020584_03");
        pspConf.setPassword("pwd_AgID");
        return pspConf;
        /*
		return azureADRestClient.getAccessToken(identity, STORAGE)
				.onErrorMap(t -> {
					log.error("[{}] Error while calling Azure AD rest service", ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES, t);
					return new ResponseStatusException(
							HttpStatus.INTERNAL_SERVER_ERROR,
							new Errors(List.of(ErrorCode.ERROR_CALLING_AZUREAD_REST_SERVICES)).toString());
				})
				.flatMap(token -> {
					log.debug("BasePaymentService -> retrievePspConfiguration: Azure AD service returned a 200 status, response token: [{}]", token);

					if (token.getToken() == null) {
						return Mono.error(new ResponseStatusException(
								HttpStatus.INTERNAL_SERVER_ERROR,
								new Errors(List.of(ErrorCode.AZUREAD_ACCESS_TOKEN_IS_NULL)).toString()));
					}

					return milRestService.getPspConfiguration(BEARER + token.getToken(), acquirerId)
							.onErrorMap(t -> {
								if (t instanceof WebClientResponseException webEx && webEx.getStatusCode().value() == 404) {
									log.error("[{}] Missing psp configuration for acquirerId", ErrorCode.UNKNOWN_ACQUIRER_ID, t);
									return new ResponseStatusException(
											HttpStatus.INTERNAL_SERVER_ERROR,
											new Errors(List.of(ErrorCode.UNKNOWN_ACQUIRER_ID)).toString());
								} else {
									log.error("[{}] Error retrieving the psp configuration", ErrorCode.ERROR_CALLING_MIL_REST_SERVICES, t);
									return new ResponseStatusException(
											HttpStatus.INTERNAL_SERVER_ERROR,
											new Errors(List.of(ErrorCode.ERROR_CALLING_MIL_REST_SERVICES)).toString());
								}
							})
							.map(acquirerConfiguration -> switch (api) {
								case ACTIVATE, VERIFY -> acquirerConfiguration.getPspConfigForVerifyAndActivate();
								case CLOSE, FEE -> acquirerConfiguration.getPspConfigForGetFeeAndClosePayment();
							});
				})
				.block();
         */
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
}
