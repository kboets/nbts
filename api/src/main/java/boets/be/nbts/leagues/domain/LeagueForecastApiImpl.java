package boets.be.nbts.leagues.domain;

import boets.be.nbts.leagues.LeagueForecastApi;
import boets.be.nbts.leagues.domain.models.ForecastLeague;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class LeagueForecastApiImpl implements LeagueForecastApi {

    private final LeagueService leagueService;

    @Override
    public List<ForecastLeague> getSelectedLeaguesForForecasting() {
        return leagueService.getSelectedLeagues()
                .stream()
                .map(league -> new ForecastLeague(
                            league.leagueId(),
                            league.name(),
                            league.season()
                    ))
                .toList();
        }
}
