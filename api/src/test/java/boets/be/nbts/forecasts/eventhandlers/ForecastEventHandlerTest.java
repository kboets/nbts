package boets.be.nbts.forecasts.eventhandlers;

import boets.be.nbts.datacollector.models.ForecastCollectedEvent;
import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.forecasts.domain.ForecastService;
import boets.be.nbts.forecasts.domain.calculator.BaseMatchResultRule;
import boets.be.nbts.forecasts.domain.calculator.BoosterRule;
import boets.be.nbts.forecasts.domain.calculator.CalculationContext;
import boets.be.nbts.forecasts.domain.calculator.HtmlDefinerRule;
import boets.be.nbts.forecasts.domain.calculator.NextMatchRule;
import boets.be.nbts.forecasts.domain.calculator.OpponentRankRule;
import boets.be.nbts.forecasts.domain.calculator.PointsCalculationEngine;
import boets.be.nbts.forecasts.domain.models.Forecast;
import boets.be.nbts.leagues.domain.models.ForecastLeague;
import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.domain.models.Standing;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ForecastEventHandlerTest {

    @Test
    void storesCombinedCalculationWithForecast() {
        ForecastService service = mock(ForecastService.class);
        PointsCalculationEngine engine = new PointsCalculationEngine(List.of(
                new BaseMatchResultRule(), new OpponentRankRule(), new BoosterRule(),
                new NextMatchRule(), new HtmlDefinerRule()));
        ForecastService cache = new ForecastService();
        doAnswer(invocation -> {
            cache.saveForecastCache(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(service).saveForecastCache(any(), any());
        Result previous = new Result(1, "League", "A", "B", 2, 0,
                LocalDate.of(2026, 1, 1), "FT", true, false, 1, false);
        Result away = new Result(3, "League", "B", "A", 0, 1,
                LocalDate.of(2025, 12, 1), "FT", false, true, 1, false);
        Result next = new Result(2, "League", "A", "C", null, null,
                LocalDate.of(2026, 1, 8), "NS", false, false, 2, true);
        Standing standing = new Standing(1, 2026, 3, "B", 2, 0, 0, 0, 0, 0, LocalDate.of(2026, 1, 1));
        ForecastRawData raw = new ForecastRawData(new ForecastLeague(1, "League", 2026, "BE"),
                List.of(previous, next), List.of(standing), 1);
        when(service.canForecastResults(raw.results())).thenReturn(true);
        when(service.prepareDataForForecasting(raw)).thenReturn(List.of(
                new CalculationContext(18, "A", "C", 0, 0, next, List.of(previous, away), List.of(standing))));

        new ForecastEventHandler(service, engine).onForecastDataCollected(new ForecastCollectedEvent(List.of(raw)));

        List<Forecast> forecasts = cache.getForecasts(1, 2, 2026);
        assertThat(forecasts).hasSize(1);
        assertThat(forecasts.getFirst().forecastDetails()).singleElement().satisfies(detail -> {
            assertThat(detail.totalPoints()).isEqualTo(130);
            assertThat(detail.calculationHtml())
                    .containsSubsequence("Berekening thuiswedstrijden", "Eind score thuis wedstrijd : 55",
                            "Berekening uitwedstrijden", "Eind score uit wedstrijd : 65",
                            "Volgende wedstrijd is thuis", ">Eind score</span>")
                    .contains("Score: 20 + (18 - 3) + 20 (booster) = 55",
                            "Score: 30 + (18 - 3) + 20 (booster) = 65",
                            "<p>Volgende wedstrijd is thuis</p><p><b>A</b> - C: voeg extra punten toe: 10</p>")
                    .endsWith("<p>55 + 65 + 10 = <b>130</b></p>");
        });
    }
}
