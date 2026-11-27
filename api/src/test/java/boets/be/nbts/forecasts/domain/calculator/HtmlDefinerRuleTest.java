package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.domain.models.Standing;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmlDefinerRuleTest {

    @Test
    void retainsCombinedHtmlWhenScoresAreUpdated() {
        CalculationContext original = context(List.of(), List.of()).withCalculationHtml("<p>Calculation</p>");

        CalculationContext updated = original.withTeamScore(10).withOpponentRankScore(3);

        assertThat(updated.teamScore()).isEqualTo(17);
        assertThat(updated.opponentRankScore()).isEqualTo(3);
        assertThat(updated.calculationHtml()).isEqualTo("<p>Calculation</p>");
    }

    @Test
    void showsHomeNextMatchBonusAndEscapesTeamNames() {
        Result match = result("Team A & B", "Opp <onent>", false, false);
        CalculationContext context = new CalculationContext(18, "Team A & B", "Opp <onent>", 0, 0,
                match, List.of(), List.of());
        CalculationContext scored = new NextMatchRule().calculatePoints(context);

        assertThat(scored.teamScore()).isEqualTo(10);
        assertThat(new HtmlDefinerRule().calculatePoints(scored).calculationHtml()).contains(
                "<p>Volgende wedstrijd is thuis</p><p><b>Team A &amp; B</b> - Opp &lt;onent&gt;: voeg extra punten toe: 10</p>");
    }

    @Test
    void showsAwayNextMatchMalusAndEscapesTeamNames() {
        Result match = result("Opp <onent>", "Team A & B", false, false);
        CalculationContext context = new CalculationContext(18, "Team A & B", "Opp <onent>", 0, 0,
                match, List.of(), List.of());
        CalculationContext scored = new NextMatchRule().calculatePoints(context);

        assertThat(scored.teamScore()).isEqualTo(-10);
        assertThat(new HtmlDefinerRule().calculatePoints(scored).calculationHtml()).contains(
                "<p>Volgende wedstrijd is uit</p><p>Opp &lt;onent&gt; - <b>Team A &amp; B</b>: verminder met malus punten: 10</p>");
    }

    @Test
    void handlesMissingOrUnrelatedNextMatch() {
        assertThat(new HtmlDefinerRule().calculatePoints(context(List.of(), List.of())).calculationHtml())
                .doesNotContain("Volgende wedstrijd is", ">Eind score</span>");
        CalculationContext unrelated = new CalculationContext(18, "Team A", "Other", 0, 0,
                result("Other", "Different", false, false), List.of(), List.of());
        assertThatThrownBy(() -> new HtmlDefinerRule().calculatePoints(unrelated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team A");
    }

    @Test
    void showsFinalEquationWithNegativeAwaySubtotalAndHomeBonus() {
        Result next = result("Team A", "Next", false, false);
        CalculationContext context = new CalculationContext(18, "Team A", "Next", 0, 0, next,
                List.of(result("Team A", "B", true, false), result("B", "Team A", true, false)),
                List.of(standing("B", 3)));
        PointsCalculationEngine engine = new PointsCalculationEngine(List.of(
                new BaseMatchResultRule(), new OpponentRankRule(), new BoosterRule(),
                new NextMatchRule(), new HtmlDefinerRule()));

        CalculationContext scored = engine.calculateTotalPoints(context);

        assertThat(scored.calculationHtml())
                .containsSubsequence("Berekening thuiswedstrijden", "Eind score thuis wedstrijden : 55",
                        "Berekening uitwedstrijden", "Eind score uit wedstrijden : -28",
                        "Volgende wedstrijd is thuis", ">Eind score</span>")
                .contains("<h3 class=\"aligncenter\" style=\"color: #5e9ca0; text-align: left;\"><span style=\"color: #333300;\">Eind score</span></h3>")
                .endsWith("<p>55 - 28 + 10 = <b>37</b></p>");
        assertThat(scored.teamScore() + scored.opponentRankScore()).isEqualTo(37);
    }

    @Test
    void showsZeroSubtotalsAndAwayMalusWithoutPreviousGames() {
        CalculationContext context = new CalculationContext(18, "Team A", "B", 0, 0,
                result("B", "Team A", false, false), List.of(), List.of());
        assertThat(new HtmlDefinerRule().calculatePoints(context).calculationHtml())
                .endsWith("<p>0 + 0 - 10 = <b>-10</b></p>");
    }

    @Test
    void showsHomeScoresWithBoosterAndCurrentOpponentRank() {
        List<Result> matches = List.of(
                result("Team A", "B & C", true, false),
                result("Away", "Team A", true, false),
                result("Team A", "D", true, false),
                result("Team A", "E", true, false));
        CalculationContext context = context(matches, List.of(standing("B & C", 3), standing("D", 5), standing("E", 8)));

        CalculationContext result = new HtmlDefinerRule().calculatePoints(context);

        assertThat(result.calculationHtml())
                .contains("<p><small><b>1 Winst tegen B &amp; C (2026-01-01)</b></small></p>")
                .contains("Score: 20 + (18 - 3) + 20 (booster) = 55")
                .contains("Score: 20 + (18 - 5) + 10 (booster) = 43")
                .contains("Score: 20 + (18 - 8) = 30")
                .contains("<p><b>Eind score thuis wedstrijden : 128</b></p>");
        assertThat(result.teamScore()).isEqualTo(context.teamScore());
        assertThat(result.opponentRankScore()).isEqualTo(context.opponentRankScore());
    }

    @Test
    void showsLossAndDrawWithoutChangingBoosterPositions() {
        List<Result> matches = List.of(
                result("Team A", "B", false, true),
                result("Team A", "C", false, false),
                result("Team A", "D", false, true));
        CalculationContext context = context(matches, List.of(standing("B", 3), standing("C", 5), standing("D", 8)));

        assertThat(new HtmlDefinerRule().calculatePoints(context).calculationHtml())
                .contains("Score: -10 - 3 - 20 (booster) = -33")
                .contains("Score: 5 + (18 - 5) = 18")
                .contains("Score: -10 - 8 = -18")
                .contains("<p><b>Eind score thuis wedstrijden : -33</b></p>");
    }

    @Test
    void usesTheSameFallbackForMissingStandingsAsTheScoringRule() {
        CalculationContext context = context(List.of(result("Team A", "Unknown", true, false)), List.of());
        CalculationContext scored = new OpponentRankRule().calculatePoints(context);
        assertThat(scored.opponentRankScore()).isEqualTo(18);
        assertThat(new HtmlDefinerRule().calculatePoints(scored).calculationHtml())
                .contains("Score: 20 + (18 - 0) + 20 (booster) = 58");
    }

    @Test
    void showsTheSecondNegativeBoosterAndHandlesNoHomeMatches() {
        CalculationContext context = context(List.of(
                result("Team A", "B", false, false),
                result("Team A", "C", false, true)), List.of(standing("B", 3), standing("C", 5)));
        assertThat(new HtmlDefinerRule().calculatePoints(context).calculationHtml())
                .contains("Score: -10 - 5 - 10 (booster) = -25");
        assertThat(new HtmlDefinerRule().calculatePoints(context(List.of(), List.of())).calculationHtml())
                .contains("Berekening thuiswedstrijden")
                .contains("<p><b>Eind score thuis wedstrijden : 0</b></p>")
                .doesNotContain("Score:");
    }

    @Test
    void showsAwayScoresAndUsesSeparateBoosterPositions() {
        CalculationContext context = context(List.of(
                result("Team A", "Home opponent", true, false),
                result("B & C", "Team A", true, false),
                result("D", "Team A", true, false),
                result("E", "Team A", false, true)),
                List.of(standing("B & C", 3), standing("D", 5), standing("E", 8)));

        CalculationContext scored = new HtmlDefinerRule().calculatePoints(context);

        assertThat(scored.calculationHtml())
                .contains("<p><small><b>1 Verlies tegen B &amp; C (2026-01-01)</b></small></p>")
                .contains("Score: -5 - 3 - 20 (booster) = -28")
                .contains("Score: -5 - 5 - 10 (booster) = -20")
                .contains("Score: 30 + (18 - 8) = 40")
                .contains("<p><b>Eind score uit wedstrijden : -8</b></p>");
        assertThat(scored.teamScore()).isEqualTo(context.teamScore());
        assertThat(scored.opponentRankScore()).isEqualTo(context.opponentRankScore());
    }

    @Test
    void showsAwayWinBoosterAndMissingRankFallback() {
        CalculationContext context = context(List.of(
                result("Unknown", "Team A", false, true),
                result("Draw", "Team A", false, false)), List.of(standing("Draw", 5)));
        assertThat(new HtmlDefinerRule().calculatePoints(context).calculationHtml())
                .contains("Score: 30 + (18 - 0) + 20 (booster) = 68")
                .contains("Score: 10 + (18 - 5) = 23");
        assertThat(new HtmlDefinerRule().calculatePoints(context(List.of(), List.of())).calculationHtml())
                .contains("Berekening uitwedstrijden")
                .endsWith("<p><b>Eind score uit wedstrijden : 0</b></p>")
                .doesNotContain("Score:");
    }

    private CalculationContext context(List<Result> results, List<Standing> standings) {
        return new CalculationContext(18, "Team A", "Next", 7, 0, null, results, standings);
    }

    private Result result(String home, String away, boolean won, boolean lost) {
        return new Result(1, "League", home, away, 0, 0, LocalDate.of(2026, 1, 1), "FT", won, lost, 1, false);
    }

    private Standing standing(String team, int rank) {
        return new Standing(1, 2026, rank, team, 1, 0, 0, 0, 0, 0, LocalDate.of(2026, 1, 1));
    }
}
