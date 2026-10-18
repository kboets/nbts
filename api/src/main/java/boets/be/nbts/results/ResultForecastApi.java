package boets.be.nbts.results;

import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.domain.models.Standing;

import java.util.List;
import java.util.Optional;

public interface ResultForecastApi {

    List<Result> getResultsForForecasting(int leagueId, int season);

    List<Standing> getStandingsForForecasting(int leagueId, int season);

    Optional<Result> getCurrentRound(int leagueId, int season);
}
