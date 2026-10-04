package boets.be.nbts.datacollector.models;

import boets.be.nbts.leagues.domain.models.ForecastLeague;
import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.domain.models.Standing;

import java.util.List;

public record ForecastRawData(ForecastLeague league, List<Result> results, List<Standing> standings) {}
