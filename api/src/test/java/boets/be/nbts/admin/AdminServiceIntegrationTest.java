package boets.be.nbts.admin;

import boets.be.nbts.CleanFlywayTestConfiguration;
import boets.be.nbts.TestcontainersConfiguration;
import boets.be.nbts.admin.domain.ApiCounterEntity;
import boets.be.nbts.admin.domain.ApiCounterRepository;
import boets.be.nbts.admin.domain.models.ApiCounter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TestcontainersConfiguration.class,
        CleanFlywayTestConfiguration.class,
        AdminService.class
})
@Testcontainers
class AdminServiceIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ApiCounterRepository apiCounterRepository;

    @Test
    @DisplayName("getApiCounter - when no record exists, should create new record with zero counter")
    void getApiCounter_whenNoRecord_shouldCreateAndReturnZero() {
        LocalDate date = LocalDate.of(2026, 7, 21);
        
        // Ensure no record exists
        ApiCounterEntity existing = apiCounterRepository.findByDate(date);
        assertThat(existing).isNull();

        ApiCounter result = adminService.getApiCounter(date);

        assertThat(result.counter()).isEqualTo(0);

        ApiCounterEntity saved = apiCounterRepository.findByDate(date);
        assertThat(saved).isNotNull();
        assertThat(saved.getCounter()).isEqualTo(0);
        assertThat(saved.getDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("getApiCounter - when record exists, should return existing counter")
    void getApiCounter_whenRecordExists_shouldReturnCounter() {
        LocalDate date = LocalDate.of(2026, 7, 22);
        ApiCounterEntity entity = new ApiCounterEntity();
        entity.setDate(date);
        entity.setCounter(42);
        apiCounterRepository.save(entity);

        ApiCounter result = adminService.getApiCounter(date);

        assertThat(result.counter()).isEqualTo(42);
    }

    @Test
    @DisplayName("saveApiCounter - should update existing record")
    void saveApiCounter_shouldUpdateExisting() {
        LocalDate date = LocalDate.of(2026, 7, 23);
        ApiCounterEntity entity = new ApiCounterEntity();
        entity.setDate(date);
        entity.setCounter(10);
        apiCounterRepository.save(entity);

        adminService.saveApiCounter(date, 25);

        ApiCounterEntity updated = apiCounterRepository.findByDate(date);
        assertThat(updated.getCounter()).isEqualTo(25);
    }

    @Test
    @DisplayName("saveApiCounter - should create new record if not exists")
    void saveApiCounter_shouldCreateIfNew() {
        LocalDate date = LocalDate.of(2026, 7, 24);

        adminService.saveApiCounter(date, 50);

        ApiCounterEntity saved = apiCounterRepository.findByDate(date);
        assertThat(saved).isNotNull();
        assertThat(saved.getCounter()).isEqualTo(50);
        assertThat(saved.getDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("getCurrentApiCounter - should work for current date")
    void getCurrentApiCounter_shouldWork() {
        LocalDate today = LocalDate.now();
        adminService.saveApiCounter(today, 100);

        ApiCounter result = adminService.getCurrentApiCounter();

        assertThat(result.counter()).isEqualTo(100);
    }
}
