package it.gov.pagopa.miladapter.services.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Response of the getFee API
 */
@Getter
@Setter
public class GetFeeResponse {

	@NotNull
	@Min(value = 1)
	@Max(value = 99999999999L)
	private Long fee;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetFeeResponse [fee=");
		builder.append(fee);
		builder.append("]");
		return builder.toString();
	}
}
