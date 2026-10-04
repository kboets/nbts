package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.domain.models.Standing;

import java.util.List;

public record CalculationContext(int totalTeams, String team, String opponent, int teamScore, int opponentScore, Result nextMatch, List<Result> previousResults, List<Standing> standings) {
}
