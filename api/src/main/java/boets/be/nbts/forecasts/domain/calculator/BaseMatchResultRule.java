package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class BaseMatchResultRule implements PointsCalculationRule {

    private static final int HOME_WIN_POINTS = 20;
    private static final int HOME_DRAW_POINTS = 5;
    private static final int HOME_LOSS_POINTS = -10;

    private static final int AWAY_WIN_POINTS = 30;
    private static final int AWAY_DRAW_POINTS = 10;
    private static final int AWAY_LOSS_POINTS = -5;

    @Override
    public int calculatePoints(CalculationContext context) {
        int points = 0;
        String team = context.team();
        for (Result result : context.previousResults()) {
            if (isHomeMatch(result, team)) {
                points += calculateHomeMatchPoints(result);
            } else if (result.awayTeam().equals(team)) {
                points += calculateAwayMatchPoints(result);
            }
        }
        return points;
    }

    private int calculateHomeMatchPoints(Result result) {
        if (result.homeTeamHasWon()) {
            return HOME_WIN_POINTS;
        } else if (result.homeTeamHasLost()) {
            return HOME_LOSS_POINTS;
        } else {
            return HOME_DRAW_POINTS;
        }
    }

    private int calculateAwayMatchPoints(Result result) {
        if (result.homeTeamHasWon()) {
            return AWAY_LOSS_POINTS;
        } else if (result.homeTeamHasLost()) {
            return AWAY_WIN_POINTS;
        } else {
            return AWAY_DRAW_POINTS;
        }
    }
}
