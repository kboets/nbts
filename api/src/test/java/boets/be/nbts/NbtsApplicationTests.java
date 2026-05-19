package boets.be.nbts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NbtsApplicationTests {

	@Container
	@ServiceConnection(name = "postgres")
	private static final SharedPostgresContainer POSTGRES_CONTAINER = SharedPostgresContainer.getInstance();

	@Test
	void contextLoads() {
	}

}
