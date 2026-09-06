package boets.be.nbts.results.web;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.common.RapidApiClient;
import boets.be.nbts.results.domain.models.Standing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class StandingClientService extends RapidApiClient {

    public StandingClientService(RestClient.Builder restClientBuilder,
                                 @Value("${nbts.rapidApi.key}") String apiKey, AdminService adminService) {
        super(restClientBuilder, apiKey, adminService);
    }

    public List<Standing> getStandingsByLeagueAndSeason(int leagueId, int season) {
        StandingApiResponse apiResponse = get("/v3/standings", StandingApiResponse.class, Map.of(
                "league", leagueId,
                "season", season
        ));

        return apiResponse.response()
                .stream()
                .flatMap(responseItem -> responseItem.league()
                        .standings()
                        .stream()
                        .flatMap(List::stream)
                        .map(standingEntry -> mapToStanding(responseItem.league(), standingEntry)))
                .toList();
    }

    private Standing mapToStanding(StandingApiResponse.League league, StandingApiResponse.StandingEntry standingEntry) {
        return new Standing(
                league.id(),
                league.season(),
                standingEntry.rank(),
                standingEntry.team().name(),
                standingEntry.team().id(),
                standingEntry.points(),
                standingEntry.all().played(),
                standingEntry.all().win(),
                standingEntry.all().draw(),
                standingEntry.all().lose(),
                standingEntry.update().toLocalDate()
        );
    }

}
