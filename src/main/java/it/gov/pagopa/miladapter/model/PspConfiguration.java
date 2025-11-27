package it.gov.pagopa.miladapter.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration of a PSP used when connecting to the node
 */
@Getter
@Setter
public class PspConfiguration {

	/**
	 * Identifier of the PSP, assigned by PagoPA
	 */
	private String psp;

	/**
	 * Identifier of the broker, assigned by PagoPA
	 */
	private String broker;

	/**
	 * Identifier of the channel used for the payment transaction.
	 * Is assigned by PagoPA and is unique for the psp
	 */
	private String channel;

	/**
	 * Channel's password, assigned by PagoPA
	 */
	private String password;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PspConfiguration{");
		sb.append("psp='").append(psp).append('\'');
		sb.append(", broker='").append(broker).append('\'');
		sb.append(", channel='").append(channel).append('\'');
		sb.append(", password='").append(password != null ? "***" : null).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
