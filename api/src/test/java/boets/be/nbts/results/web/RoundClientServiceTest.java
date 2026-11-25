package boets.be.nbts.results.web;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.admin.domain.models.ApiCounter;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(RoundClientService.class)
@Import(RoundClientServiceTest.TestConfig.class)
class RoundClientServiceTest {

    @Value("classpath:/boets/be/nbts/round/roundsByLeagueAndSeason.json")
    private Resource roundResource;

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
    private RoundClientService roundClientService;

    @Test
    public void testGetRoundsByLeagueAndSeason() {
        server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/fixtures/rounds")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("league", "144"))
                .andExpect(queryParam("current", "true"))
                .andExpect(queryParam("season", "2026"))
                .andRespond(withSuccess(roundResource, MediaType.APPLICATION_JSON));

        int round = roundClientService.getRoundByLeagueAndSeason(144, 2026);
        assertThat(round).isEqualTo(11);
    }
}
