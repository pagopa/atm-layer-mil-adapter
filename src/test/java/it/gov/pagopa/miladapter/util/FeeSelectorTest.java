package it.gov.pagopa.miladapter.util;


import it.gov.pagopa.miladapter.client.model.BundleOption;
import it.gov.pagopa.miladapter.client.model.GecGetFeesResponse;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FeeSelectorTest {

	GecGetFeesResponse gecGetFeesResponse;

	GecGetFeesResponse gecGetFeesResponseEmpty;

	@BeforeAll
	void createTestObjects() {

		// GEC getFees response
		gecGetFeesResponse = new GecGetFeesResponse();

		gecGetFeesResponse.setBelowThreshold(false);

		BundleOption bundleOption1 = new BundleOption();
		bundleOption1.setTaxPayerFee(50);
		bundleOption1.setPrimaryCiIncurredFee(0);
		bundleOption1.setPaymentMethod("ANY");
		bundleOption1.setTouchpoint("ANY");
		bundleOption1.setIdBundle("b51f2062-def7-4976-a258-6c94f57d402a");
		bundleOption1.setBundleName("soft-cli-test10");
		bundleOption1.setBundleDescription("pacchetto di test per software client");
		bundleOption1.setIdCiBundle(null);
		bundleOption1.setIdPsp("AGID_01");
		bundleOption1.setIdChannel(null);
		bundleOption1.setIdBrokerPsp(null);
		bundleOption1.setOnUs(false);
		bundleOption1.setAbi(null);

		BundleOption bundleOption2 = new BundleOption();
		bundleOption2.setTaxPayerFee(100);
		bundleOption2.setPrimaryCiIncurredFee(0);
		bundleOption2.setPaymentMethod("CP");
		bundleOption2.setTouchpoint("POS");
		bundleOption2.setIdBundle("5afb70a5-b427-49a3-8e2c-4b74625e4fe1");
		bundleOption2.setBundleName("soft-cli-test7");
		bundleOption2.setBundleDescription("pacchetto di test per software client");
		bundleOption2.setIdCiBundle(null);
		bundleOption2.setIdPsp("AGID_01");
		bundleOption2.setIdChannel(null);
		bundleOption2.setIdBrokerPsp(null);
		bundleOption2.setOnUs(false);
		bundleOption2.setAbi(null);

		BundleOption bundleOption3 = new BundleOption();
		bundleOption3.setTaxPayerFee(150);
		bundleOption3.setPrimaryCiIncurredFee(0);
		bundleOption3.setPaymentMethod("ANY");
		bundleOption3.setTouchpoint("ANY");
		bundleOption3.setIdBundle("ca9a56e7-aa02-4d11-a8c8-71e1670f974e");
		bundleOption3.setBundleName("soft-cli-test8");
		bundleOption3.setBundleDescription("pacchetto di test per software client");
		bundleOption3.setIdCiBundle(null);
		bundleOption3.setIdPsp("AGID_01");
		bundleOption3.setIdChannel(null);
		bundleOption3.setIdBrokerPsp(null);
		bundleOption3.setOnUs(false);
		bundleOption3.setAbi(null);

		BundleOption bundleOption4 = new BundleOption();
		bundleOption4.setTaxPayerFee(200);
		bundleOption4.setPrimaryCiIncurredFee(0);
		bundleOption4.setPaymentMethod("ANY");
		bundleOption4.setTouchpoint("ANY");
		bundleOption4.setIdBundle("683589f9-9471-4bd4-ba44-a1044aea4dcc");
		bundleOption4.setBundleName("soft-cli-test9");
		bundleOption4.setBundleDescription("pacchetto di test per software client");
		bundleOption4.setIdCiBundle(null);
		bundleOption4.setIdPsp("AGID_01");
		bundleOption4.setIdChannel(null);
		bundleOption4.setIdBrokerPsp(null);
		bundleOption4.setOnUs(false);
		bundleOption4.setAbi(null);

		gecGetFeesResponse.setBundleOptions(Arrays.asList(bundleOption1, bundleOption2, bundleOption3, bundleOption4));

		// GEC getFees response with empty bundles
		gecGetFeesResponseEmpty = new GecGetFeesResponse();
		gecGetFeesResponseEmpty.setBelowThreshold(false);
		gecGetFeesResponseEmpty.setBundleOptions(List.of());

	}

	@Test
	void testGetFirstFee() {
		long fee = FeeSelector.getFirstFee(gecGetFeesResponse.getBundleOptions());
		Assertions.assertEquals(50, fee);
	}

	@Test
	void testGetFirstFee_emptyResponse() {
		List<BundleOption> bundleOptions = gecGetFeesResponseEmpty.getBundleOptions();
		Assertions.assertThrows(NoSuchElementException.class, () -> FeeSelector.getFirstFee(bundleOptions));
	}

	@Test
	void testGetFeeByPaymentMethodAndTouchpoint() {
		long fee = FeeSelector.getFeeByPaymentMethodAndTouchpoint(gecGetFeesResponse.getBundleOptions(),
			"CP", "POS");
		Assertions.assertEquals(100, fee);
	}

	@Test
	void testGetFeeByPaymentMethodAndTouchpoint_default() {
		long fee = FeeSelector.getFeeByPaymentMethodAndTouchpoint(gecGetFeesResponse.getBundleOptions(),
			"CP", "ATM");
		Assertions.assertEquals(50, fee);
	}

	@Test
	void testGetFeeByPaymentMethodAndTouchpoint_emptyResponse() {
		List<BundleOption> bundleOptions = gecGetFeesResponseEmpty.getBundleOptions();
		Assertions.assertThrows(NoSuchElementException.class,
			() -> FeeSelector.getFeeByPaymentMethodAndTouchpoint(bundleOptions, "CP", "POS"));
	}

	@Test
	void testGetFeeByPaymentMethodAndTouchpoint_paymentNull() {
		long fee = FeeSelector.getFeeByPaymentMethodAndTouchpoint(gecGetFeesResponse.getBundleOptions(),
			null, "ATM");
		Assertions.assertEquals(50, fee);
	}

	@Test
	void testGetFeeByPaymentMethodAndTouchpoint_bundleOptionPaymentNull() {
		GecGetFeesResponse response = new GecGetFeesResponse();

		BundleOption bundleOption = new BundleOption();
		bundleOption.setTaxPayerFee(200);
		bundleOption.setPrimaryCiIncurredFee(0);
		bundleOption.setPaymentMethod(null);
		bundleOption.setTouchpoint("ANY");
		bundleOption.setIdBundle("683589f9-9471-4bd4-ba44-a1044aea4dcc");
		bundleOption.setBundleName("soft-cli-test9");
		bundleOption.setBundleDescription("pacchetto di test per software client");
		bundleOption.setIdCiBundle(null);
		bundleOption.setIdPsp("AGID_01");
		bundleOption.setIdChannel(null);
		bundleOption.setIdBrokerPsp(null);
		bundleOption.setOnUs(false);
		bundleOption.setAbi(null);

		response.setBundleOptions(List.of(bundleOption));

		long fee = FeeSelector.getFeeByPaymentMethodAndTouchpoint(response.getBundleOptions(),
			"CP", "ATM");
		Assertions.assertEquals(200, fee);
	}

	@Test
	void testGetFeeByPaymentMethodAndTouchpoint_touchpointAny() {
		GecGetFeesResponse response = new GecGetFeesResponse();

		BundleOption bundleOption = new BundleOption();
		bundleOption.setTaxPayerFee(200);
		bundleOption.setPrimaryCiIncurredFee(0);
		bundleOption.setPaymentMethod(null);
		bundleOption.setTouchpoint("ANY");
		bundleOption.setIdBundle("683589f9-9471-4bd4-ba44-a1044aea4dcc");
		bundleOption.setBundleName("soft-cli-test9");
		bundleOption.setBundleDescription("pacchetto di test per software client");
		bundleOption.setIdCiBundle(null);
		bundleOption.setIdPsp("AGID_01");
		bundleOption.setIdChannel(null);
		bundleOption.setIdBrokerPsp(null);
		bundleOption.setOnUs(false);
		bundleOption.setAbi(null);

		response.setBundleOptions(List.of(bundleOption));

		long fee = FeeSelector.getFeeByPaymentMethodAndTouchpoint(response.getBundleOptions(),
			null, "ATM");
		Assertions.assertEquals(200, fee);
	}

}
