package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BaseMatchResultRuleTest {

    private BaseMatchResultRule rule;

    @BeforeEach
    void setUp() {
        rule = new BaseMatchResultRule();
    }

    @Test
    void calculatePoints_shouldReturnZero_whenNoPreviousResults() {
        CalculationContext context = new CalculationContext(
                18, "TeamA", "TeamB", 0, 0, null, Collections.emptyList(),  Collections.emptyList());

        int points = rule.calculatePoints(context);

        assertThat(points).isZero();
    }

    @Test
    void calculatePoints_shouldCalculateHomeWinPoints() {
        Result result = createResult("TeamA", "TeamB", true, false);
        CalculationContext context = createContext("TeamA", List.of(result));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(20);
    }

    @Test
    void calculatePoints_shouldCalculateHomeLossPoints() {
        Result result = createResult("TeamA", "TeamB", false, true);
        CalculationContext context = createContext("TeamA", List.of(result));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(-10);
    }

    @Test
    void calculatePoints_shouldCalculateHomeDrawPoints() {
        Result result = createResult("TeamA", "TeamB", false, false);
        CalculationContext context = createContext("TeamA", List.of(result));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(5);
    }

    @Test
    void calculatePoints_shouldCalculateAwayWinPoints() {
        // When away team wins, homeTeamHasLost is true
        Result result = createResult("TeamB", "TeamA", false, true);
        CalculationContext context = createContext("TeamA", List.of(result));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(30);
    }

    @Test
    void calculatePoints_shouldCalculateAwayLossPoints() {
        // When away team loses, homeTeamHasWon is true
        Result result = createResult("TeamB", "TeamA", true, false);
        CalculationContext context = createContext("TeamA", List.of(result));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(-5);
    }

    @Test
    void calculatePoints_shouldCalculateAwayDrawPoints() {
        // When away match ends in draw, both homeTeamHasWon and homeTeamHasLost are false
        Result result = createResult("TeamB", "TeamA", false, false);
        CalculationContext context = createContext("TeamA", List.of(result));

        int points = rule.calculatePoints(context);

        assertThat(points).isEqualTo(10);
    }

    @Test
    void calculatePoints_shouldIgnoreMatchesWhereTeamIsNotPlaying() {
        Result result = createResult("TeamB", "TeamC", true, false);
        CalculationContext context = createContext("TeamA", List.of(result));

        int points = rule.calculatePoints(context);

        assertThat(points).isZero();
    }

    @Test
    void calculatePoints_shouldAccumulatePointsAcrossMultipleMatches() {
        List<Result> results = List.of(
                createResult("TeamA", "TeamB", true, false),   // Home win: +20
                createResult("TeamC", "TeamA", false, true),   // Away win: +30
                createResult("TeamA", "TeamD", false, false),  // Home draw: +5
                createResult("TeamE", "TeamA", false, false),  // Away draw: +10
                createResult("TeamA", "TeamF", false, true),   // Home loss: -10
                createResult("TeamG", "TeamA", true, false)    // Away loss: -5
        );
        CalculationContext context = createContext("TeamA", results);

        int points = rule.calculatePoints(context);

        // 20 + 30 + 5 + 10 - 10 - 5 = 50
        assertThat(points).isEqualTo(50);
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
