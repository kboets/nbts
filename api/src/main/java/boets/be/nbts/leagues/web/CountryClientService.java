package boets.be.nbts.leagues.web;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Retrieves country data from the external API.
 */
@Service
public class CountryClientService {

    private final RestClient restClient;

    public CountryClientService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://restcountries.com/v3.1")
                .build();
    }

    public List<CountryResponse> getCountriesByRegion(String region) {
        return restClient.get()
                .uri("/region/{region}?fields=name,flags,translations,cca2", region)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

}
