package boets.be.nbts.forecasts.domain;

import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.forecasts.domain.models.ForecastRound;
import boets.be.nbts.leagues.domain.models.ForecastLeague;
import boets.be.nbts.results.domain.models.Result;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {


    @InjectMocks
    private ForecastService forecastService;

    private Logger logger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(ForecastService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
        logAppender.stop();
    }


    @Test
    void hasEnoughResultsForForecasting_shouldReturnFalseWhenCurrentRoundIsBelowSeven() {
        boolean hasEnoughResults = hasEnoughResultsForForecasting(List.of(
                result(5, false),
                result(6, true)
        ));

        assertThat(hasEnoughResults).isFalse();
    }

    @Test
    void hasEnoughResultsForForecasting_shouldReturnTrueWhenCurrentRoundIsSeven() {
        boolean hasEnoughResults = hasEnoughResultsForForecasting(List.of(
                result(6, false),
                result(7, true)
        ));

        assertThat(hasEnoughResults).isTrue();
    }

    @Test
    void getNextRound_shouldReturnEmptyWhenFollowingRoundDoesNotExist() {
        Optional<Result> nextRound = getNextRound(List.of(
                result(6, false),
                result(7, true)
        ));

        assertThat(nextRound).isEmpty();
    }

    @Test
    void getNextRound_shouldReturnFollowingRoundWhenPresent() {
        Result expectedNextRound = result(8, false);

        Optional<Result> nextRound = getNextRound(List.of(
                result(6, false),
                result(7, true),
                expectedNextRound
        ));

        assertThat(nextRound).contains(expectedNextRound);
    }

    private boolean hasEnoughResultsForForecasting(List<Result> results) {
        return ReflectionTestUtils.invokeMethod(
                forecastService,
                "hasEnoughResultsForForecasting",
                results
        );
    }

    private Optional<Result> getNextRound(List<Result> results) {
        return ReflectionTestUtils.invokeMethod(
                forecastService,
                "getNextRound",
                results
        );
    }

    private Result result(int round, boolean isCurrent) {
        return createResult(round, "Home " + round, "Away " + round, round, LocalDate.of(2026, 1, round), isCurrent);
    }

    private Result createResult(int id, String homeTeam, String awayTeam, int round, LocalDate matchDate, boolean isCurrent) {
        return new Result(
                id,
                "Allsvenskan",
                homeTeam,
                awayTeam,
                1,
                0,
                matchDate,
                "FT",
                true,
                false,
                round,
                isCurrent
        );
    }

    @Test
    void isForecastAlreadyCalculated_shouldReturnFalseWhenCacheIsEmpty() {
        ForecastLeague league = new ForecastLeague(1, "Allsvenskan", 2026, "SE");
        ForecastRawData rawData = new ForecastRawData(league, Collections.emptyList(), Collections.emptyList(), 7);

        boolean result = forecastService.isForecastAlreadyCalculated(rawData);

        assertThat(result).isFalse();
    }

    @Test
    void isForecastAlreadyCalculated_shouldReturnTrueWhenLeagueAndRoundMatch() {
        int leagueId = 1;
        int currentRound = 7;
        int season = 2026;
        ForecastLeague league = new ForecastLeague(leagueId, "Allsvenskan", season, "SE");
        ForecastRawData rawData = new ForecastRawData(league, Collections.emptyList(), Collections.emptyList(), currentRound);

        // We forecast for the next round
        ForecastRound forecastRound = new ForecastRound(currentRound + 1, leagueId, season);
        forecastService.saveForecastCache(forecastRound, Collections.emptyList());

        boolean result = forecastService.isForecastAlreadyCalculated(rawData);

        assertThat(result).isTrue();
    }

    @Test
    void isForecastAlreadyCalculated_shouldReturnFalseWhenLeagueIdDiffers() {
        int round = 7;
        int season = 2026;

        // Save forecast for league 1
        forecastService.saveForecastCache(new ForecastRound(round, 1, season), Collections.emptyList());

        // Check for league 2
        ForecastLeague league2 = new ForecastLeague(2, "Other", season, "BE");
        ForecastRawData rawData = new ForecastRawData(league2, Collections.emptyList(), Collections.emptyList(), round);

        boolean result = forecastService.isForecastAlreadyCalculated(rawData);

        assertThat(result).isFalse();
    }

    @Test
    void isForecastAlreadyCalculated_shouldReturnFalseWhenRoundDiffers() {
        int leagueId = 1;
        int season = 2026;

        // Save forecast for round 7
        forecastService.saveForecastCache(new ForecastRound(7, leagueId, season), Collections.emptyList());

        // Check for round 8
        ForecastLeague league = new ForecastLeague(leagueId, "Allsvenskan", season, "SE");
        ForecastRawData rawData = new ForecastRawData(league, Collections.emptyList(), Collections.emptyList(), 8);

        boolean result = forecastService.isForecastAlreadyCalculated(rawData);

        assertThat(result).isFalse();
    }

    @Test
    void isForecastAlreadyCalculated_shouldReturnFalseWhenSeasonDiffers() {
        int leagueId = 1;
        int round = 7;

        // Save forecast for season 2025
        forecastService.saveForecastCache(new ForecastRound(round, leagueId, 2025), Collections.emptyList());

        // Check for season 2026
        ForecastLeague league = new ForecastLeague(leagueId, "Allsvenskan", 2026, "SE");
        ForecastRawData rawData = new ForecastRawData(league, Collections.emptyList(), Collections.emptyList(), round);

        boolean result = forecastService.isForecastAlreadyCalculated(rawData);

        assertThat(result).isFalse();
    }
}
