package boets.be.nbts.leagues.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Retrieves country data from the external API.
 */
// TODO the contract has changed, the response should be updated when using again.
@Service
public class CountryClientService {

    private final RestClient restClient;

    public CountryClientService(RestClient.Builder restClientBuilder, @Value("${nbts.restCountries.key}") String apiKey) {
        String baseUrl = "https://api.restcountries.com/countries/v5";
        String auth = "Bearer " + apiKey;
        this.restClient = restClientBuilder.baseUrl(baseUrl)
                .defaultHeader("Authorization", auth)
                .requestInterceptor((request, body, execution) -> {
                    System.out.println(">>> HTTP REST CLIENT CALL TO URI: " + request.getURI());
                    System.out.println(">>> Headers Sent: " + request.getHeaders());
                    return execution.execute(request, body);
                })
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
