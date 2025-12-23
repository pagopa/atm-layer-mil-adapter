package it.gov.pagopa.miladapter.services.model;

import it.gov.pagopa.miladapter.util.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request of the closePayment API.
 * Contains the details of the e-money payment and its outcome
 */
@Getter
@Setter
@ToString
public class ClosePaymentRequest {

	/**
	 * Outcome of the e-payment transaction
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_OUTCOME_MUST_NOT_BE_NULL + "] outcome must not be null")
	@Pattern(regexp = "CLOSE|ERROR_ON_PAYMENT", message = "[" + ErrorCode.ERROR_OUTCOME_MUST_MATCH_MATCH_REGEXP + "] outcome must match \"{regexp}\"")
	private String outcome;


	/**
	 * Method used to pay notice/s
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_METHOD_MUST_NOT_BE_NULL + "] paymentMethod must not be null")
	@Pattern(regexp = "PAGOBANCOMAT|DEBIT_CARD|CREDIT_CARD|PAYMENT_CARD|BANK_ACCOUNT|CASH",
			message = "[" + ErrorCode.ERROR_PAYMENT_METHOD_MUST_MATCH_REGEXP + "] paymentMethod must match \"{regexp}\"")
	private String paymentMethod;


	/**
	 * Timestamp of e-money transaction
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_TIMESTAMP_MUST_NOT_BE_NULL + "] paymentTimestamp must not be null")
	@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T(2[0-3]|[01]\\d):[0-5]\\d:[0-5]\\d",
			message = "[" + ErrorCode.ERROR_PAYMENT_TIMESTAMP_MUST_MATCH_REGEXP + "] paymentTimestamp must match \"{regexp}\"")
	private String paymentTimestamp;

    /**
     * IDs of the payment activations
     */
    @NotNull(message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_NOT_BE_NULL + "] paymentTokens must not be null")
    @Size(max = 5, message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_LIST_MUST_HAVE_AT_MOST + "] paymentTokens must have at most {max} elements")
    private List<@Pattern(regexp = "^[ -~]{1,35}$", message = "[" + ErrorCode.ERROR_PAYMENT_TOKEN_MATCH_MATCH_REGEXP + "] paymentTokens element must match \"{regexp}\"") String> paymentTokens;
}
