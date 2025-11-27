package it.gov.pagopa.miladapter.client.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Details of a transfer
 */
@Getter
@Setter
public class GecTransfer {
	private String creditorInstitution;
	private Boolean digitalStamp;
	private String transferCategory;

	@Override
	public String toString() {
		return new StringBuilder("GecTransfer [creditorInstitution=")
			.append(creditorInstitution)
			.append(", digitalStamp=")
			.append(digitalStamp)
			.append(", transferCategory=")
			.append(transferCategory)
			.append("]")
			.toString();
	}
}
