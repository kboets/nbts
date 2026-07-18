package boets.be.nbts;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

//	@Bean
//	@ServiceConnection
//	MongoDBContainer mongoDbContainer() {
//		return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
//	}

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return SharedPostgresContainer.getInstance();
    }

}
