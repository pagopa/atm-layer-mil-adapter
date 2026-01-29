package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.model.QrCode;
import it.gov.pagopa.miladapter.services.model.*;
import it.gov.pagopa.miladapter.util.ErrorCode;
import it.gov.pagopa.miladapter.util.LogSanitizer;
import it.gov.pagopa.miladapter.util.PaymentNoticeConstants;
import it.gov.pagopa.miladapter.util.QrCodeParser;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtQrCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import static it.gov.pagopa.miladapter.util.LogSanitizer.sanitizeForLog;

@Service
@Validated
@Slf4j
public class ActivatePaymentNoticeService {

    private final QrCodeParser qrCodeParser;
    private final BasePaymentService basePaymentService;

    /**
     * The expiration time of the payment token passed to the node
     */
    @Value("${paymentnotice.activatepayment.expiration-time}")
    BigInteger paymentNoticeExpirationTime;

	public ActivatePaymentNoticeService(QrCodeParser qrCodeParser, BasePaymentService basePaymentService) {
		this.qrCodeParser = qrCodeParser;
        this.basePaymentService = basePaymentService;
    }


	/**
	 * Activate a payment notice by its qr-code.
	 * The qr code contains, encoded, the tax code of the company and the payment notice number
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param b64UrlQrCode the base64url encoded qr-code
	 * @param activatePaymentNoticeRequest an {@link ActivatePaymentNoticeRequest} containing the amount and the idempotency key
	 * @return a {@link ActivatePaymentNoticeResponse} containing the result of the activation of the payment notice
	 */
	public ResponseEntity<ActivatePaymentNoticeResponse> activateByQrCode(
			@Valid CommonHeader headers,
			@Pattern(regexp = PaymentNoticeConstants.ENCODED_QRCODE_REGEX,
					message = "[" + ErrorCode.ENCODED_QRCODE_MUST_MATCH_REGEXP + "] qrCode must match \"{regexp}\"")
			String b64UrlQrCode,

			@Valid
			@NotNull(message = "[" + ErrorCode.ACTIVATE_REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
            ActivatePaymentNoticeRequest activatePaymentNoticeRequest) {

		log.debug("activateByQrCode - Input parameters: {}, b64UrlQrCode: {}", headers, sanitizeForLog(b64UrlQrCode));

		// parse qr-code to retrieve the notice number and the PA tax code
		QrCode parsedQrCode = qrCodeParser.b64UrlParse(b64UrlQrCode);
		log.debug("Decoded qrCode: {}", parsedQrCode);

        PspConfiguration pspConf = this.basePaymentService.retrievePSPConfiguration(headers.getAcquirerId());
        return this.callNodeActivatePaymentNotice(parsedQrCode.getPaTaxCode(), parsedQrCode.getNoticeNumber(), pspConf, activatePaymentNoticeRequest);
	}


	/**
	 * Activate a payment notice by its number and the tax code of the company
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param paTaxCode the tax code of the pa that created the payment notice
	 * @param noticeNumber the number of the payment notice
	 * @param activatePaymentNoticeRequest an {@link ActivatePaymentNoticeRequest} containing the amount and the idempotency key
	 * @return a {@link ActivatePaymentNoticeResponse} containing the result of the activation of the payment notice
	 */
	public ResponseEntity<ActivatePaymentNoticeResponse> activateByTaxCodeAndNoticeNumber(
			@Valid CommonHeader headers,
			@Pattern(regexp = PaymentNoticeConstants.PA_TAX_CODE_REGEX,
					message = "[" + ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
			String paTaxCode,
			
			@Pattern(regexp = PaymentNoticeConstants.NOTICE_NUMBER_REGEX,
					message = "[" + ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP + "] noticeNumber must match \"{regexp}\"")
			String noticeNumber,

			@Valid
			@NotNull(message = "[" + ErrorCode.ACTIVATE_REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
			ActivatePaymentNoticeRequest activatePaymentNoticeRequest) {

        log.debug(
            "activateByTaxCodeAndNoticeNumber - Input parameters: {}, paTaxCode: {}, noticeNumber: {}",
            headers,
            sanitizeForLog(paTaxCode),
            sanitizeForLog(noticeNumber));

        PspConfiguration pspConf = this.basePaymentService.retrievePSPConfiguration(headers.getAcquirerId());
		return this.callNodeActivatePaymentNotice(paTaxCode, noticeNumber, pspConf, activatePaymentNoticeRequest);
	}

	/**
	 * Branch of the activatePaymentNotice that retrieves the payment notice detail from the node
	 *
	 * @param paTaxCode the tax code of the pa that created the payment notice
	 * @param noticeNumber the number of the payment notice
	 * @param pspConfiguration the configuration of the PSP retrieved from the DB
	 * @param activatePaymentNoticeRequest the object received in request of the activatePaymentNotice
	 * @return a {@link ActivatePaymentNoticeResponse} containing the result of the activation of the payment notice
	 */
	private ResponseEntity<ActivatePaymentNoticeResponse> callNodeActivatePaymentNotice(String paTaxCode, String noticeNumber, PspConfiguration pspConfiguration,
														ActivatePaymentNoticeRequest activatePaymentNoticeRequest) {

		CtQrCode ctQrCode = new CtQrCode();
		ctQrCode.setFiscalCode(paTaxCode);
		ctQrCode.setNoticeNumber(noticeNumber);

		ActivatePaymentNoticeV2Request nodeActivateRequest = new ActivatePaymentNoticeV2Request();
		nodeActivateRequest.setIdPSP(pspConfiguration.getPsp());
		nodeActivateRequest.setIdBrokerPSP(pspConfiguration.getBroker());
		nodeActivateRequest.setIdChannel(pspConfiguration.getChannel());
		nodeActivateRequest.setPassword(pspConfiguration.getPassword());
		nodeActivateRequest.setIdempotencyKey(activatePaymentNoticeRequest.getIdempotencyKey());
		nodeActivateRequest.setQrCode(ctQrCode);
		nodeActivateRequest.setAmount(activatePaymentNoticeRequest.getAmount());
        nodeActivateRequest.setExpirationTime(paymentNoticeExpirationTime);

        try {
            final ActivatePaymentNoticeV2Response activateResponse = this.basePaymentService.activatePaymentNoticeV2(nodeActivateRequest);

            if (activateResponse == null) {
                log.error("[{}] Node returned null response", ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)).toString());
            }

            ActivatePaymentNoticeResponse response = this.buildResponse(activateResponse);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ResponseStatusException e) {
            // Re-throw ResponseStatusException
            throw e;
        } catch (Exception e) {
            log.error("[{}] Error calling the node activatePaymentNoticeV2 service", ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)).toString());
        }
	}

    /**
     * Builds the response of the activatePayment API based on the response from the node
     *
     * @param activateResponse the {@link ActivatePaymentNoticeV2Response} from the node
     * @return the {@link ActivatePaymentNoticeResponse} to be returned by the API
     */
	private ActivatePaymentNoticeResponse buildResponse(ActivatePaymentNoticeV2Response activateResponse) {
		if (Outcome.OK.name().equals(activateResponse.getOutcome().value())) {
			return this.buildResponseOk(activateResponse);
		} else {
			return this.buildResponseKo(activateResponse);
		}
	}

	/**
	 * Builds the OK response of the activatePayment API based on the response from the node
	 *
	 * @param response the {@link ActivatePaymentNoticeV2Response} from the node
	 * @return the {@link ActivatePaymentNoticeResponse} to be returned by the API
	 */
	private ActivatePaymentNoticeResponse buildResponseOk(ActivatePaymentNoticeV2Response response) {
		ActivatePaymentNoticeResponse activateResponse = new ActivatePaymentNoticeResponse();
		activateResponse.setOutcome(response.getOutcome().value());
		activateResponse.setAmount(response.getTotalAmount());
		activateResponse.setPaTaxCode(response.getFiscalCodePA());
		activateResponse.setPaymentToken(response.getPaymentToken());
        activateResponse.setDescription(response.getPaymentDescription());
        activateResponse.setCompany(response.getCompanyName());
        activateResponse.setOfficeName(response.getOfficeName());
		List<Transfer> transfers = new ArrayList<>();
		response.getTransferList().getTransfer().forEach(t -> {
			Transfer transfer = new Transfer();
            transfer.setIdTransfer(t.getIdTransfer());
            transfer.setTransferAmount(t.getTransferAmount());
			transfer.setPaTaxCode(t.getFiscalCodePA());
            transfer.setCompany(t.getCompanyName());
			transfer.setCategory(t.getTransferCategory());
            transfer.setIban(t.getIBAN());
            transfer.setRemittanceInformation(t.getRemittanceInformation());
			transfers.add(transfer);
		});
		activateResponse.setTransfers(transfers);
		activateResponse.setCreditorReferenceId(response.getCreditorReferenceId());
        activateResponse.setSuggestedPaFee(response.getSuggestedPaFee() != null ? response.getSuggestedPaFee() : BigDecimal.ZERO);
        activateResponse.setSuggestedUserFee(response.getSuggestedUserFee() != null ? response.getSuggestedUserFee() : BigDecimal.ZERO);
		return activateResponse;
	}

	/**
	 * Builds the KO response of the activatePayment API based on the response from the node
	 *
	 * @param response the {@link ActivatePaymentNoticeV2Response} from the node
	 * @return the {@link ActivatePaymentNoticeResponse} to be returned by the API
	 */
	private ActivatePaymentNoticeResponse buildResponseKo(ActivatePaymentNoticeV2Response response) {
		ActivatePaymentNoticeResponse activatePaymentNoticeResponse = new ActivatePaymentNoticeResponse();
		activatePaymentNoticeResponse.setOutcome(
				this.basePaymentService.remapNodeFaultToOutcome(
						response.getFault().getFaultCode(),
						response.getFault().getOriginalFaultCode()
				));
        activatePaymentNoticeResponse.setFault(this.basePaymentService.setFaultDetails(response.getFault()));
		log.error("Node activatePaymentNoticeV2 responded with fault [{}] and fault code [{}]",
                response.getFault().getFaultString(), response.getFault().getFaultCode());
		return activatePaymentNoticeResponse;
	}
}
