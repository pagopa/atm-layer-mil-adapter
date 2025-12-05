package it.gov.pagopa.miladapter.services.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Transfer essential data
 */
@Setter
@Getter
public class Transfer {

	/**
	 * Tax code of the creditor company
	 */
	@NotNull
	@Pattern(regexp = "^\\d{11}$")
	private String paTaxCode;

	/**
	 * Transfer category
	 */
	@NotNull
	@Pattern(regexp = "^[ -~]{0,1024}$")
	private String category;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Transfer{");
		sb.append("paTaxCode='").append(paTaxCode).append('\'');
		sb.append(", category='").append(category).append('\'');
		sb.append('}');
		return sb.toString();
	}

}
