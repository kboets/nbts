package boets.be.nbts.forecasts.eventhandlers;

import boets.be.nbts.CleanFlywayTestConfiguration;
import boets.be.nbts.TestcontainersConfiguration;
import boets.be.nbts.datacollector.models.ForecastCollectedEvent;
import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.forecasts.domain.ForecastService;
import boets.be.nbts.forecasts.domain.models.Forecast;
import boets.be.nbts.forecasts.domain.models.ForecastDetail;
import boets.be.nbts.leagues.domain.models.ForecastLeague;
import boets.be.nbts.results.domain.ResultService;
import boets.be.nbts.results.domain.StandingService;
import boets.be.nbts.results.domain.models.Result;
import boets.be.nbts.results.domain.models.Standing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

@ApplicationModuleTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        extraIncludes = {"config", "forecast", "results"})
@Import({
        CleanFlywayTestConfiguration.class,
        TestcontainersConfiguration.class
})
@Testcontainers
@ActiveProfiles("test")
class ForecastEventHandlerIT {

    @Autowired
    private ResultService resultService;

    @Autowired
    private StandingService standingService;

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private ForecastEventHandler forecastEventHandler;

    @BeforeEach
    void setup() {
        ForecastCollectedEvent forecastCollectedEvent = setupForecastCollectedEvent();
        forecastEventHandler.onForecastDataCollected(forecastCollectedEvent);
    }


    @Test
    public void testForecastEventHandler_givenJupilerLeague() {
        // @ApplicationModuleListener runs asynchronously (even when invoked on the Spring bean)
        await().atMost(Duration.ofSeconds(15))
                .until(() -> !forecastService.getForecasts(144, 11, 2026).isEmpty());

        List<Forecast> forecasts = forecastService.getForecasts(144, 11, 2026);
        assertThat(forecasts).isNotEmpty();
        Forecast belgianJupilerForecast = forecasts.getFirst();
        //check AA Gent
        Optional<ForecastDetail> gentOptional = belgianJupilerForecast.forecastDetails().stream()
                .filter(forecastDetail -> forecastDetail.team().equals("Gent"))
                .findFirst();
        assertThat(gentOptional).isPresent();
        ForecastDetail gentForecastDetail = gentOptional.get();
        assertThat(gentForecastDetail.opponent()).isEqualTo("Kortrijk");
        assertThat(gentForecastDetail.totalPoints()).isEqualTo(165);
    }


    private ForecastCollectedEvent setupForecastCollectedEvent() {
        ForecastLeague forecastLeague = new ForecastLeague(144, "Jupiler Pro League", 2026, "BE");
        List<Result> resultsByLeagueAndSeason = resultService.getResultsByLeagueAndSeason(144, 2026);
        List<Standing> standingsByLeagueAndSeason = standingService.getStandingsByLeagueAndSeason(144, 2026);
        ForecastRawData forecastRawData = new ForecastRawData(forecastLeague, resultsByLeagueAndSeason, standingsByLeagueAndSeason, 10);
        return new ForecastCollectedEvent(List.of(forecastRawData));
    }
}
