package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This rule calculates points based on the rank of the opponent team.
 * If the team wins or draws, the points awared are equal to the amount of teams minus the rank of the opponent team.
 * eg if there are 20 teams and the opponent is ranked 5th, the team will get 15 points.
 *
 * If the team loses, the points deducted are equal to the rank of the opponent team.
 * eg if there are 20 teams and the opponent is ranked 5th, the team will lose 5 points.
 */
@Component
@Order(2)
public class OpponentRankRule implements PointsCalculationRule {

    @Override
    public int calculatePoints(CalculationContext context) {
        int points = 0;
        int totalTeams = context.totalTeams();
        for (Result match : context.previousResults()) {
            int opponentRank = getOpponentRank(context);
            if (isHomeMatch(match, context.team())) {
                if (match.homeTeamHasWon() || !match.homeTeamHasLost()) {
                    points += (totalTeams - opponentRank);
                } else {
                    points -= opponentRank;
                }
            } else {
                if (match.homeTeamHasLost() || !match.homeTeamHasWon()) {
                    points += (totalTeams - opponentRank);
                } else {
                    points -= opponentRank;
                }
            }
        }
        return points;
    }

    private int getOpponentRank(CalculationContext context) {
        String team = context.opponent();
        for (var standing : context.standings()) {
            if (standing.teamName().equals(team)) {
                return standing.rank();
            }
        }
        return 0;
    }
}
