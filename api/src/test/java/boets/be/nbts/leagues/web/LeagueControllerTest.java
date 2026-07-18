package boets.be.nbts.leagues.web;

import boets.be.nbts.leagues.domain.LeagueService;
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

@WebMvcTest(LeagueController.class)
@AutoConfigureRestTestClient
class LeagueControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private LeagueService leagueService;

    @Test
    @DisplayName( "GET /api/currentLeagues - should return all current leagues")
    void getSelectedLeagues() {
        var leagues = List.of(
                League.builder().leagueId(1).name("Jupiler Pro League").start(LocalDate.of(2025, 7, 25))
                .end(LocalDate.of(2026, 3, 31)).countryCode("BE").season(2025).current(true).build(),
                League.builder().leagueId(2).name("EreDivisie").start(LocalDate.of(2025, 8, 15))
                        .end(LocalDate.of(2026, 5, 18)).countryCode("NL").season(2025).current(true).build()
        );
        when(leagueService.getSelectedLeagues()).thenReturn(leagues);

        restTestClient.get().uri("/api/currentLeagues").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].leagueId").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Jupiler Pro League")
                .jsonPath("$[0].start").isEqualTo("2025-07-25")
                .jsonPath("$[0].end").isEqualTo("2026-03-31")
                .jsonPath("$[0].countryCode").isEqualTo("BE")
                .jsonPath("$[0].season").isEqualTo(2025)
                .jsonPath("$[0].current").isEqualTo(true);
    }

    @Test
    @DisplayName("GET /api/currentLeagues/{countryCode} - should return current leagues for a country")
    void getCurrentLeaguesForCountry() {
        String countryCode = "BE";
        var leagues = List.of(
                League.builder().leagueId(1).name("Jupiler Pro League").start(LocalDate.of(2025, 7, 25))
                        .end(LocalDate.of(2026, 3, 31)).countryCode("BE").season(2025).current(true).build()
        );
        when(leagueService.getCurrentLeaguesForCountry(countryCode)).thenReturn(leagues);

        restTestClient.get().uri("/api/currentLeagues/{countryCode}", countryCode).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].leagueId").isEqualTo(1)
                .jsonPath("$[0].countryCode").isEqualTo("BE");
    }

    @Test
    @DisplayName("GET /api/currentLeagues/{countryCode} - should return empty list when no leagues found")
    void getCurrentLeaguesForCountry_empty() {
        String countryCode = "US";
        when(leagueService.getCurrentLeaguesForCountry(countryCode)).thenReturn(List.of());

        restTestClient.get().uri("/api/currentLeagues/{countryCode}", countryCode).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("GET /api/currentLeagues/selected/{countryCode} - should return selected leagues for a country")
    void getCurrentSelectedLeaguesForCountry() {
        String countryCode = "BE";
        var leagues = List.of(
                League.builder().leagueId(1).name("Jupiler Pro League").start(LocalDate.of(2025, 7, 25))
                        .end(LocalDate.of(2026, 3, 31)).countryCode("BE").season(2025).current(true).build()
        );
        when(leagueService.getCurrentSelectedLeaguesForCountry(countryCode)).thenReturn(leagues);

        restTestClient.get().uri("/api/currentLeagues/selected/{countryCode}", countryCode).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].leagueId").isEqualTo(1)
                .jsonPath("$[0].countryCode").isEqualTo("BE")
                .jsonPath("$[0].current").isEqualTo(true);
    }

    @Test
    @DisplayName("GET /api/currentLeagues/selected/{countryCode} - should return empty list when no selected leagues found")
    void getCurrentSelectedLeaguesForCountry_empty() {
        String countryCode = "US";
        when(leagueService.getCurrentSelectedLeaguesForCountry(countryCode)).thenReturn(List.of());

        restTestClient.get().uri("/api/currentLeagues/selected/{countryCode}", countryCode).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }
}
