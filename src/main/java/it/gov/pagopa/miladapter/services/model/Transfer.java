package it.gov.pagopa.miladapter.services.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Transfer essential data
 */
@Setter
@Getter
@ToString
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

}
