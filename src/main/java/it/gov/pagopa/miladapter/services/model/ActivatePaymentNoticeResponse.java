package it.gov.pagopa.miladapter.services.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigInteger;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Response of the activatePaymentNotice API.
 * Contains the details of the payment notice as returned by the node
 */
@Setter
@Getter
@ToString
public class ActivatePaymentNoticeResponse {

	/**
	 * Outcome of the operation
	 */
	@NotNull
	@Pattern(regexp = "^(?:OK|NOTICE_GLITCH|WRONG_NOTICE_DATA|CREDITOR_PROBLEMS|PAYMENT_ALREADY_IN_PROGRESS|EXPIRED_NOTICE|REVOKED_NOTICE|NOTICE_ALREADY_PAID|UNEXPECTED_ERROR)$")
	private String outcome;

	/**
	 * Amount in euro cents
	 */
	@Min(1)
	@Max(99999999999L)
	@JsonInclude(Include.NON_NULL)
	private BigInteger amount;

    /**
     * Description of the payment
     */
    @JsonInclude(Include.NON_NULL)
    private String description;

	/**
	 * Tax code of the creditor company
	 */
	@Pattern(regexp = "^\\d{11}$")
	@JsonInclude(Include.NON_NULL)
	private String paTaxCode;

    /**
     * Name of the creditor company
     */
    @JsonInclude(Include.NON_NULL)
    private String company;

    /**
     * Name of the office
     */
    @JsonInclude(Include.NON_NULL)
    private String officeName;

	/**
	 * ID of the payment activation
	 */
	@Pattern(regexp = "^[ -~]{1,35}$")
	@JsonInclude(Include.NON_NULL)
	private String paymentToken;

	/**
	 * List of transfers
	 */
	@JsonInclude(Include.NON_NULL)
	private List<Transfer> transfers;

    /**
     * Creditor reference ID
     */
    @JsonInclude(Include.NON_NULL)
    private String creditorReferenceId;

    /**
     * Fault details in case of outcome KO
     */
    @JsonInclude(Include.NON_NULL)
    private Fault fault;
}
