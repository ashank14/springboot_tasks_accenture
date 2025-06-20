package spring_tasks.spring_project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"google.api.key=test-key",
		"google.api.base-url=https://mock-api.com"
})
class SpringProjectApplicationTests {

	@Test
	void contextLoads() {
	}

}
