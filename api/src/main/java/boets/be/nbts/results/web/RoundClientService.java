package boets.be.nbts.results.web;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.common.RapidApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Profile("!test")
@Slf4j
public class RoundClientService extends RapidApiClient implements RoundClient {


    protected RoundClientService(RestClient.Builder restClientBuilder, @Value("${nbts.rapidApi.key}") String apiKey,
                                 AdminService adminService) {
        super(restClientBuilder, apiKey, adminService);
    }

    @Override
    public int getRoundByLeagueAndSeason(int leagueId, int season) {
        // Implementation here
        RoundApiResponse response = get("/v3/fixtures/rounds", RoundApiResponse.class, Map.of(
                "league", leagueId,
                "season", season,
                "current", true
        ));

        if (response.response() != null && !response.response().isEmpty()) {
            String roundString = response.response().getFirst();
            return Integer.parseInt(roundString.substring(roundString.lastIndexOf('-') + 1).trim());
        } else {
            log.error("No current round found for league {} and season {}", leagueId, season);
            return 0;
        }

    }
}
