package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Component
@Order(5)
public class HtmlDefinerRule implements PointsCalculationRule {

    @Override
    public CalculationContext calculatePoints(CalculationContext context) {
        List<Result> homeMatches = context.previousResults().stream()
                .filter(match -> isHomeMatch(match, context.team()))
                .toList();
        List<Result> awayMatches = context.previousResults().stream()
                .filter(match -> match.awayTeam().equals(context.team()))
                .toList();
        MatchBreakdown home = calculateMatchHtml(homeMatches, context, true);
        MatchBreakdown away = calculateMatchHtml(awayMatches, context, false);
        return context.withCalculationHtml(home.html() + away.html() + calculateNextMatchHtml(context)
                + calculateFinalScoreHtml(context, home.total(), away.total()));
    }

    private String calculateFinalScoreHtml(CalculationContext context, int home, int away) {
        if (context.nextMatch() == null) {
            return "";
        }
        int next = NextMatchRule.calculateNextMatchPoints(context.nextMatch(), context.team());
        return """
                <h3 class="aligncenter" style="color: #5e9ca0; text-align: left;"><span style="color: #333300;">Eind score</span></h3>
                """ + "<p>" + home + formatAdjustment(away) + formatAdjustment(next)
                + " = <b>" + (home + away + next) + "</b></p>";
    }

    private String formatAdjustment(int points) {
        return points < 0 ? " - " + -(long) points : " + " + points;
    }

    private String calculateNextMatchHtml(CalculationContext context) {
        Result match = context.nextMatch();
        if (match == null) {
            return "";
        }
        boolean home = match.homeTeam().equals(context.team());
        if (!home && !match.awayTeam().equals(context.team())) {
            throw new IllegalArgumentException("Next match does not contain team: " + context.team());
        }
        int points = NextMatchRule.calculateNextMatchPoints(match, context.team());
        String team = HtmlUtils.htmlEscape(context.team());
        String opponent = HtmlUtils.htmlEscape(home ? match.awayTeam() : match.homeTeam());
        String fixture = home ? "<b>" + team + "</b> - " + opponent : opponent + " - <b>" + team + "</b>";
        return "<p>Volgende wedstrijd is " + (home ? "thuis" : "uit") + "</p><p>" + fixture
                + (home ? ": voeg extra punten toe: " : ": verminder met malus punten: ")
                + Math.abs(points) + "</p><br>";
    }

    private MatchBreakdown calculateMatchHtml(List<Result> matches, CalculationContext context, boolean home) {
        StringBuilder html = new StringBuilder(home ? getInitialHomeGameHtml() : getInitialAwayGameHtml());
        int index = 1;
        int total = 0;
        for (Result match : matches) {
            boolean lost = home ? match.homeTeamHasLost() : match.homeTeamHasWon() && !match.homeTeamHasLost();
            String matchType = home
                    ? (match.homeTeamHasWon() ? "Winst" : match.homeTeamHasLost() ? "Verlies" : "Gelijkspel")
                    : (match.homeTeamHasWon() ? "Verlies" : match.homeTeamHasLost() ? "Winst" : "Gelijkspel");
            int opponentRank = OpponentRankRule.getOpponentRankFromMatch(match, context);
            int base = home ? BaseMatchResultRule.calculateHomeMatchPoints(match) : BaseMatchResultRule.calculateAwayMatchPoints(match);
            int rankPoints = home ? OpponentRankRule.calculateHomeRankPoints(match, context.totalTeams(), opponentRank)
                    : OpponentRankRule.calculateAwayRankPoints(match, context.totalTeams(), opponentRank);
            int booster = home ? BoosterRule.calculateHomeBoosterPoints(match, index) : BoosterRule.calculateAwayBoosterPoints(match, index);
            html.append(getGeneralMatchHtml(match, index, matchType, home));
            html.append("<p>Score: ").append(base);
            if (lost) {
                html.append(" - ").append(opponentRank);
            } else {
                html.append(" + (").append(context.totalTeams()).append(" - ").append(opponentRank).append(")");
            }
            if (booster != 0) {
                html.append(booster > 0 ? " + " : " - ").append(Math.abs(booster)).append(" (booster)");
            }
            int matchPoints = base + rankPoints + booster;
            html.append(" = ").append(matchPoints).append("</p>");
            total += matchPoints;
            index++;
        }
        html.append("<br><p><b>Eind score ").append(home ? "thuis" : "uit")
                .append(" wedstrijd : ").append(total).append("</b></p><br>");
        return new MatchBreakdown(html.toString(), total);
    }

    private record MatchBreakdown(String html, int total) {}

    private StringBuilder getGeneralMatchHtml(Result match, int index, String matchType, boolean home) {
        StringBuilder htmlBuilder = new StringBuilder();
        return htmlBuilder.append("<p><small><b>")
                .append(index)
                .append(". ")
                .append(matchType)
                .append(" tegen ")
                .append(HtmlUtils.htmlEscape(home ? match.awayTeam() : match.homeTeam()))
                .append(" (")
                .append(match.matchDate())
                .append(")")
                .append("</b></small></p>");

    }

    private String getInitialHomeGameHtml() {
        return """
                <h3 class="aligncenter" style="color: #5e9ca0; text-align: left;"><span style="color: #333300;">Berekening thuiswedstrijden</span></h3>
                <p><span style="color: #333300;">Winst:   20 punten + (aantal teams - rank tegenstrever)   +  booster (booster 1=20,  booster 2=10)</span></p>
                <p><span style="color: #333300;">Verlies : -10 punten -  (rank tegenstrever)  -  negatieve booster (</span><span style="color: #333300;">booster 1=20, booster 2=10)</span></p>
                <p><span style="color: #333300;">gelijkspel: 5 punten + (aantal teams - rank tegenstrever) </span></p>                
                """;
    }

    private String getInitialAwayGameHtml() {
        return """
                <h3 class="aligncenter" style="color: #5e9ca0; text-align: left;"><span style="color: #333300;">Berekening uitwedstrijden</span></h3>
                <p><span style="color: #333300;">Winst: 30 punten + (aantal teams - rank tegenstrever) + booster (booster 1=20, booster 2=10)</span></p>
                <p><span style="color: #333300;">Verlies: -5 punten - (rank tegenstrever) - negatieve booster (booster 1=20, booster 2=10)</span></p>
                <p><span style="color: #333300;">Gelijkspel: 10 punten + (aantal teams - rank tegenstrever)</span></p>
                """;
    }

}
