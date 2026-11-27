package boets.be.nbts.forecasts.domain.calculator;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.admin.domain.models.ApiCounter;
import boets.be.nbts.datacollector.models.ForecastCollectedEvent;
import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.forecasts.domain.ForecastService;
import boets.be.nbts.forecasts.domain.models.ForecastDetail;
import boets.be.nbts.forecasts.eventhandlers.ForecastEventHandler;
import boets.be.nbts.leagues.domain.models.ForecastLeague;
import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.domain.models.Standing;
import boets.be.nbts.results.web.ResultClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ResultClientService.class)
@Import(HomeMatchFixtureTest.TestConfig.class)
class HomeMatchFixtureTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AdminService adminService() {
            AdminService service = mock(AdminService.class);
            when(service.getApiCounter(any(LocalDate.class))).thenReturn(new ApiCounter(0));
            return service;
        }
    }

    @Value("classpath:/boets/be/nbts/results/web/allResultsByLeagueAndSeason.json")
    private Resource resultsResource;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ResultClientService resultClient;

    @Test
    void calculatesAllBreakdownsForAwayNextMatchFromRealLeagueResults() throws Exception {
        List<Result> results = loadResults();
        List<Standing> standings = List.of(
                standing("BK Hacken", 4),
                standing("Degerfors IF", 7),
                standing("Kalmar FF", 10),
                standing("IF Brommapojkarna", 6),
                standing("IF Elfsborg", 8));
        ForecastRawData raw = new ForecastRawData(
                new ForecastLeague(113, "Allsvenskan", 2026, "SE"), results, standings, 17);
        CalculationContext context = new ForecastService().prepareDataForForecasting(raw).stream()
                .filter(item -> item.team().equals("Hammarby FF"))
                .findFirst().orElseThrow();

        assertThat(context.totalTeams()).isEqualTo(16);
        assertThat(context.previousResults()).extracting(Result::resultId)
                .containsExactly(1494235, 1494213, 1494204, 1494217, 1494195, 1494188);
        assertThat(context.nextMatch().homeTeam()).isEqualTo("Kalmar FF");
        PointsCalculationEngine engine = new PointsCalculationEngine(List.of(
                new BaseMatchResultRule(), new OpponentRankRule(), new BoosterRule(),
                new NextMatchRule(), new HtmlDefinerRule()));
        CalculationContext scored = engine.calculateTotalPoints(context);

        assertThat(scored.calculationHtml())
                .contains("<p><small><b>1 Winst tegen BK Hacken (2026-08-09)</b></small></p><p>Score: 20 + (16 - 4) + 20 (booster) = 52</p>")
                .contains("<p><small><b>2 Winst tegen Degerfors IF (2026-07-19)</b></small></p><p>Score: 20 + (16 - 7) + 10 (booster) = 39</p>")
                .contains("<p><small><b>3 Winst tegen Kalmar FF (2026-07-12)</b></small></p><p>Score: 20 + (16 - 10) = 26</p>")
                .contains("<p><b>Eind score thuis wedstrijden : 117</b></p>");
        assertThat(scored.calculationHtml())
                .contains("<p><small><b>1 Gelijkspel tegen IF Brommapojkarna (2026-07-26)</b></small></p><p>Score: 10 + (16 - 6) = 20</p>")
                .contains("<p><small><b>2 Winst tegen IF Elfsborg (2026-07-05)</b></small></p><p>Score: 30 + (16 - 8) + 10 (booster) = 48</p>")
                .contains("<p><small><b>3 Verlies tegen BK Hacken (2026-05-31)</b></small></p><p>Score: -5 - 4 = -9</p>")
                .contains("<p><b>Eind score uit wedstrijden : 59</b></p>");
        assertThat(scored.calculationHtml()).contains(
                "<p>Volgende wedstrijd is uit</p><p>Kalmar FF - <b>Hammarby FF</b>: verminder met malus punten: 10</p>");
        assertThat(scored.calculationHtml())
                .containsSubsequence("Berekening thuiswedstrijden", "Eind score thuis wedstrijden : 117",
                        "Berekening uitwedstrijden", "Eind score uit wedstrijden : 59", "Volgende wedstrijd is uit",
                        ">Eind score</span>", "117 + 59 - 10 = <b>166</b>")
                .endsWith("<p>117 + 59 - 10 = <b>166</b></p>");
        assertThat(scored.teamScore() + scored.opponentRankScore()).isEqualTo(166);
        server.verify();
    }

    @Test
    void storesAllBreakdownsForHomeNextMatchFromRealLeagueResults() throws Exception {
        List<Result> results = resultClient.markCurrentRound(loadResults(), 17);
        List<Standing> standings = List.of(
                standing("Mjallby AIF", 2),
                standing("Malmo FF", 5),
                standing("Orgryte IS", 12),
                standing("IFK Goteborg", 9),
                standing("BK Hacken", 4),
                standing("Hammarby FF", 1));
        ForecastRawData raw = new ForecastRawData(
                new ForecastLeague(113, "Allsvenskan", 2026, "SE"), results, standings, 17);
        ForecastService service = new ForecastService();
        CalculationContext context = service.prepareDataForForecasting(raw).stream()
                .filter(item -> item.team().equals("Kalmar FF"))
                .findFirst().orElseThrow();
        assertThat(context.totalTeams()).isEqualTo(16);
        assertThat(context.previousResults()).extracting(Result::resultId)
                .containsExactly(1494221, 1494214, 1494199, 1494233, 1494231, 1494204);

        PointsCalculationEngine engine = new PointsCalculationEngine(List.of(
                new BaseMatchResultRule(), new OpponentRankRule(), new BoosterRule(),
                new NextMatchRule(), new HtmlDefinerRule()));
        new ForecastEventHandler(service, engine).onForecastDataCollected(new ForecastCollectedEvent(List.of(raw)));

        assertThat(service.getForecasts(113, 17, 2026)).hasSize(1);
        ForecastDetail detail = service.getForecasts(113, 17, 2026).getFirst().forecastDetails().stream()
                .filter(item -> item.team().equals("Kalmar FF"))
                .findFirst().orElseThrow();
        assertThat(detail.calculationHtml())
                .contains("<p><small><b>1 Winst tegen Mjallby AIF (2026-07-25)</b></small></p><p>Score: 20 + (16 - 2) + 20 (booster) = 54</p>")
                .contains("<p><small><b>2 Gelijkspel tegen Malmo FF (2026-07-20)</b></small></p><p>Score: 5 + (16 - 5) = 16</p>")
                .contains("<p><small><b>3 Winst tegen Orgryte IS (2026-07-05)</b></small></p><p>Score: 20 + (16 - 12) = 24</p>")
                .contains("<p><b>Eind score thuis wedstrijden : 94</b></p>");
        assertThat(detail.calculationHtml())
                .contains("<p><small><b>1 Verlies tegen IFK Goteborg (2026-08-09)</b></small></p><p>Score: -5 - 9 - 20 (booster) = -34</p>")
                .contains("<p><small><b>2 Gelijkspel tegen BK Hacken (2026-08-01)</b></small></p><p>Score: 10 + (16 - 4) = 22</p>")
                .contains("<p><small><b>3 Verlies tegen Hammarby FF (2026-07-12)</b></small></p><p>Score: -5 - 1 = -6</p>")
                .contains("<p><b>Eind score uit wedstrijden : -18</b></p>");
        assertThat(detail.calculationHtml()).contains(
                "<p>Volgende wedstrijd is thuis</p><p><b>Kalmar FF</b> - Hammarby FF: voeg extra punten toe: 10</p>");
        assertThat(detail.calculationHtml())
                .containsSubsequence("Berekening thuiswedstrijden", "Eind score thuis wedstrijden : 94",
                        "Berekening uitwedstrijden", "Eind score uit wedstrijden : -18",
                        "Volgende wedstrijd is thuis", ">Eind score</span>")
                .contains("<h3 class=\"aligncenter\" style=\"color: #5e9ca0; text-align: left;\"><span style=\"color: #333300;\">Eind score</span></h3>")
                .endsWith("<p>94 - 18 + 10 = <b>86</b></p>");
        assertThat(detail.totalPoints()).isEqualTo(86);
        server.verify();
    }

    private List<Result> loadResults() throws Exception {
        server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/fixtures")))
                .andExpect(queryParam("league", "113"))
                .andExpect(queryParam("season", "2026"))
                .andRespond(withSuccess(resultsResource.getContentAsString(StandardCharsets.UTF_8), MediaType.APPLICATION_JSON));
        List<Result> results = resultClient.getResultsByLeagueAndSeason(113, 2026);
        assertThat(results).hasSize(240);
        return results;
    }

    private Standing standing(String team, int rank) {
        return new Standing(113, 2026, rank, team, 0, 0, 0, 0, 0, 0, LocalDate.of(2026, 8, 16));
    }
}
