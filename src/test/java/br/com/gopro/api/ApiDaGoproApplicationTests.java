package br.com.gopro.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.documents.s3.bucket=false",
		"aws.region=us-east-1"
})
class ApiDaGoproApplicationTests {

	@Test
	void contextLoads() {
	}

}
