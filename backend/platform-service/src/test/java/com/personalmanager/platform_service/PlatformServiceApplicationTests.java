package com.personalmanager.platform_service;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootTest
@ContextConfiguration(
		initializers = DatasourceDebugInitializer.class
)
class PlatformServiceApplicationTests {


	@Test
	void contextLoads() {
	}



}
