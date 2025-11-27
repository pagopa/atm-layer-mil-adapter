/**
 * 
 */
package it.gov.pagopa.miladapter.client.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Psp {
	/**
	 * Identifier of the PSP
	 */
	private String idPsp;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Psp [idPsp=");
		builder.append(idPsp);
		builder.append("]");
		return builder.toString();
	}

}
