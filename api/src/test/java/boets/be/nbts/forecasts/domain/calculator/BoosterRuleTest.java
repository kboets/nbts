package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoosterRuleTest {

    private BoosterRule rule;

    @BeforeEach
    void setUp() {
        rule = new BoosterRule();
    }

    @Test
    void calculatePoints_shouldReturnZero_whenNoPreviousResults() {
        CalculationContext context = createContext("TeamA", Collections.emptyList());

        int points = rule.calculatePoints(context);

        assertThat(points).isZero();
    }

    @Test
    void calculatePoints_shouldReturnFirstBoosterPoints_forFirstHomeWin() {
        Result homeWin = createResult("TeamA", "TeamB", true, false);
        CalculationContext context = createContext("TeamA", List.of(homeWin));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(20);
    }

    @Test
    void calculatePoints_shouldReturnNegativeFirstBoosterPoints_forFirstHomeLoss() {
        Result homeLoss = createResult("TeamA", "TeamB", false, true);
        CalculationContext context = createContext("TeamA", List.of(homeLoss));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(-20);
    }

    @Test
    void calculatePoints_shouldReturnZero_forFirstHomeDraw() {
        Result homeDraw = createResult("TeamA", "TeamB", false, false);
        CalculationContext context = createContext("TeamA", List.of(homeDraw));

        int points = rule.calculatePoints(context);

        assertThat(points).isZero();
    }

    @Test
    void calculatePoints_shouldReturnSecondBoosterPoints_forSecondHomeWin() {
        Result homeDraw = createResult("TeamA", "TeamB", false, false);
        Result homeWin = createResult("TeamA", "TeamC", true, false);
        CalculationContext context = createContext("TeamA", List.of(homeDraw, homeWin));

        int points = rule.calculatePoints(context);

        // 0 (draw) + 10 (2nd home win) = 10
        assertThat(points).isEqualTo(10);
    }

    @Test
    void calculatePoints_shouldReturnNegativeSecondBoosterPoints_forSecondHomeLoss() {
        Result homeDraw = createResult("TeamA", "TeamB", false, false);
        Result homeLoss = createResult("TeamA", "TeamC", false, true);
        CalculationContext context = createContext("TeamA", List.of(homeDraw, homeLoss));

        int points = rule.calculatePoints(context);

        // 0 (draw) + (-10) (2nd home loss) = -10
        assertThat(points).isEqualTo(-10);
    }

    @Test
    void calculatePoints_shouldReturnZero_forThirdAndSubsequentHomeMatches() {
        Result homeWin1 = createResult("TeamA", "TeamB", true, false);  // +20
        Result homeWin2 = createResult("TeamA", "TeamC", true, false);  // +10
        Result homeWin3 = createResult("TeamA", "TeamD", true, false);  // 0 (3rd match)
        Result homeLoss4 = createResult("TeamA", "TeamE", false, true); // 0 (4th match)
        CalculationContext context = createContext("TeamA", List.of(homeWin1, homeWin2, homeWin3, homeLoss4));

        int points = rule.calculatePoints(context);

        // 20 + 10 + 0 + 0 = 30
        assertThat(points).isEqualTo(30);
    }

    @Test
    void calculatePoints_shouldReturnFirstBoosterPoints_forFirstAwayWin() {
        // Away win: homeTeamHasLost = true
        Result awayWin = createResult("TeamB", "TeamA", false, true);
        CalculationContext context = createContext("TeamA", List.of(awayWin));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(20);
    }

    @Test
    void calculatePoints_shouldReturnNegativeFirstBoosterPoints_forFirstAwayLoss() {
        // Away loss: homeTeamHasWon = true
        Result awayLoss = createResult("TeamB", "TeamA", true, false);
        CalculationContext context = createContext("TeamA", List.of(awayLoss));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(-20);
    }

    @Test
    void calculatePoints_shouldReturnZero_forFirstAwayDraw() {
        // Away draw: homeTeamHasWon = false, homeTeamHasLost = false
        Result awayDraw = createResult("TeamB", "TeamA", false, false);
        CalculationContext context = createContext("TeamA", List.of(awayDraw));

        int points = rule.calculatePoints(context);

        assertThat(points).isZero();
    }

    @Test
    void calculatePoints_shouldReturnSecondBoosterPoints_forSecondAwayWin() {
        Result awayDraw = createResult("TeamB", "TeamA", false, false);
        Result awayWin = createResult("TeamC", "TeamA", false, true);
        CalculationContext context = createContext("TeamA", List.of(awayDraw, awayWin));

        int points = rule.calculatePoints(context);

        // 0 (draw) + 10 (2nd away win) = 10
        assertThat(points).isEqualTo(10);
    }

    @Test
    void calculatePoints_shouldReturnNegativeSecondBoosterPoints_forSecondAwayLoss() {
        Result awayDraw = createResult("TeamB", "TeamA", false, false);
        Result awayLoss = createResult("TeamC", "TeamA", true, false);
        CalculationContext context = createContext("TeamA", List.of(awayDraw, awayLoss));

        int points = rule.calculatePoints(context);

        // 0 (draw) + (-10) (2nd away loss) = -10
        assertThat(points).isEqualTo(-10);
    }

    @Test
    void calculatePoints_shouldReturnZero_forThirdAndSubsequentAwayMatches() {
        Result awayWin1 = createResult("TeamB", "TeamA", false, true);  // +20
        Result awayWin2 = createResult("TeamC", "TeamA", false, true);  // +10
        Result awayWin3 = createResult("TeamD", "TeamA", false, true);  // 0 (3rd match)
        Result awayLoss4 = createResult("TeamE", "TeamA", true, false); // 0 (4th match)
        CalculationContext context = createContext("TeamA", List.of(awayWin1, awayWin2, awayWin3, awayLoss4));

        int points = rule.calculatePoints(context);

        // 20 + 10 + 0 + 0 = 30
        assertThat(points).isEqualTo(30);
    }

    @Test
    void calculatePoints_shouldTrackHomeAndAwayCountsSeparately() {
        List<Result> results = List.of(
                createResult("TeamA", "TeamB", true, false),   // Home match 1 (win): +20
                createResult("TeamC", "TeamA", false, true),   // Away match 1 (win): +20
                createResult("TeamA", "TeamD", false, true),   // Home match 2 (loss): -10
                createResult("TeamE", "TeamA", false, false),  // Away match 2 (draw): 0
                createResult("TeamA", "TeamF", true, false),   // Home match 3 (win): 0
                createResult("TeamG", "TeamA", true, false)    // Away match 3 (loss): 0
        );
        CalculationContext context = createContext("TeamA", results);

        int points = rule.calculatePoints(context);

        // 20 + 20 - 10 + 0 + 0 + 0 = 30
        assertThat(points).isEqualTo(30);
    }

    private CalculationContext createContext(String team, List<Result> previousResults) {
        return new CalculationContext(
                18,
                team,
                "Opponent",
                0,
                0,
                null,
                previousResults,
                Collections.emptyList()
        );
    }

    private Result createResult(String homeTeam, String awayTeam, boolean homeTeamWon, boolean homeTeamLost) {
        return new Result(
                1,
                "League",
                homeTeam,
                awayTeam,
                homeTeamWon ? 2 : (homeTeamLost ? 0 : 1),
                homeTeamWon ? 0 : (homeTeamLost ? 2 : 1),
                LocalDate.of(2026, 1, 1),
                "FT",
                homeTeamWon,
                homeTeamLost,
                1,
                false
        );
    }
}
