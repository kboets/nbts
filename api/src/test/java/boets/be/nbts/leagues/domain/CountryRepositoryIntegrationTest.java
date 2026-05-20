package boets.be.nbts.leagues.domain;

import boets.be.nbts.CleanFlywayTestConfiguration;
import boets.be.nbts.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJdbcTest
@Sql("/db/testdata/insert_test_data.sql")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        CleanFlywayTestConfiguration.class,
        TestcontainersConfiguration.class
})
@Testcontainers
class CountryRepositoryIntegrationTest {

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void givenSpecificExistingRegion_shouldReturnCorrectCountries() {
        var countries = countryRepository.findByRegion("Europe");
        assertEquals(2, countries.size());
    }

    @Test
    void givenSpecificNonExistingRegion_shouldNotReturnCountries() {
        var countries = countryRepository.findByRegion("Asia");
        assertTrue(countries.isEmpty());
    }

}
