package it.gov.pagopa.miladapter.services.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.miladapter.util.ErrorCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Request of the activatePaymentNotice API
 */
@Getter
@Setter
public class ActivatePaymentNoticeRequest {

	/**
	 * Idempotency key for activate request
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_IDEMPOTENCY_KEY_MUST_NOT_BE_NULL + "] idempotencyKey must not be null")
	@Pattern(regexp = "^\\d{11}_[a-zA-Z0-9]{10}$", message = "[" + ErrorCode.ERROR_IDEMPOTENCY_KEY_MUST_MATCH_REGEXP + "] idempotencyKey must match \"{regexp}\"")
    @JsonProperty("idempotencyKey")
	private String idempotencyKey;

	/**
	 * Amount of the payment notice in euro cents
	 */
	@NotNull(message = "[" + ErrorCode.ERROR_AMOUNT_MUST_NOT_BE_NULL + "] amount must not be null")
	@Min(value = 1L, message = "[" + ErrorCode.ERROR_AMOUNT_MUST_BE_GREATER_THAN + "] amount must be greater than {value}")
	@Max(value = 99999999999L, message = "[" + ErrorCode.ERROR_AMOUNT_MUST_BE_LESS_THAN + "] amount must less than {value}")
    @JsonProperty("amount")
	private Long amount;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ActivatePaymentNoticeRequest{");
		sb.append("idempotencyKey='").append(idempotencyKey).append('\'');
		sb.append(", amount=").append(amount);
		sb.append('}');
		return sb.toString();
	}
}
