package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PointsCalculationEngineTest {

    private PointsCalculationEngine engine;
    private BaseMatchResultRule baseMatchResultRule;
    private NextMatchRule nextMatchRule;

    @BeforeEach
    void setUp() {
        baseMatchResultRule = new BaseMatchResultRule();
        nextMatchRule = new NextMatchRule();
        engine = new PointsCalculationEngine(List.of(baseMatchResultRule, nextMatchRule));
    }

    @Test
    void calculateTotalPoints_shouldAccumulatePointsFromAllRules() {
        // BaseMatchResultRule: Home win gives 20 points
        Result previousResult = createResult("TeamA", "TeamB", true, false);
        // NextMatchRule: Home match gives 10 points
        Result nextMatch = createResult("TeamA", "TeamC", false, false);

        CalculationContext context = new CalculationContext(
                18, "TeamA", "TeamC", 0, 0, nextMatch, List.of(previousResult), Collections.emptyList());

        CalculationContext resultContext = engine.calculateTotalPoints(context);

        // Expected: 20 (base) + 10 (next) = 30
        assertThat(resultContext.teamScore()).isEqualTo(30);
    }

    private Result createResult(String homeTeam, String awayTeam, boolean homeTeamWon, boolean homeTeamLost) {
        return new Result(
                1, "League", homeTeam, awayTeam,
                homeTeamWon ? 2 : (homeTeamLost ? 0 : 1),
                homeTeamWon ? 0 : (homeTeamLost ? 2 : 1),
                LocalDate.of(2026, 1, 1), "FT",
                homeTeamWon, homeTeamLost, 1, false
        );
    }
}
