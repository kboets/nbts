package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.ResultService;
import boets.be.nbts.results.domain.models.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(ResultController.class)
@AutoConfigureRestTestClient
class ResultControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private ResultService resultService;

    @Test
    @DisplayName("GET /api/results/{leagueId}/{season} - should return results for the given league and season")
    void getResultsByLeagueAndSeason_shouldReturnResults() {
        // Given
        int leagueId = 113;
        int season = 2026;
        Result result = new Result(1494118, "Allsvenskan", "Hammarby FF", "Mjallby AIF", 3, 0, "2026-04-04", "FT", true, false, 1, false);
        List<Result> results = List.of(result);

        when(resultService.getResultsByLeagueAndSeason(leagueId, season)).thenReturn(results);

        // When & Then
        restTestClient.get().uri("/api/results/{leagueId}/{season}", leagueId, season)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$[0].resultId").isEqualTo(1494118)
                .jsonPath("$[0].leagueName").isEqualTo("Allsvenskan")
                .jsonPath("$[0].homeTeam").isEqualTo("Hammarby FF")
                .jsonPath("$[0].awayTeam").isEqualTo("Mjallby AIF")
                .jsonPath("$[0].homeTeamScore").isEqualTo(3)
                .jsonPath("$[0].awayTeamScore").isEqualTo(0)
                .jsonPath("$[0].matchDate").isEqualTo("2026-04-04")
                .jsonPath("$[0].matchStatus").isEqualTo("FT")
                .jsonPath("$[0].homeTeamHasWon").isEqualTo(true)
                .jsonPath("$[0].homeTeamHasLost").isEqualTo(false)
                .jsonPath("$[0].round").isEqualTo(1)
                .jsonPath("$[0].isCurrent").isEqualTo(false);
    }
}
