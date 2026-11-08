package boets.be.nbts.results.web;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.common.RapidApiClient;
import boets.be.nbts.results.domain.models.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Profile("!test")
public class ResultClientService extends RapidApiClient implements ResultClient {

    public ResultClientService(RestClient.Builder restClientBuilder,
                               @Value("${nbts.rapidApi.key}") String apiKey, AdminService adminService) {
        super(restClientBuilder, apiKey, adminService);
    }

    @Override
    public List<Result> getResultsByLeagueAndSeason(int leagueId, int season) {
        ResultApiResponse apiResponse = get("/v3/fixtures", ResultApiResponse.class, Map.of(
                "league", leagueId,
                "season", season
        ));

        var results = apiResponse.response()
                .stream()
                .map(this::mapToResult)
                .toList();

        return markCurrentRound(results);
    }
}
