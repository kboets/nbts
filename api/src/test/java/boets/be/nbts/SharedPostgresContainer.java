package boets.be.nbts;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class SharedPostgresContainer extends PostgreSQLContainer {

    private static final DockerImageName IMAGE_NAME = DockerImageName.parse("postgres:18.0-alpine");

    private static volatile SharedPostgresContainer sharedPostgresContainer;

    public SharedPostgresContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        this.withReuse(false);
    }

    public static SharedPostgresContainer getInstance() {
        if (sharedPostgresContainer == null) {
            synchronized (SharedPostgresContainer.class) {
                sharedPostgresContainer = new SharedPostgresContainer(IMAGE_NAME);
                sharedPostgresContainer.start();
            }
        }
        return sharedPostgresContainer;
    }


}
