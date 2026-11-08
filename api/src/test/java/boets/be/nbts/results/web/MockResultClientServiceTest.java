package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.models.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig({MockResultClientService.class, ResultClientService.class})
@ActiveProfiles("test")
class MockResultClientServiceTest {

    @Autowired
    private ResultClient resultClient;

    @Test
    void getResultsByLeagueAndSeason_shouldReturnPredefinedResults() {
        List<Result> results = resultClient.getResultsByLeagueAndSeason(144, 2026);
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(306);
        // current round should be 10
        Optional<Result> result = results.stream().filter(Result::isCurrent).findFirst();
        assertThat(result).isPresent();
        assertThat(result.get().round()).isEqualTo(10);
    }
}
