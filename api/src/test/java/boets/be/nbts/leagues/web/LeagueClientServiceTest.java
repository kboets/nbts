package boets.be.nbts.leagues.web;

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

@RestClientTest(LeagueClientService.class)
@Import(LeagueClientServiceTest.TestConfig.class)
public class LeagueClientServiceTest {

    @Value("classpath:/boets/be/nbts/leagues/web/leagues4countryAndSeason.json")
    private Resource resourceCountryAndSeason;

    @Value("classpath:/boets/be/nbts/leagues/web/leagues4CountryNoCurrent.json")
    private Resource resourceCountryNoCurrent;

    @Value("classpath:/boets/be/nbts/leagues/web/leagues4CountryWithCurrent.json")
    private Resource resourceCountryActiveCurrent;

    @Value("classpath:/boets/be/nbts/leagues/web/leaguesWithWomen.json")
    private Resource resourceLeagueWithWomen;

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
    private LeagueClientService leagueClientService;


    @Test
    public void getLeaguesByCountryAndSeason_givenBEAnd2025_shouldReturnAllLeagues() throws Exception{
        String jsonResponse = resourceCountryAndSeason.getContentAsString(Charset.defaultCharset());

        this.server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/leagues")))
                .andExpect(queryParam("code", "BE"))
                .andExpect(queryParam("season", "2025"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<League> belgium2025Leagues = leagueClientService.getLeaguesByCountryAndSeason("BE", 2025);
        assertThat(belgium2025Leagues).hasSize(3);

        League firstLeague = belgium2025Leagues.getFirst();
        assertThat(firstLeague.name()).isEqualTo("Jupiler Pro League");
        assertThat(firstLeague.logo()).isEqualTo("https://media.api-sports.io/football/leagues/144.png");
        assertThat(firstLeague.countryCode()).isEqualTo("BE");
        assertThat(firstLeague.season()).isEqualTo(2025);

        int callCount = leagueClientService.getCallCount();
        assertThat(callCount).isEqualTo(1);
    }

    @Test
    public void getLeaguesByCountry_givenNoActiveCurrent_shouldReturnNoLeagues() throws Exception {
        String jsonResponse = resourceCountryNoCurrent.getContentAsString(Charset.defaultCharset());

        this.server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/leagues")))
                .andExpect(queryParam("code", "BE"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<League> belgiumLeagues = leagueClientService.getLeaguesByCountry("BE");
        assertThat(belgiumLeagues).isEmpty();

        int callCount = leagueClientService.getCallCount();
        assertThat(callCount).isEqualTo(2);
    }

    @Test
    public void getLeaguesByCountry_givenActiveCurrent_shouldLeagues() throws Exception {
        String jsonResponse = resourceCountryActiveCurrent.getContentAsString(Charset.defaultCharset());
        this.server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/leagues")))
                .andExpect(queryParam("code", "NL"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<League> dutchLeagues = leagueClientService.getLeaguesByCountry("NL");
        assertThat(dutchLeagues).hasSize(2);
        assertThat(dutchLeagues.getFirst().name()).isEqualTo("Eredivisie");
        assertThat(dutchLeagues.getFirst().logo()).isEqualTo("https://media.api-sports.io/football/leagues/88.png");
        assertThat(dutchLeagues.getFirst().countryCode()).isEqualTo("NL");
        assertThat(dutchLeagues.getFirst().season()).isEqualTo(2026);

        int callCount = leagueClientService.getCallCount();
        assertThat(callCount).isEqualTo(3);

    }

    @Test
    public void getLeaguesWithWomen_shouldNotReturnLeaguesWithWomen() throws Exception {
        String jsonResponse = resourceLeagueWithWomen.getContentAsString(Charset.defaultCharset());
        this.server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/leagues")))
                .andExpect(queryParam("code", "BE"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
        List<League> belgiumLeagues = leagueClientService.getLeaguesByCountry("BE");
        assertThat(belgiumLeagues).isNotEmpty();

        // league should not contain women leagues or cup
        belgiumLeagues.forEach(league -> {
            assertThat(league.name()).doesNotContain("Women");
            assertThat(league.name()).doesNotContain("Cup");
        });

        //assertThat(callCount).isEqualTo(4);
    }
}
