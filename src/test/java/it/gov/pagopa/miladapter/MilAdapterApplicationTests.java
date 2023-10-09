package it.gov.pagopa.miladapter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@EnableConfigurationProperties
@SpringBootTest
@ActiveProfiles("test")
class MilAdapterApplicationTests {

	@Test
	void contextLoads() {
	}

}
