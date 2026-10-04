package boets.be.nbts.forecasts.domain;

import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.forecasts.domain.calculator.CalculationContext;
import boets.be.nbts.leagues.LeagueForecastApi;
import boets.be.nbts.results.ResultForecastApi;
import boets.be.nbts.results.domain.models.Result;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Mock
    private LeagueForecastApi leagueForecastApi;

    @Mock
    private ResultForecastApi resultForecastApi;

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
    void prepareDataForForecasting_shouldReturnCalculationContextsForTeamsWithNextMatchAndLastResults() {
        // Round 1: TeamA vs TeamB, TeamC vs TeamD
        // Total 4 teams
        Result r1_1 = createResult(1, "TeamA", "TeamB", 1, LocalDate.of(2026, 1, 1), false);
        Result r1_2 = createResult(2, "TeamC", "TeamD", 1, LocalDate.of(2026, 1, 2), false);

        // Round 2
        Result r2_1 = createResult(3, "TeamA", "TeamC", 2, LocalDate.of(2026, 1, 8), false);
        Result r2_2 = createResult(4, "TeamB", "TeamD", 2, LocalDate.of(2026, 1, 9), false);

        // Round 3
        Result r3_1 = createResult(5, "TeamD", "TeamA", 3, LocalDate.of(2026, 1, 15), false);
        Result r3_2 = createResult(6, "TeamB", "TeamC", 3, LocalDate.of(2026, 1, 16), false);

        // Round 4
        Result r4_1 = createResult(7, "TeamB", "TeamA", 4, LocalDate.of(2026, 1, 22), false);
        Result r4_2 = createResult(8, "TeamD", "TeamC", 4, LocalDate.of(2026, 1, 23), false);

        // Round 5
        Result r5_1 = createResult(9, "TeamC", "TeamA", 5, LocalDate.of(2026, 1, 29), false);
        Result r5_2 = createResult(10, "TeamD", "TeamB", 5, LocalDate.of(2026, 1, 30), false);

        // Round 6
        Result r6_1 = createResult(11, "TeamA", "TeamD", 6, LocalDate.of(2026, 2, 5), false);
        Result r6_2 = createResult(12, "TeamC", "TeamB", 6, LocalDate.of(2026, 2, 6), false);

        // Round 7 (Current Round)
        Result r7_1 = createResult(13, "TeamA", "TeamB", 7, LocalDate.of(2026, 2, 12), true);
        Result r7_2 = createResult(14, "TeamC", "TeamD", 7, LocalDate.of(2026, 2, 13), false);

        // Round 8 (Next Round)
        Result r8_1 = createResult(15, "TeamA", "TeamC", 8, LocalDate.of(2026, 2, 19), false);
        Result r8_2 = createResult(16, "TeamB", "TeamD", 8, LocalDate.of(2026, 2, 20), false);

        List<Result> allResults = List.of(
                r1_1, r1_2, r2_1, r2_2, r3_1, r3_2, r4_1, r4_2, r5_1, r5_2, r6_1, r6_2, r7_1, r7_2, r8_1, r8_2
        );
        ForecastRawData rawData = new ForecastRawData(null, allResults, Collections.emptyList());

        List<CalculationContext> contexts = forecastService.prepareDataForForecasting(rawData);

        assertThat(contexts).hasSize(4);

        // Check TeamA context
        CalculationContext contextA = contexts.stream()
                .filter(c -> c.team().equals("TeamA"))
                .findFirst()
                .orElseThrow();
        assertThat(contextA.totalTeams()).isEqualTo(4);
        assertThat(contextA.team()).isEqualTo("TeamA");
        assertThat(contextA.opponent()).isEqualTo("TeamC"); // in nextMatch r8_1: TeamA vs TeamC
        assertThat(contextA.nextMatch()).isEqualTo(r8_1);
        assertThat(contextA.teamScore()).isEqualTo(0);
        assertThat(contextA.opponentScore()).isEqualTo(0);
        // TeamA played in r1_1, r2_1, r3_1, r4_1, r5_1, r6_1, r7_1, r8_1 (8 matches in total list).
        // Descending date sorted limit 6: r8_1, r7_1, r6_1, r5_1, r4_1, r3_1
        assertThat(contextA.previousResults()).containsExactly(r8_1, r7_1, r6_1, r5_1, r4_1, r3_1);

        // Check TeamC context where TeamC is awayTeam in next match r8_1
        CalculationContext contextC = contexts.stream()
                .filter(c -> c.team().equals("TeamC"))
                .findFirst()
                .orElseThrow();
        assertThat(contextC.totalTeams()).isEqualTo(4);
        assertThat(contextC.team()).isEqualTo("TeamC");
        assertThat(contextC.opponent()).isEqualTo("TeamA");
        assertThat(contextC.nextMatch()).isEqualTo(r8_1);
        // TeamC played in r1_2, r2_1, r3_2, r4_2, r5_1, r6_2, r7_2, r8_1.
        // Descending date sorted limit 6: r8_1, r7_2, r6_2, r5_1, r4_2, r3_2
        assertThat(contextC.previousResults()).containsExactly(r8_1, r7_2, r6_2, r5_1, r4_2, r3_2);
    }

    @Test
    void prepareDataForForecasting_shouldHandleWhenNextRoundDoesNotExist() {
        Result r1_1 = createResult(1, "TeamA", "TeamB", 1, LocalDate.of(2026, 1, 1), true);
        ForecastRawData rawData = new ForecastRawData(null, List.of(r1_1), Collections.emptyList());

        List<CalculationContext> contexts = forecastService.prepareDataForForecasting(rawData);

        assertThat(contexts).hasSize(2);

        CalculationContext contextA = contexts.stream()
                .filter(c -> c.team().equals("TeamA"))
                .findFirst()
                .orElseThrow();
        assertThat(contextA.totalTeams()).isEqualTo(2);
        assertThat(contextA.nextMatch()).isNull();
        assertThat(contextA.opponent()).isNull();
        assertThat(contextA.previousResults()).containsExactly(r1_1);

        CalculationContext contextB = contexts.stream()
                .filter(c -> c.team().equals("TeamB"))
                .findFirst()
                .orElseThrow();
        assertThat(contextB.totalTeams()).isEqualTo(2);
        assertThat(contextB.nextMatch()).isNull();
        assertThat(contextB.opponent()).isNull();
        assertThat(contextB.previousResults()).containsExactly(r1_1);
    }

    @Test
    void prepareDataForForecasting_shouldLimitResultsToMaxSix() {
        Result r1 = createResult(1, "TeamA", "TeamB", 1, LocalDate.of(2026, 1, 1), false);
        Result r2 = createResult(2, "TeamA", "TeamB", 2, LocalDate.of(2026, 1, 2), false);
        Result r3 = createResult(3, "TeamA", "TeamB", 3, LocalDate.of(2026, 1, 3), false);
        Result r4 = createResult(4, "TeamA", "TeamB", 4, LocalDate.of(2026, 1, 4), false);
        Result r5 = createResult(5, "TeamA", "TeamB", 5, LocalDate.of(2026, 1, 5), false);
        Result r6 = createResult(6, "TeamA", "TeamB", 6, LocalDate.of(2026, 1, 6), false);
        Result r7 = createResult(7, "TeamA", "TeamB", 7, LocalDate.of(2026, 1, 7), true);
        Result r8 = createResult(8, "TeamA", "TeamB", 8, LocalDate.of(2026, 1, 8), false);

        ForecastRawData rawData = new ForecastRawData(null, List.of(r1, r2, r3, r4, r5, r6, r7, r8), Collections.emptyList());

        List<CalculationContext> contexts = forecastService.prepareDataForForecasting(rawData);

        CalculationContext contextA = contexts.stream()
                .filter(c -> c.team().equals("TeamA"))
                .findFirst()
                .orElseThrow();

        assertThat(contextA.previousResults()).hasSize(6);
        assertThat(contextA.previousResults()).containsExactly(r8, r7, r6, r5, r4, r3);
    }

    @Test
    void hasEnoughResultsForForecasting_shouldReturnFalseWhenCurrentRoundIsMissing() {
        boolean hasEnoughResults = hasEnoughResultsForForecasting(List.of(
                result(5, false),
                result(6, false)
        ));

        assertThat(hasEnoughResults).isFalse();
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

    private List<String> loggedMessages() {
        return logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    private boolean hasEnoughResultsForForecasting(List<Result> results) {
        return ReflectionTestUtils.invokeMethod(
                forecastService,
                "hasEnoughResultsForForecasting",
                results
        );
    }

    @SuppressWarnings("unchecked")
    private Optional<Result> getNextRound(List<Result> results) {
        return (Optional<Result>) ReflectionTestUtils.invokeMethod(
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
}
