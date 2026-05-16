package boets.be.nbts.leagues.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(CountryClientService.class)
class CountryClientServiceTest {

    @Autowired
    private CountryClientService countryClientService;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void getEuropeanCountries_shouldMapCorrectly() {
        String jsonResponse = """
                [
                    {
                        "flags": {
                            "png": "https://flagcdn.com/w320/si.png",
                            "svg": "https://flagcdn.com/si.svg",
                            "alt": "The flag of Slovenia..."
                        },
                        "name": {
                            "common": "Slovenia",
                            "official": "Republic of Slovenia"
                        },
                        "translations": {
                            "nld": {
                                "official": "Republiek Slovenië",
                                "common": "Slovenië"
                            }
                        },
                        "cca2": "SI"
                    },
                    {
                        "flags": {
                            "png": "https://flagcdn.com/w320/se.png",
                            "svg": "https://flagcdn.com/se.svg",
                            "alt": "The flag of Sweden..."
                        },
                        "name": {
                            "common": "Sweden",
                            "official": "Kingdom of Sweden"
                        },
                        "translations": {
                            "nld": {
                                "official": "Koninkrijk Zweden",
                                "common": "Zweden"
                            }
                        },
                        "cca2": "SE"
                    }
                ]
                """;

        this.server.expect(requestTo("https://restcountries.com/v3.1/region/Europe?fields=name,flags,translations,cca2"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<CountryResponse> countries = countryClientService.getEuropeanCountries();

        assertThat(countries).hasSize(2);
        
        CountryResponse slovenia = countries.getFirst();
        assertThat(slovenia.name().common()).isEqualTo("Slovenia");
        assertThat(slovenia.name().official()).isEqualTo("Republic of Slovenia");
        assertThat(slovenia.cca2()).isEqualTo("SI");
        assertThat(slovenia.flags().png()).isEqualTo("https://flagcdn.com/w320/si.png");
        assertThat(slovenia.flags().svg()).isEqualTo("https://flagcdn.com/si.svg");
        assertThat(slovenia.flags().alt()).isEqualTo("The flag of Slovenia...");
        assertThat(slovenia.translations().nld().common()).isEqualTo("Slovenië");
        assertThat(slovenia.translations().nld().official()).isEqualTo("Republiek Slovenië");

        CountryResponse sweden = countries.get(1);
        assertThat(sweden.name().common()).isEqualTo("Sweden");
        assertThat(sweden.name().official()).isEqualTo("Kingdom of Sweden");
        assertThat(sweden.cca2()).isEqualTo("SE");
        assertThat(sweden.flags().png()).isEqualTo("https://flagcdn.com/w320/se.png");
        assertThat(sweden.flags().svg()).isEqualTo("https://flagcdn.com/se.svg");
        assertThat(sweden.flags().alt()).isEqualTo("The flag of Sweden...");
        assertThat(sweden.translations().nld().common()).isEqualTo("Zweden");
        assertThat(sweden.translations().nld().official()).isEqualTo("Koninkrijk Zweden");
    }
}
