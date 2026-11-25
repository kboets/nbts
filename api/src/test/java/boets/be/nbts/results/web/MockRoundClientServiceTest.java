package boets.be.nbts.results.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig({MockRoundClientService.class, RoundClientService.class, MockRoundClientServiceTest.TestConfig.class})
@ActiveProfiles("test")
class MockRoundClientServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        JsonMapper jsonMapper() {
            return new JsonMapper();
        }
    }

    @Autowired
    private RoundClient roundClient;

    @Test
    void getRoundByLeagueAndSeason_shouldReturnPredefinedRound() {
        int round = roundClient.getRoundByLeagueAndSeason(144, 2026);
        assertEquals(11, round);
    }

}
