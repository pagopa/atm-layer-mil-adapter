package it.gov.pagopa.miladapter.services.model;

import it.gov.pagopa.miladapter.util.FeeCalculatorErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Request of the getFee API
 */
@Getter
@Setter
public class GetFeeRequest {

	/**
	 * Method used to pay notice/s
	 */
	@Pattern(regexp = "PAGOBANCOMAT|DEBIT_CARD|CREDIT_CARD|PAYMENT_CARD|BANK_ACCOUNT|CASH", message = "[" + FeeCalculatorErrorCode.PAYMENT_METHOD_MUST_MATCH_REGEXP + "] paymentMethod must match one of the values \"{regexp}\"")
	private String paymentMethod;

	/**
	 * Payment notice data
	 */
	@Valid
	@NotNull(message = "[" + FeeCalculatorErrorCode.NOTICES_MUST_NOT_BE_NULL + "] notices must not be null")
	@Size(max = 5, message = "[" + FeeCalculatorErrorCode.NOTICES_LIST_EXCEEDED_SIZE + "] notices must contain at most {max} elements")
	private List<Notice> notices;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetFeeRequest [paymentMethod=");
		builder.append(paymentMethod);
		builder.append(", notices=");
		builder.append(notices);
		builder.append("]");
		return builder.toString();
	}

}
