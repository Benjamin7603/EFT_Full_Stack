package com.duoc.ms_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Esto le dice que use el archivo application-test.yml
class MsGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}