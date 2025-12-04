package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.client.AzureADRestClient;
import it.gov.pagopa.miladapter.client.FeeRestClient;
import it.gov.pagopa.miladapter.client.NodeForPspWrapper;
import it.gov.pagopa.miladapter.client.model.GecGetFeesRequest;
import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.properties.NodeErrorMappingProperties;
import it.gov.pagopa.miladapter.properties.NodeMappingProperties;
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
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
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
    private final NodeMappingProperties nodePaymentMethodMap;
	private final AzureADRestClient azureADRestClient;
	private final MilRestService milRestService;
	private final NodeForPspWrapper nodeWrapper;
    private final FeeRestClient feeRestClient;

	@Value("${azure-auth-api.identity}")
	private String identity;

	public static final String STORAGE = "https://storage.azure.com";
	private static final String BEARER = "Bearer ";

	public BasePaymentService(NodeErrorMappingProperties nodeErrorMappingProperties,
                                NodeMappingProperties nodePaymentMethodMap,
                              MilRestService milRestService,
                              NodeForPspWrapper nodeWrapper,
                              AzureADRestClient azureADRestClient,
                              FeeRestClient feeRestClient) {
		this.nodeErrorMappingProperties = nodeErrorMappingProperties;
        this.nodePaymentMethodMap = nodePaymentMethodMap;
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
		return nodeWrapper.verifyPaymentNotice(verifyPaymentNoticeReq).block();
	}

	/**
	 * Delegates the call to activatePaymentNoticeV2 to the NodeForPspWrapper
	 *
	 * @param activatePaymentNoticeV2Request the request to be sent to the node
	 * @return an {@link ActivatePaymentNoticeV2Response} with the response from the node
	 */
	public ActivatePaymentNoticeV2Response activatePaymentNoticeV2(ActivatePaymentNoticeV2Request activatePaymentNoticeV2Request) {
		return nodeWrapper.activatePaymentNoticeV2Async(activatePaymentNoticeV2Request).block();
	}

	/**
	 * Delegates the call to sendPaymentOutcomeV2 to the NodeForPspWrapper
	 *
	 * @param sendPaymentOutcomeV2Request the request to be sent to the node
	 * @return a {@link SendPaymentOutcomeV2Response} with the response from the node
	 */
	public SendPaymentOutcomeV2Response sendPaymentOutcomeV2(SendPaymentOutcomeV2Request sendPaymentOutcomeV2Request) {
		return nodeWrapper.sendPaymentOutcomeV2Async(sendPaymentOutcomeV2Request).block();
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
     * Creates a payment transaction to be stored in the DB from the data passed in request in the
     * {@link PaymentResource#preClose(CommonHeader, PreCloseRequest)} and the notice data retrieved from cache
     *
     * @param headers the MIL headers passed in request to the preClose
     * @param transactionId the transaction ID of the payment transaction
     * @param fees the fees of the payment transaction as returned by GEC
     * @param notices the list of notices retrieved from the cache
     * @param outcome the outcome passed in request of the preClose, con be PRE_CLOSE or ABORT
     * @return the {@link PaymentTransactionEntity} to be stored in the DB
     */
    /*
    protected static PaymentTransactionEntity createPaymentTransactionEntity(CommonHeader headers,
                                                                             String transactionId,
                                                                             Long fees,
                                                                             List<Notice> notices,
                                                                             String outcome,
                                                                             Preset preset) {
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionId(transactionId);
        paymentTransaction.setAcquirerId(headers.getAcquirerId());
        paymentTransaction.setChannel(headers.getChannel());
        paymentTransaction.setMerchantId(headers.getMerchantId());
        paymentTransaction.setTerminalId(headers.getTerminalId());
        paymentTransaction.setInsertTimestamp(getTimestamp());
        paymentTransaction.setNotices(notices);
        paymentTransaction.setTotalAmount(notices.stream().map(Notice::getAmount).reduce(Long::sum).orElse(0L));
        paymentTransaction.setFee(fees);
        paymentTransaction.setStatus(PaymentTransactionOutcome.PRE_CLOSE.name().equals(outcome) ?
                PaymentTransactionStatus.PRE_CLOSE.name() : PaymentTransactionStatus.ABORTED.name());

        paymentTransaction.setPreset(preset);
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        entity.transactionId = transactionId;
        entity.paymentTransaction = paymentTransaction;

        return entity;
    }

     */

    /**
     * Creates the request for the closePayment REST API of the node
     *
     * @param paymentMethod the payment method used for the e-money transaction
     * @param paymentTimestamp the timestamp of the e-money transaction
     * @param outcome the outcome of the e-money transaction
     * @param paymentTransaction the object containing the data of the payment transaction, retrieved from the DB
     * @param pspConfiguration the configuration of the PSP, retrieved from the MIL configuration API
     * @return the {@link NodeClosePaymentRequest} to be sent to the node
     */
    /*
    protected NodeClosePaymentRequest createNodeClosePaymentRequest(String paymentMethod,
                                                                    String paymentTimestamp,
                                                                    Outcome outcome,
                                                                    PaymentTransaction paymentTransaction,
                                                                    PspConfiguration pspConfiguration) {

        NodeClosePaymentRequest nodeClosePaymentRequest = new NodeClosePaymentRequest();

        nodeClosePaymentRequest.setPaymentTokens(paymentTransaction.getNotices().stream().map(Notice::getPaymentToken).toList());
        nodeClosePaymentRequest.setOutcome(outcome.name());
        nodeClosePaymentRequest.setIdPsp(pspConfiguration.getPsp());
        nodeClosePaymentRequest.setIdBrokerPSP(pspConfiguration.getBroker());
        nodeClosePaymentRequest.setIdChannel(pspConfiguration.getChannel());
        // remapping payment method based on property file
        nodeClosePaymentRequest.setPaymentMethod(nodePaymentMethodMap.getPaymentMethod().getOrDefault(paymentMethod, paymentMethod));
        nodeClosePaymentRequest.setTransactionId(paymentTransaction.getTransactionId());
        // conversion from euro cents to euro
        nodeClosePaymentRequest.setTotalAmount(BigDecimal.valueOf(paymentTransaction.getTotalAmount(), 2));
        nodeClosePaymentRequest.setFee(BigDecimal.valueOf(Objects.requireNonNullElse(paymentTransaction.getFee(), 0L), 2));
        // transform the date from LocalDateTime to ZonedDateTime as requested by the closePayment on the node
        ZonedDateTime timestampOperation = LocalDateTime.parse(paymentTimestamp).atZone(ZoneId.of("UTC"));
        nodeClosePaymentRequest.setTimestampOperation(timestampOperation.format(DateTimeFormatter.ISO_INSTANT));

        nodeClosePaymentRequest.setAdditionalPaymentInformations(new AdditionalPaymentInformations());

        return nodeClosePaymentRequest;
    }

     */



    /**
     * Generates the current timestamp (UTC time) in the uuuu-MM-dd'T'HH:mm:ss format
     * @return the timestamp
     */
    protected static String getTimestamp() {
        return LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Checks if transaction stored on DB was created by the client invoking the API
     *
     * @param headers the object containing all the common headers used by the mil services
     * @param paymentTransaction the payment transaction stored on the DB
     * @return true if transaction was created by the caller, false otherwise
     */
    /*
    protected boolean isTransactionLinkedToClient(CommonHeader headers, PaymentTransaction paymentTransaction) {
        return StringUtils.equals(headers.getAcquirerId(), paymentTransaction.getAcquirerId())
                && StringUtils.equals(headers.getMerchantId(), paymentTransaction.getMerchantId())
                && StringUtils.equals(headers.getChannel(), paymentTransaction.getChannel())
                && StringUtils.equals(headers.getTerminalId(), paymentTransaction.getTerminalId());
    }

     */

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
