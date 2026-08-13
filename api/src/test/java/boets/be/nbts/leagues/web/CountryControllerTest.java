package boets.be.nbts.leagues.web;

import boets.be.nbts.leagues.domain.CountryService;
import boets.be.nbts.leagues.domain.LeagueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(CountryController.class)
@AutoConfigureRestTestClient
class CountryControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private CountryService countryService;

    @MockitoBean
    private LeagueService leagueService;

    @Test
    @DisplayName("GET /api/countries - should return all countries")
    void getCountries() {
        var countries = List.of(
                new Country(1, "BE", "België", "Belgium", "http://flag.be", "Europe"),
                new Country(2, "NL", "Nederland", "Netherlands", "http://flag.nl", "Europe")
        );
        when(countryService.getCountries()).thenReturn(countries);

        restTestClient.get().uri("/api/countries").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].countryId").isEqualTo(1)
                .jsonPath("$[0].countryCode").isEqualTo("BE")
                .jsonPath("$[0].nameNL").isEqualTo("België")
                .jsonPath("$[0].nameEN").isEqualTo("Belgium")
                .jsonPath("$[1].countryId").isEqualTo(2)
                .jsonPath("$[1].countryCode").isEqualTo("NL")
                .jsonPath("$[1].nameNL").isEqualTo("Nederland")
                .jsonPath("$[1].nameEN").isEqualTo("Netherlands");
    }

    @Test
    @DisplayName("GET /api/countries/selected - should return only selected countries")
    void getSelectedCountries() {
        var countries = List.of(
                new Country(1, "BE", "België", "Belgium", "http://flag.be", "Europe"),
                new Country(2, "NL", "Nederland", "Netherlands", "http://flag.nl", "Europe"),
                new Country(3, "FR", "Frankrijk", "France", "http://flag.fr", "Europe")
        );
        var selectedCountryCodes = List.of("BE", "FR");

        when(countryService.getCountries()).thenReturn(countries);
        when(leagueService.getSelectedLeaguesCountryCodes()).thenReturn(selectedCountryCodes);

        restTestClient.get().uri("/api/countries/selected").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].countryCode").isEqualTo("BE")
                .jsonPath("$[1].countryCode").isEqualTo("FR");
    }

    @Test
    @DisplayName("GET /api/countries/selected - should return empty list when no countries are selected")
    void getSelectedCountries_empty() {
        var countries = List.of(
                new Country(1, "BE", "België", "Belgium", "http://flag.be", "Europe")
        );
        var selectedCountryCodes = List.<String>of();

        when(countryService.getCountries()).thenReturn(countries);
        when(leagueService.getSelectedLeaguesCountryCodes()).thenReturn(selectedCountryCodes);

        restTestClient.get().uri("/api/countries/selected").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }
}
