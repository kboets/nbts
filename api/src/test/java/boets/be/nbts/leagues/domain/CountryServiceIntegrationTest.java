package boets.be.nbts.leagues.domain;

import boets.be.nbts.CleanFlywayTestConfiguration;
import boets.be.nbts.TestcontainersConfiguration;
import boets.be.nbts.leagues.web.Country;
import boets.be.nbts.leagues.web.CountryClientService;
import boets.be.nbts.leagues.web.CountryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TestcontainersConfiguration.class,
        CleanFlywayTestConfiguration.class,
        CountryService.class
})
@Testcontainers
class CountryServiceIntegrationTest {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private CountryService countryService;
    @MockitoBean
    private CountryClientService countryClientService;

    @Test
    public void loadCountries_givenEmptyDataBase_shouldGetCountriesFromExternalApi() {
        CountryResponse responseBE = CountryResponse.CountryResponseBuilder.aCountryResponse()
                .withCca2("BE")
                .withFlags(new CountryResponse.Flags("https://www.be.png", "https://www.be.svg", "Belgium flag"))
                .withName(new CountryResponse.Name("Belgium", "Belgium"))
                .withTranslations(new CountryResponse.Translations(new CountryResponse.Translation("België", "Koninkrijk Belgie")))
                .build();
        when(countryClientService.getCountriesByRegion("Europe"))
                .thenReturn(List.of(responseBE));

        // verify on start, no data in database
        assertEquals(0, countryRepository.findByRegion("Europe").size());
        // load countries
        countryService.loadCountries();
        // verify data in database
        assertFalse(countryRepository.findByRegion("Europe").isEmpty());
    }

    @Test
    @Sql("/db/testdata/insert_test_data.sql")
    public void loadCountries_givenNonEmptyDataBase_shouldDoNothing() {
        // verify on start, no data in database
        assertEquals(2, countryRepository.findByRegion("Europe").size());
        // load countries
        countryService.loadCountries();
        // verify data in database, no new countries
        assertEquals(2, countryRepository.findByRegion("Europe").size());
    }

    @Test
    @Sql("/db/testdata/insert_test_data.sql")
    public void getCountries_shouldReturnAllCountries() {
        List<Country> countries = countryService.getCountries();
        assertEquals(2, countries.size());
    }

}
