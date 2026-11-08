package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.domain.models.Standing;

import java.util.List;

public record CalculationContext(int totalTeams, String team, String opponent, int teamScore, int opponentRankScore, Result nextMatch, List<Result> previousResults, List<Standing> standings) {

    public CalculationContext withTeamScore(int teamScore) {
        return new CalculationContext(
                totalTeams,
                team,
                opponent,
                teamScore + this.teamScore,
                opponentRankScore,
                nextMatch,
                previousResults,
                standings
        );
    }

    public CalculationContext withOpponentRankScore(int opponentRankScore) {
        return new CalculationContext(
                totalTeams,
                team,
                opponent,
                teamScore,
                opponentRankScore + this.opponentRankScore,
                nextMatch,
                previousResults,
                standings
        );
    }


}
