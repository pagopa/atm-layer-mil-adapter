package it.gov.pagopa.miladapter.services.impl;

import it.gov.pagopa.miladapter.model.PspConfiguration;
import it.gov.pagopa.miladapter.model.QrCode;
import it.gov.pagopa.miladapter.services.model.CommonHeader;
import it.gov.pagopa.miladapter.services.model.Errors;
import it.gov.pagopa.miladapter.services.model.Outcome;
import it.gov.pagopa.miladapter.services.model.VerifyPaymentNoticeResponse;
import it.gov.pagopa.miladapter.util.*;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtPaymentOptionDescription;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.CtQrCode;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
@Slf4j
public class VerifyPaymentNoticeService {

	private final QrCodeParser qrCodeParser;
	private final BasePaymentService basePaymentService;

	public VerifyPaymentNoticeService(QrCodeParser qrCodeParser, BasePaymentService basePaymentService) {
		this.qrCodeParser = qrCodeParser;
		this.basePaymentService = basePaymentService;
	}

	/**
	 * Retrieve the data of a payment notice by its qr-code.
	 * The qr code contains, encoded, the tax code of the company and the payment notice number
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param b64UrlQrCode the base64url-encoded qr-code
	 * @return a {@link VerifyPaymentNoticeResponse} containing the data of the payment notice retrieved from the node
	 */
	public ResponseEntity<VerifyPaymentNoticeResponse> verifyByQrCode(
			@Valid CommonHeader headers,
			@Pattern(regexp = PaymentNoticeConstants.ENCODED_QRCODE_REGEX, message = "[" + ErrorCode.ENCODED_QRCODE_MUST_MATCH_REGEXP + "] qrCode must match \"{regexp}\"")
			String b64UrlQrCode) {

		log.debug("verifyPaymentNoticeByQrCode - Input parameters: {}, b64UrlQrCode: {}", headers, b64UrlQrCode);

		// parse qr-code to retrieve the notice number and the PA tax code
		QrCode parsedQrCode = qrCodeParser.b64UrlParse(b64UrlQrCode);
		log.debug("Decoded qrCode: {}", parsedQrCode);

		PspConfiguration pspConf = this.basePaymentService.retrievePSPConfiguration(headers.getAcquirerId());

		return callNodeVerifyPaymentNotice(parsedQrCode.getPaTaxCode(), parsedQrCode.getNoticeNumber(), pspConf);
	}

