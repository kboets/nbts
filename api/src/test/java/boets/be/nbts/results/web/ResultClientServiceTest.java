package boets.be.nbts.results.web;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.admin.domain.models.ApiCounter;
import boets.be.nbts.results.domain.models.Result;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ResultClientService.class)
@Import(ResultClientServiceTest.TestConfig.class)
class ResultClientServiceTest {

    @Value("classpath:/boets/be/nbts/results/web/resultsByLeagueAndSeason.json")
    private Resource oneResultsResource;

    @Value("classpath:/boets/be/nbts/results/web/allResultsByLeagueAndSeason.json")
    private Resource allResultsResource;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AdminService adminService() {
            AdminService adminService = Mockito.mock(AdminService.class);
            when(adminService.getApiCounter(any(LocalDate.class))).thenReturn(new ApiCounter(0));
            return adminService;
        }
    }

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ResultClientService resultClientService;

    @Test
    void getResultsByLeagueAndSeason_givenLeagueAndSeason_shouldReturnMappedResults() throws Exception {
        String jsonResponse = oneResultsResource.getContentAsString(Charset.defaultCharset());

        server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/fixtures")))
                .andExpect(queryParam("league", "113"))
                .andExpect(queryParam("season", "2026"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<Result> results = resultClientService.getResultsByLeagueAndSeason(113, 2026);

        assertThat(results).hasSize(1);

        Result result = results.getFirst();
        assertThat(result.resultId()).isEqualTo(1494118);
        assertThat(result.leagueName()).isEqualTo("Allsvenskan");
        assertThat(result.homeTeam()).isEqualTo("Hammarby FF");
        assertThat(result.awayTeam()).isEqualTo("Mjallby AIF");
        assertThat(result.homeTeamScore()).isEqualTo(3);
        assertThat(result.awayTeamScore()).isEqualTo(0);
        assertThat(result.matchDate()).isEqualTo("2026-04-04");
        assertThat(result.matchStatus()).isEqualTo("FT");
        assertThat(result.homeTeamHasWon()).isTrue();
        assertThat(result.homeTeamHasLost()).isFalse();
        assertThat(result.round()).isEqualTo(1);
        assertThat(resultClientService.getCallCount()).isEqualTo(1);
    }

    @Test
    void getAllResultsByLeagueAndSeason_givenLeagueAndSeason_shouldReturnMappedResults() throws Exception {
        String jsonResponse = allResultsResource.getContentAsString(Charset.defaultCharset());

        server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/fixtures")))
                .andExpect(queryParam("league", "113"))
                .andExpect(queryParam("season", "2026"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<Result> results = resultClientService.getResultsByLeagueAndSeason(113, 2026);

        // Assert that the results list is not empty and contains the expected number of results
        assertThat(results).isNotEmpty();
        // get result of round 16, it should be the current round
        results.stream().filter(result -> result.round() == 16).findFirst().ifPresent(result -> {
            assertThat(result.isCurrent()).isTrue();
        });

        // get result of round 15, it should not be the current round
        results.stream().filter(result -> result.round() == 15).findFirst().ifPresent(result -> {
            assertThat(result.isCurrent()).isFalse();
        });
    }

}
