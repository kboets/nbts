package boets.be.nbts.datacollector.forecast;

import boets.be.nbts.datacollector.models.ForecastCollectedEvent;
import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.leagues.LeagueForecastApi;
import boets.be.nbts.results.ResultForecastApi;
import boets.be.nbts.results.domain.models.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataIngestionService {

    private final LeagueForecastApi leagueForecastApi;
    private final ResultForecastApi resultForecastApi;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * This method is scheduled to run every 10 minutes and triggers the data ingestion process for forecasting.
     * It performs the following steps:
     * 1. Retrieves all the selected leagues for which the forecasts should be calculated.
     * 2. For each league, retrieves the results and standings for forecasting in parallel using CompletableFuture.
     * 3. Waits for all external calls to finish.
     * 4. Publishes an event across the Spring Modulith boundary with the collected forecast data.
     */
    @Scheduled(cron = "0 */10 * ? * *")
    public void triggerDataIngestion() {
        log.info("[FORECASTS] Triggering data ingestion");
        //1. get the leagues for which the forecasts should be calculated, and filter out the leagues that have already been forecasted
        var selectedLeagues = leagueForecastApi.getSelectedLeaguesForForecasting();
        log.info("[FORECASTS] Number of selected leagues to forecast: {}", selectedLeagues.size());
        //2. For each league, get the results, standings and current round for forecasting in parallel using CompletableFuture
        var futures = selectedLeagues.stream()
                .map(league -> CompletableFuture.supplyAsync(() -> {
                    var results = resultForecastApi.getResultsForForecasting(league.leagueId(), league.season());
                    var standings = resultForecastApi.getStandingsForForecasting(league.leagueId(), league.season());
                    var currentRound = resultForecastApi.getCurrentRound(league.leagueId(), league.season());
                    int currentRoundNumber = currentRound.map(Result::round).orElse(0);
                    return new ForecastRawData(league, results, standings, currentRoundNumber);
                 })
                ).toList();

        //3. Wait for all external calls to finish
        log.info("[FORECASTS] Waiting for all external calls to finish");
        var rawDataList = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        //4. Publish event across Spring Modulith boundary
        log.info("[FORECASTS] Publishing ForecastCollectedEvent with {} raw data entries", rawDataList.size());
        eventPublisher.publishEvent(new ForecastCollectedEvent(rawDataList));
    }

}
