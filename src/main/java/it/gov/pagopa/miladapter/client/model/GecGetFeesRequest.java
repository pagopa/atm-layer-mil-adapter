/**
 * request to the GEC service
 */
package it.gov.pagopa.miladapter.client.model;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * The request to the getFees API exposed by GEC
 */
@Getter
@Setter
public class GecGetFeesRequest {
	private List<Psp> idPspList;
	@NotNull
	private Long paymentAmount;
	@NotNull
	private String primaryCreditorInstitution;
	private String paymentMethod;
	private String touchpoint;
	@NotNull
	private List<GecTransfer> transferList;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GecGetFeesRequest [idPspList=");
		builder.append(idPspList);
		builder.append(", paymentAmount=");
		builder.append(paymentAmount);
		builder.append(", primaryCreditorInstitution=");
		builder.append(primaryCreditorInstitution);
		builder.append(", paymentMethod=");
		builder.append(paymentMethod);
		builder.append(", touchpoint=");
		builder.append(touchpoint);
		builder.append(", transferList=");
		builder.append(transferList);
		builder.append("]");
		return builder.toString();
	}

}
