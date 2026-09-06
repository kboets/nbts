package boets.be.nbts.results.domain;

import boets.be.nbts.config.NbtsCacheConfiguration;
import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.web.ResultClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@ContextConfiguration(classes = {ResultService.class, NbtsCacheConfiguration.class})
class ResultServiceTest {

    @Autowired
    private ResultService resultService;

    @MockitoBean
    private ResultClientService resultClientService;

    @Test
    void getResultsByLeagueAndSeason_shouldUseCacheForSameLeagueAndSeason() {
        List<Result> expectedResults = List.of(new Result(
                1494118,
                "Allsvenskan",
                "Hammarby FF",
                "Mjallby AIF",
                3,
                0,
                "2026-04-04",
                "FT",
                true,
                false,
                1,
                true
        ));
        when(resultClientService.getResultsByLeagueAndSeason(113, 2026)).thenReturn(expectedResults);

        List<Result> firstCall = resultService.getResultsByLeagueAndSeason(113, 2026);
        List<Result> secondCall = resultService.getResultsByLeagueAndSeason(113, 2026);

        assertThat(secondCall).isEqualTo(firstCall);
        verify(resultClientService, times(1)).getResultsByLeagueAndSeason(113, 2026);
    }
}
