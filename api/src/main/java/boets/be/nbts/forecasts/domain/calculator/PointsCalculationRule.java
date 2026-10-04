package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;

public interface PointsCalculationRule {

    int calculatePoints(CalculationContext context);

    default boolean isHomeMatch(Result result, String team) {
        return result.homeTeam().equals(team);
    }
}
