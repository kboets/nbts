package boets.be.nbts.leagues.web;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.common.RapidApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class LeagueClientService extends RapidApiClient {

    public LeagueClientService(RestClient.Builder restClientBuilder,
                               @Value("${nbts.rapidApi.key}") String apiKey,
                               AdminService adminService) {
        super(restClientBuilder, apiKey, adminService);
    }

    public List<League> getLeaguesByCountry(String countryCode) {
        LeagueApiResponse apiResponse = get("/v3/leagues", LeagueApiResponse.class, Map.of(
                "code", countryCode
        ));
        return apiResponse.response()
                .stream()
                .filter(item -> "League".equalsIgnoreCase(item.league().type()))
                .filter(item -> {
                    List<String> leagueNameParts = Arrays.asList(item.league().name().split(" "));
                    return !leagueNameParts.contains("Women") && !leagueNameParts.contains("Cup");
                })
                // only first 3 leagues should be given
                .limit(3)
                .flatMap(item -> item.seasons()
                        .stream()
                        .filter(LeagueApiResponse.LeagueApiItem.LeagueApiLeague.LeagueApiCountry.LeagueApiSeason::current)
                        // only league of type League allowed
                        .filter(apiSeason -> apiSeason.end().isAfter(LocalDate.now()))
                        .map(apiSeason -> mapToLeague(item, apiSeason)))
                .toList();
    }
    public List<League> getLeaguesByCountryAndSeason(String countryCode, int season) {
        LeagueApiResponse apiResponse = get("/v3/leagues", LeagueApiResponse.class, Map.of(
                "code", countryCode,
                "season", season
        ));

        return apiResponse.response()
                .stream()
                .filter(item -> "League".equalsIgnoreCase(item.league().type()))
                .limit(3)
                .flatMap(item -> item.seasons()
                        .stream()
                        .map(apiSeason -> mapToLeague(item, apiSeason)))
                .toList();
    }

    private League mapToLeague(LeagueApiResponse.LeagueApiItem item,
                               LeagueApiResponse.LeagueApiItem.LeagueApiLeague.LeagueApiCountry.LeagueApiSeason apiSeason) {
        return new League(
                item.league().id(),
                item.league().name(),
                item.league().logo(),
                item.country().code(),
                apiSeason.year(),
                apiSeason.start(),
                apiSeason.end(),
                apiSeason.current()
        );
    }

}
