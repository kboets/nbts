package boets.be.nbts.forecasts.web;

import boets.be.nbts.forecasts.domain.ForecastService;
import boets.be.nbts.forecasts.domain.models.Forecast;
import boets.be.nbts.forecasts.domain.models.ForecastDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(ForecastController.class)
@AutoConfigureRestTestClient
class ForecastControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private ForecastService forecastService;

    @Test
    void returnsOneCombinedHtmlStringInForecastDetail() {
        String html = "<h3>Berekening thuiswedstrijden</h3><h3>Berekening uitwedstrijden</h3>"
                + "<p>Volgende wedstrijd is thuis</p><h3>Eind score</h3><p>10 + 20 + 10 = <b>40</b></p>";
        ForecastDetail detail = new ForecastDetail(LocalDate.of(2026, 8, 16), "Team A", "Team A",
                "Team B", 30, 10, 40, html);
        when(forecastService.getForecasts(113, 17, 2026))
                .thenReturn(List.of(new Forecast("Allsvenskan", "SE", 17, List.of(detail))));

        restTestClient.get().uri("/api/forecasts/{leagueId}/{round}/{season}", 113, 17, 2026)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$[0].forecastDetails[0].totalPoints").isEqualTo(40)
                .jsonPath("$[0].forecastDetails[0].calculationHtml").isEqualTo(html)
                .jsonPath("$[0].forecastDetails[0].homeMatchHtml").doesNotExist()
                .jsonPath("$[0].forecastDetails[0].awayMatchHtml").doesNotExist()
                .jsonPath("$[0].forecastDetails[0].nextMatchHtml").doesNotExist()
                .jsonPath("$[0].forecastDetails[0].finalScoreHtml").doesNotExist();
    }
}
