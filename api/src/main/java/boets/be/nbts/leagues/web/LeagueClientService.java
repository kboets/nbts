package boets.be.nbts.leagues.web;

import boets.be.nbts.common.RapidApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class LeagueClientService extends RapidApiClient {

    public LeagueClientService(RestClient.Builder restClientBuilder,
                               @Value("${nbts.rapidApi.key}") String apiKey) {
        super(restClientBuilder, apiKey);
    }

    public List<League> getLeaguesByCountryAndSeason(String countryCode, int season) {
        LeagueApiResponse apiResponse = get("/v3/leagues", LeagueApiResponse.class, Map.of(
                "code", countryCode,
                "season", season
        ));

        return apiResponse.response()
                .stream()
                .flatMap(item -> item.seasons()
                        .stream()
                        .map(apiSeason -> new League(
                                item.league().id(),
                                item.league().name(),
                                item.league().logo(),
                                item.country().code(),
                                apiSeason.year(),
                                apiSeason.start(),
                                apiSeason.end(),
                                apiSeason.current()
                        )))
                .toList();
    }

}
