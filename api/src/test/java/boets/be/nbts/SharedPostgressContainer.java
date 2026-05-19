package boets.be.nbts;

import org.flywaydb.core.Flyway;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class SharedPostgressContainer extends PostgreSQLContainer {

    private static final DockerImageName IMAGE_NAME = DockerImageName.parse("postgres:18.0-alpine");

    private static volatile SharedPostgressContainer sharedPostgressContainer;

    public SharedPostgressContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        this.withReuse(true);
    }

    public static SharedPostgressContainer getInstance() {
        if (sharedPostgressContainer == null) {
            synchronized (SharedPostgressContainer.class) {
                sharedPostgressContainer = new SharedPostgressContainer(IMAGE_NAME);
                sharedPostgressContainer.start();
                Flyway flyway = Flyway.configure()
                        .dataSource(sharedPostgressContainer.getJdbcUrl(), sharedPostgressContainer.getUsername(), sharedPostgressContainer.getPassword())
                        .load();
                flyway.migrate();
            }
        }
        return sharedPostgressContainer;
    }


}
