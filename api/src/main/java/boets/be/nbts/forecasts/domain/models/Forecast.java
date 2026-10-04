package boets.be.nbts.forecasts.domain.models;

import boets.be.nbts.results.domain.models.Result;

public record Forecast(Result nextResult, String team, String opponent, int totalPoints) {
}
