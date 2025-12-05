package it.gov.pagopa.miladapter.services.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigInteger;
import lombok.Getter;
import lombok.Setter;

/**
 * Response of the verifyPaymentNotice API
 */
@Setter
@Getter
public class VerifyPaymentNoticeResponse {

    @NotNull
	@Pattern(regexp = "^(?:OK|NOTICE_GLITCH|WRONG_NOTICE_DATA|CREDITOR_PROBLEMS|PAYMENT_ALREADY_IN_PROGRESS|EXPIRED_NOTICE|REVOKED_NOTICE|NOTICE_ALREADY_PAID|UNEXPECTED_ERROR)$")
	private String outcome;

    @Min(1)
	@Max(99999999999L)
	@JsonInclude(Include.NON_NULL)
	private BigInteger amount;

    // string($date) 2022-11-30
	@Size(min =10, max = 10)
	@JsonInclude(Include.NON_NULL)
	private String dueDate;

    @Pattern(regexp = "^[ -~]{1,210}$")
	@JsonInclude(Include.NON_NULL)
	private String note;

    @Pattern(regexp = "^[ -~]{1,140}$")
	@JsonInclude(Include.NON_NULL)
	private String description;

    @Pattern(regexp = "^[ -~]{1,140}$")
	@JsonInclude(Include.NON_NULL)
	private String company;

    @Pattern(regexp = "^[ -~]{1,140}$")
	@JsonInclude(Include.NON_NULL)
	private String office;

    @Pattern(regexp = "^\\d{11}$")
	@JsonInclude(Include.NON_NULL)
	private String paTaxCode;

    @Pattern(regexp = "^\\d{18}$")
	@JsonInclude(Include.NON_NULL)
	private String noticeNumber;

    @Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("VerifyPaymentNoticeResponse{");
		sb.append("outcome='").append(outcome).append('\'');
		sb.append(", amount=").append(amount);
		sb.append(", dueDate='").append(dueDate).append('\'');
		sb.append(", note='").append(note).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append(", company='").append(company).append('\'');
		sb.append(", office='").append(office).append('\'');
		sb.append(", paTaxCode='").append(paTaxCode).append('\'');
		sb.append(", noticeNumber='").append(noticeNumber).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
