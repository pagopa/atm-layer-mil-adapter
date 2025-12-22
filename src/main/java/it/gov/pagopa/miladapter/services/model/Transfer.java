package it.gov.pagopa.miladapter.services.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Transfer essential data
 */
@Setter
@Getter
@ToString
public class Transfer {

    /**
     * Transfer identifier
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int idTransfer;

    /**
     * Transfer amount
     */
    @JsonInclude(Include.NON_NULL)
    private BigDecimal transferAmount;

	/**
	 * Tax code of the creditor company
	 */
	@NotNull
	@Pattern(regexp = "^\\d{11}$")
	private String paTaxCode;

    @JsonInclude(Include.NON_NULL)
    private String company;

    /**
     * IBAN of the creditor company
     */
    @JsonInclude(Include.NON_NULL)
    private String iban;

    /**
     * Remittance information
     */
    @JsonInclude(Include.NON_NULL)
    private String remittanceInformation;

	/**
	 * Transfer category
	 */
	@NotNull
	@Pattern(regexp = "^[ -~]{0,1024}$")
	private String category;

}
