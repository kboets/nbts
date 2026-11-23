package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.results.domain.models.Result;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(5)
public class HtmlDefinerRule implements PointsCalculationRule {

    @Override
    public CalculationContext calculatePoints(CalculationContext context) {
        StringBuilder htmlBuilder = new StringBuilder();
        //1. Berekening thuiswedstrijden
        List<Result> homeMatches = context.previousResults().stream()
                .filter(match -> isHomeMatch(match, context.team()))
                .toList();
        htmlBuilder.append(calculateHomeMatchHtml(homeMatches));
        List<Result> awayMatches = context.previousResults().stream()
                .filter(match -> !isHomeMatch(match, context.team()))
                .toList();
        htmlBuilder.append(calculateAwayMatchHtml(awayMatches));
        return context;

    }



    private String calculateHomeMatchHtml(List<Result> matches) {
        StringBuilder html = new StringBuilder(getInitialHomeGameHtml());
        int index = 1;
        for (Result match : matches) {
            if (match.homeTeamHasWon()) {
                html.append(getGeneralMatchHtml(match, index, "Winst"));
            } else if (match.homeTeamHasLost()) {
                html.append(getGeneralMatchHtml(match, index, "Verlies"));
            } else {
                html.append(getGeneralMatchHtml(match, index, "Gelijkspel"));
            }
            index++;
        }
        return html.toString();
    }

    private String calculateAwayMatchHtml(List<Result> matches) {
        StringBuilder html = new StringBuilder(getInitialAwayGameHtml());
        int index = 1;
        for (Result match : matches) {
            if (match.homeTeamHasLost()) {
                html.append(getGeneralMatchHtml(match, index, "Winst"));
            } else if (match.homeTeamHasWon()) {
                html.append(getGeneralMatchHtml(match, index, "Verlies"));
            } else {
                html.append(getGeneralMatchHtml(match, index, "Gelijkspel"));
            }
            index++;
        }
        return html.toString();
    }

    private StringBuilder getGeneralMatchHtml(Result match, int index, String matchType) {
        StringBuilder htmlBuilder = new StringBuilder();
        return htmlBuilder.append("<p><b>")
                .append(index)
                .append(" ")
                .append(matchType)
                .append(" tegen ")
                .append(match.awayTeam())
                .append(" (")
                .append(match.matchDate())
                .append(")")
                .append("</b></p>");

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
                <h3 class="aligncenter" style="color: #5e9ca0; text-align: left;"><span style="color: #333300;">Berekening uitwedstrijden</span></h3>
                <p><span style="color: #333300;">Winst:   30 punten + (aantal teams - rank tegenstrever)   +  booster(booster 1=20,  booster 2=10)</span></p>
                <p><span style="color: #333300;">Verlies : -5 punten -  (rank tegenstrever)  -  negatieve booster</span><span style="color: #333300;"> (= booster 1=20, booster 2=10)</span></p>
                <p><span style="color: #333300;">gelijkspel: 10 punten + (aantal teams - rank tegenstrever) </span></p>                
                """;
    }

    private int getOpponentRankFromMatch(Result match, CalculationContext context) {
        String opponentTeam = match.awayTeam().equals(context.team()) ? match.homeTeam() : match.awayTeam();
        for (var standing : context.standings()) {
            if (standing.teamName().equals(opponentTeam)) {
                return standing.rank();
            }
        }
        return 0;
    }
}
