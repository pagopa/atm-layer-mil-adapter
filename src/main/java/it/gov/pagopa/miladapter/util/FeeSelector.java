package it.gov.pagopa.miladapter.util;

import it.gov.pagopa.miladapter.client.model.BundleOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

public class FeeSelector {

	private static final Logger LOG = Logger.getLogger(FeeSelector.class);

	private FeeSelector() {
	}

	/**
	 * Selects the first fee returned by GEC
	 *
	 * @param bundleOptions the bundle options returned by the getFees API on GEC
	 * @return the fee value
	 * @throws NoSuchElementException if the list is empty
	 */
	public static long getFirstFee(List<BundleOption> bundleOptions) throws NoSuchElementException {

		Optional<BundleOption> bundleOptionOpt = bundleOptions.stream().findFirst();

		return bundleOptionOpt.orElseThrow().getTaxPayerFee();
	}

	/**
	 * Selects the correct fee to return to the client based on the payment method and touchpoint passed
	 * to GEC
	 *
	 * @param bundleOptions    the bundle options returned by the getFees API on GEC
	 * @param gecPaymentMethod the payment method passed in request to GEC
	 * @param gecTouchpoint    the touchpoint passed in request to GEC
	 * @return the fee value
	 * @throws NoSuchElementException if the list is empty
	 */
	public static long getFeeByPaymentMethodAndTouchpoint(List<BundleOption> bundleOptions, String gecPaymentMethod, String gecTouchpoint) {

		LOG.debugf("Choose fee to return to the client ");
		Optional<BundleOption> bundleOptionOpt = bundleOptions.stream().filter(fee -> {
			if (fee.getPaymentMethod() != null && gecPaymentMethod != null) {
				return StringUtils.equals(fee.getPaymentMethod(), gecPaymentMethod) && StringUtils.equals(fee.getTouchpoint(), gecTouchpoint);
			}
			return StringUtils.equals(fee.getTouchpoint(), gecTouchpoint);
		}).findFirst();

		if (bundleOptionOpt.isPresent()) {
			return bundleOptionOpt.get().getTaxPayerFee();
		} else {
			// search for a default
			bundleOptionOpt = bundleOptions.stream().filter(fee -> {
				if (fee.getPaymentMethod() != null) {
					return (StringUtils.equals(fee.getPaymentMethod(), "ANY") && StringUtils.equals(fee.getTouchpoint(), "ANY"));
				}
				return StringUtils.equals(fee.getTouchpoint(), "ANY");
			}).findFirst();

			return bundleOptionOpt.orElseThrow().getTaxPayerFee();

		}

	}

}
