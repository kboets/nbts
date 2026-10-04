package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This rule calculates points based on the first two matches of the team.
 * If the team wins the first match, it awards 20 points.
 * If the team wins the second match, it awards 10 points.
 * If the team loses the first match, it deducts 20 points.
 * If the team loses the second match, it deducts 10 points.
 */
@Component
@Order(3)
public class BoosterRule implements PointsCalculationRule {

    private static final int FIRST_BOOSTER_POINTS = 20;
    private static final int SECOND_BOOSTER_POINTS = 10;


    @Override
    public int calculatePoints(CalculationContext context) {
        int points = 0;
        int homeMatches = 0;
        int awayMatches = 0;
        String team = context.team();
        for (var match: context.previousResults()) {
            if(isHomeMatch(match, team)) {
                homeMatches++;
                points += calculateHomeBoosterPoints(match, homeMatches);
            } else {
                awayMatches++;
                points += calculateAwayBoosterPoints(match, awayMatches);
            }
        }
        return points;
    }

    private int calculateHomeBoosterPoints(Result result, int index) {
        if (result.homeTeamHasWon()) {
            if (index == 1) {
                return FIRST_BOOSTER_POINTS;
            } else if (index == 2) {
                return SECOND_BOOSTER_POINTS;
            }
        } else if (result.homeTeamHasLost()) {
            if (index == 1) {
                return -FIRST_BOOSTER_POINTS;
            } else if (index == 2) {
                return -SECOND_BOOSTER_POINTS;
            }
        }
        return 0;
    }

    private int calculateAwayBoosterPoints(Result result, int index) {
        if (result.homeTeamHasLost()) {
            if (index == 1) {
                return FIRST_BOOSTER_POINTS;
            } else if (index == 2) {
                return SECOND_BOOSTER_POINTS;
            }
        } else if (result.homeTeamHasWon()) {
            if (index == 1) {
                return -FIRST_BOOSTER_POINTS;
            } else if (index == 2) {
                return -SECOND_BOOSTER_POINTS;
            }
        }
        return 0;
    }
}
