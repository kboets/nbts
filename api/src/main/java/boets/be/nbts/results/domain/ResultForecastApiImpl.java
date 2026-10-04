package boets.be.nbts.results.domain;

import boets.be.nbts.results.ResultForecastApi;
import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.domain.models.Standing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class ResultForecastApiImpl implements ResultForecastApi {

    private final ResultService resultService;
    private final StandingService standingService;

    @Override
    public List<Result> getResultsForForecasting(int leagueId, int season) {
        return resultService.getResultsByLeagueAndSeason(leagueId, season);
    }

    @Override
    public List<Standing> getStandingsForForecasting(int leagueId, int season) {
        return standingService.getStandingsByLeagueAndSeason(leagueId, season);
    }
}
