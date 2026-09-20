package boets.be.nbts.results;

import boets.be.nbts.results.domain.models.Result;

import java.util.List;

public interface ResultForecastApi {

    List<Result> getResultsForForecasting(int leagueId, int season);
}
