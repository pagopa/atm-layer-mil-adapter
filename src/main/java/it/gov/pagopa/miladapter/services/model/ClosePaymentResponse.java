package it.gov.pagopa.miladapter.services.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Response of the closePayment API.
 * Because the closePayment operation is asynchronous on the node, the outcome is just a notification
 * that the node has validated the request and that it will be processed
 */
@Getter
@Setter
@ToString
public class ClosePaymentResponse {

	/**
	 * Outcome of the take in charge of the close payment
	 */
	@NotNull
	@Pattern(regexp = "^(?:OK|KO)$")
	private Outcome outcome;
}
