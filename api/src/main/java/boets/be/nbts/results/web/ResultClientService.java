package boets.be.nbts.results.web;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.common.RapidApiClient;
import boets.be.nbts.results.domain.models.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ResultClientService extends RapidApiClient {

    public ResultClientService(RestClient.Builder restClientBuilder,
                               @Value("${nbts.rapidApi.key}") String apiKey, AdminService adminService) {
        super(restClientBuilder, apiKey, adminService);
    }

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

    private List<Result> markCurrentRound(List<Result> results) {
        List<Result> finishedResults = results.stream()
                .filter(result -> "FT".equalsIgnoreCase(result.matchStatus()))
                .toList();
        Result lastResult = finishedResults.isEmpty() ? results.getFirst() : finishedResults.getLast();
        int currentRound = lastResult.round();
        return results.stream()
                .map(result -> result.withCurrentRound(result.round() == currentRound))
                .toList();

    }

    private Result mapToResult(ResultApiResponse.ResultApiItem item) {
        return new Result(
                item.fixture().id(),
                item.league().name(),
                item.teams().home().name(),
                item.teams().away().name(),
                item.goals().home(),
                item.goals().away(),
                item.fixture().date().toLocalDate().toString(),
                item.fixture().status().shortStatus(),
                Boolean.TRUE.equals(item.teams().home().winner()),
                Boolean.TRUE.equals(item.teams().away().winner()),
                extractRound(item.league().round()),
                false
        );
    }

    private int extractRound(String round) {
        return Integer.parseInt(round.substring(round.lastIndexOf('-') + 1).trim());
    }
}
