package boets.be.nbts.results.domain;

import boets.be.nbts.results.ResultForecastApi;
import boets.be.nbts.results.domain.models.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class ResultForecastApiImpl implements ResultForecastApi {

    private final ResultService resultService;

    @Override
    public List<Result> getResultsForForecasting(int leagueId, int season) {
        return resultService.getResultsByLeagueAndSeason(leagueId, season);
    }
}
