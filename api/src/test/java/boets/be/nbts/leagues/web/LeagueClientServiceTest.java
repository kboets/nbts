package boets.be.nbts.leagues.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.nio.charset.Charset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(LeagueClientService.class)
public class LeagueClientServiceTest {

    @Value("classpath:/boets/be/nbts/leagues/web/leagues4country.json")
    private Resource resource;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private LeagueClientService leagueClientService;

    @Test
    public void getLeaguesByCountryAndSeason_givenBEAnd2025_shouldReturnAllLeagues() throws Exception{
        String jsonResponse = resource.getContentAsString(Charset.defaultCharset());

        this.server.expect(requestTo(startsWith("https://api-football-v1.p.rapidapi.com/v3/leagues")))
                .andExpect(queryParam("code", "BE"))
                .andExpect(queryParam("season", "2025"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<League> belgium2025Leagues = leagueClientService.getLeaguesByCountryAndSeason("BE", 2025);
        assertThat(belgium2025Leagues).hasSize(10);

        League firstLeague = belgium2025Leagues.getFirst();
        assertThat(firstLeague.name()).isEqualTo("Jupiler Pro League");
        assertThat(firstLeague.logo()).isEqualTo("https://media.api-sports.io/football/leagues/144.png");
        assertThat(firstLeague.countryCode()).isEqualTo("BE");
        assertThat(firstLeague.season()).isEqualTo(2025);

        int callCount = leagueClientService.getCallCount();
        assertThat(callCount).isEqualTo(1);
    }

}
