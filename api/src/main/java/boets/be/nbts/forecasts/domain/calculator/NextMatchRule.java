package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This rule calculates points based on the next match of the team.
 * If the next match is a home match, it awards 10 points.
 * If the next match is an away match, it deducts 10 points.
 */
@Component
@Order(4)
public class NextMatchRule implements PointsCalculationRule {

    private static final int HOME_MATCH_POINTS = 10;
    private static final int AWAY_MATCH_POINTS = -10;


    @Override
    public int calculatePoints(CalculationContext context) {
        Result nextMatch = context.nextMatch();
        if (isHomeMatch(nextMatch, context.team())) {
            return HOME_MATCH_POINTS;
        } else if (nextMatch.awayTeam().equals(context.team())) {
            return AWAY_MATCH_POINTS;
        }
        return 0;
    }
}
