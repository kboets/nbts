package boets.be.nbts.forecasts.domain.models;

import boets.be.nbts.results.domain.models.Result;

public record Forecast(Result nextResult, int homeTeamPoints, int awayTeamPoints, int totalPoints) {
}
