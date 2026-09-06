package boets.be.nbts.results.web;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.admin.domain.models.ApiCounter;
import boets.be.nbts.results.domain.models.Standing;
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

@RestClientTest(StandingClientService.class)
@Import(StandingClientServiceTest.TestConfig.class)
class StandingClientServiceTest {

    @Value("classpath:/boets/be/nbts/results/web/standingForLeague.json")
    private Resource standingsResource;

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
    private StandingClientService standingClientService;

    @Test
    void getStandingsByLeagueAndSeason_givenLeagueAndSeason_shouldReturnMappedStandings() throws Exception {
        String jsonResponse = standingsResource.getContentAsString(Charset.defaultCharset());

        server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/standings")))
                .andExpect(queryParam("league", "144"))
                .andExpect(queryParam("season", "2026"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<Standing> standings = standingClientService.getStandingsByLeagueAndSeason(144, 2026);

        assertThat(standings).hasSize(18);

        Standing standing = standings.getFirst();
        assertThat(standing.leagueId()).isEqualTo(144);
        assertThat(standing.season()).isEqualTo(2026);
        assertThat(standing.rank()).isEqualTo(1);
        assertThat(standing.teamName()).isEqualTo("Club Brugge KV");
        assertThat(standing.teamId()).isEqualTo(569);
        assertThat(standing.points()).isEqualTo(9);
        assertThat(standing.played()).isEqualTo(3);
        assertThat(standing.won()).isEqualTo(3);
        assertThat(standing.drawn()).isEqualTo(0);
        assertThat(standing.lost()).isEqualTo(0);
        assertThat(standing.lastUpdated()).isEqualTo(LocalDate.of(2026, 8, 29));
        assertThat(standingClientService.getCallCount()).isEqualTo(1);
    }
}
