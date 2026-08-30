package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.models.Standing;
import org.junit.jupiter.api.DisplayName;
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

@WebMvcTest(StandingController.class)
@AutoConfigureRestTestClient
class StandingControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private StandingClientService standingClientService;

    @Test
    @DisplayName("GET /api/standings/{leagueId}/{season} - should return standings for the given league and season")
    void getStandingsByLeagueAndSeason_shouldReturnStandings() {
        int leagueId = 144;
        int season = 2026;
        Standing standing = new Standing(144, 2026, 1, "Club Brugge KV", 569, 9, 3, 3, 0, 0, LocalDate.of(2026, 8, 29));

        when(standingClientService.getStandingsByLeagueAndSeason(leagueId, season)).thenReturn(List.of(standing));

        restTestClient.get().uri("/api/standings/{leagueId}/{season}", leagueId, season)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$[0].leagueId").isEqualTo(144)
                .jsonPath("$[0].season").isEqualTo(2026)
                .jsonPath("$[0].rank").isEqualTo(1)
                .jsonPath("$[0].teamName").isEqualTo("Club Brugge KV")
                .jsonPath("$[0].teamId").isEqualTo(569)
                .jsonPath("$[0].points").isEqualTo(9)
                .jsonPath("$[0].played").isEqualTo(3)
                .jsonPath("$[0].won").isEqualTo(3)
                .jsonPath("$[0].drawn").isEqualTo(0)
                .jsonPath("$[0].lost").isEqualTo(0)
                .jsonPath("$[0].lastUpdated").isEqualTo("2026-08-29");
    }
}
