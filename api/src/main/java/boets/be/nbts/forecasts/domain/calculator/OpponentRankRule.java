package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This rule calculates points based on the rank of the opponent team.
 * If the team wins or draws, the points awarded are equal to the amount of teams minus the rank of the opponent team.
 * ex if there are 20 teams and the opponent is ranked 5th, the team will get 15 points.
 * If the team loses, the points deducted are equal to the rank of the opponent team.
 * ex if there are 20 teams and the opponent is ranked 5th, the team will lose 5 points.
 */
@Component
@Order(2)
public class OpponentRankRule implements PointsCalculationRule {

    @Override
    public CalculationContext calculatePoints(CalculationContext context) {
        int points = 0;
        int totalTeams = context.totalTeams();
        for (Result match : context.previousResults()) {
            int opponentRank = getOpponentRankFromMatch(match, context);
            if (isHomeMatch(match, context.team())) {
                points += calculateHomeRankPoints(match, totalTeams, opponentRank);
            } else {
                points += calculateAwayRankPoints(match, totalTeams, opponentRank);
            }
        }
        return context.withOpponentRankScore(points);
    }

    static int calculateHomeRankPoints(Result match, int totalTeams, int opponentRank) {
        return match.homeTeamHasLost() ? -opponentRank : totalTeams - opponentRank;
    }

    static int calculateAwayRankPoints(Result match, int totalTeams, int opponentRank) {
        return match.homeTeamHasLost() || !match.homeTeamHasWon() ? totalTeams - opponentRank : -opponentRank;
    }

    static int getOpponentRankFromMatch(Result match, CalculationContext context) {
        String opponentTeam = match.awayTeam().equals(context.team()) ? match.homeTeam() : match.awayTeam();
        for (var standing : context.standings()) {
            if (standing.teamName().equals(opponentTeam)) {
                return standing.rank();
            }
        }
        return 0;
    }
}