	/**
	 * Retrieve the data of a payment notice by its number and the tax code of the company
	 *
	 * @param headers the object containing all the common headers used by the mil services
	 * @param paTaxCode the tax code of the pa that created the payment notice
	 * @param noticeNumber the number of the payment notice
	 * @return a {@link VerifyPaymentNoticeResponse} containing the data of the payment notice retrieved from the node
	 */
	public ResponseEntity<VerifyPaymentNoticeResponse> verifyByTaxCodeAndNoticeNumber(
			@Valid CommonHeader headers,

			@Pattern(regexp = PaymentNoticeConstants.PA_TAX_CODE_REGEX, message = "[" + ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
			String paTaxCode,

			@Pattern(regexp = PaymentNoticeConstants.NOTICE_NUMBER_REGEX, message = "[" + ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP + "] noticeNumber must match \"{regexp}\"")
			String noticeNumber) {

		log.debug("verifyByTaxCodeAndNoticeNumber - Input parameters: {}, paTaxCode: {}, noticeNumber: {}", headers, paTaxCode, noticeNumber);

		PspConfiguration pspConf = this.basePaymentService.retrievePSPConfiguration(headers.getAcquirerId());
		return callNodeVerifyPaymentNotice(paTaxCode, noticeNumber, pspConf);
	}

	/**
	 * Branch of the verifyPaymentNotice that retrieves the payment notice detail from the node
	 *
	 * @param paTaxCode the tax code of the pa that created the payment notice
	 * @param noticeNumber the number of the payment notice
	 * @param pspConfiguration the configuration of the PSP retrieved from the DB
	 * @return a {@link VerifyPaymentNoticeResponse} containing the data retrieved from the node
	 */
	private ResponseEntity<VerifyPaymentNoticeResponse> callNodeVerifyPaymentNotice(String paTaxCode, String noticeNumber, PspConfiguration pspConfiguration) {

        final VerifyPaymentNoticeReq verifyPaymentNoticeReq = getVerifyPaymentNoticeReq(paTaxCode, noticeNumber, pspConfiguration);

        try {
			VerifyPaymentNoticeRes nodeResponse = this.basePaymentService.verifyPaymentNotice(verifyPaymentNoticeReq);

			VerifyPaymentNoticeResponse verifyPaymentNoticeResponse;
			if (nodeResponse != null && Outcome.OK.name().equals(nodeResponse.getOutcome().name())) {
				log.debug("Node verifyPaymentNotice responded with outcome OK, {}",
						NodeForPspLoggingUtil.toString(nodeResponse));
				verifyPaymentNoticeResponse = buildResponseOk(nodeResponse, noticeNumber);
			}
			else if (nodeResponse != null) {
				log.debug("Node verifyPaymentNotice responded with outcome KO, {}",
						NodeForPspLoggingUtil.toString(nodeResponse.getFault()));
				verifyPaymentNoticeResponse = buildResponseKo(nodeResponse);
			}
			else {
				log.error("[{}] Node returned null response", ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES);
				throw new ResponseStatusException(
						HttpStatus.INTERNAL_SERVER_ERROR,
						new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)).toString());
			}

			log.debug("verifyPaymentNotice: Response {}", verifyPaymentNoticeResponse);
			return ResponseEntity.status(HttpStatus.OK).body(verifyPaymentNoticeResponse);

		} catch (Exception e) {
			log.error("[{}] Error calling the node verifyPaymentNotice service", ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES, e);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_SOAP_SERVICES)).toString());
		}
	}

    private static VerifyPaymentNoticeReq getVerifyPaymentNoticeReq(String paTaxCode, String noticeNumber, PspConfiguration pspConfiguration) {
        CtQrCode ctQrCode = new CtQrCode();
        ctQrCode.setFiscalCode(paTaxCode);
        ctQrCode.setNoticeNumber(noticeNumber);

        VerifyPaymentNoticeReq verifyPaymentNoticeReq = new VerifyPaymentNoticeReq();

        verifyPaymentNoticeReq.setIdPSP(pspConfiguration.getPsp());
        verifyPaymentNoticeReq.setIdBrokerPSP(pspConfiguration.getBroker());
        verifyPaymentNoticeReq.setIdChannel(pspConfiguration.getChannel());
        verifyPaymentNoticeReq.setPassword(pspConfiguration.getPassword());

        verifyPaymentNoticeReq.setQrCode(ctQrCode);
        return verifyPaymentNoticeReq;
    }


    /**
	 * Builds the OK response of the verifyPayment API based on the response from the node
	 *
	 * @param response the {@link VerifyPaymentNoticeRes} from the node
	 * @return the OK {@link VerifyPaymentNoticeResponse} to be returned by the API
	 */
	private VerifyPaymentNoticeResponse buildResponseOk(VerifyPaymentNoticeRes response, String noticeNumber) {
		VerifyPaymentNoticeResponse verifyResponse = new VerifyPaymentNoticeResponse();
		verifyResponse.setOutcome(response.getOutcome().value());
		verifyResponse.setDescription(response.getPaymentDescription());
		verifyResponse.setCompany(response.getCompanyName());
		verifyResponse.setOffice(response.getOfficeName());
		// only the first element of the payment list is returned
		if (response.getPaymentList().getPaymentOptionDescription() != null) {
			CtPaymentOptionDescription paymentOptionDescription = response.getPaymentList().getPaymentOptionDescription().getFirst();
			log.debug("Node verifyPaymentNotice responded with , {}",
					NodeForPspLoggingUtil.toString(paymentOptionDescription));
			// conversion from euro to euro cents
			verifyResponse.setAmount(paymentOptionDescription.getAmount().multiply(new BigDecimal(100)).toBigInteger());
			verifyResponse.setDueDate(paymentOptionDescription.getDueDate().toString());
			verifyResponse.setNote(paymentOptionDescription.getPaymentNote());
		}
		verifyResponse.setPaTaxCode(response.getFiscalCodePA());
		verifyResponse.setNoticeNumber(noticeNumber);
		return verifyResponse;
	}


	/**
	 * Builds the KO response of the verifyPayment API based on the response from the node
	 *
	 * @param response the {@link VerifyPaymentNoticeRes} from the node
	 * @return the KO {@link VerifyPaymentNoticeResponse} to be returned by the API
	 */
	private VerifyPaymentNoticeResponse buildResponseKo(VerifyPaymentNoticeRes response) {
		VerifyPaymentNoticeResponse verifyResponse = new VerifyPaymentNoticeResponse();
		verifyResponse.setOutcome(
				this.basePaymentService.remapNodeFaultToOutcome(
						response.getFault().getFaultCode(),
						response.getFault().getOriginalFaultCode()
				));
        verifyResponse.setFault(this.basePaymentService.setFaultDetails(response.getFault()));
        log.error("Node verifyPaymentNotice responded with fault [{}] and fault code [{}]",
                response.getFault().getFaultString(), response.getFault().getFaultCode());
		return verifyResponse;
	}

}
