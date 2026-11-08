package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.models.Standing;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringJUnitConfig({MockStandingClientService.class, StandingClientService.class})
@ActiveProfiles("test")
class MockStandingClientServiceTest {

    @Autowired
    private MockStandingClientService mockStandingClientService;

    @Test
    void getStandingsByLeagueAndSeason() {
        List<Standing> standingsByLeagueAndSeason = mockStandingClientService.getStandingsByLeagueAndSeason(1, 2026);
        assertThat(standingsByLeagueAndSeason).isNotNull();
        assertThat(standingsByLeagueAndSeason.size()).isEqualTo(18);
        Standing firstStanding = standingsByLeagueAndSeason.getFirst();
        assertThat(firstStanding.teamName()).isEqualTo("Club Brugge KV");
        assertThat(firstStanding.rank()).isEqualTo(1);
    }
}
