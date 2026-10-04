package boets.be.nbts.datacollector.forecast;

import boets.be.nbts.datacollector.models.ForecastCollectedEvent;
import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.leagues.LeagueForecastApi;
import boets.be.nbts.results.ResultForecastApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataIngestionService {

    private final LeagueForecastApi leagueForecastApi;
    private final ResultForecastApi resultForecastApi;
    private final ApplicationEventPublisher eventPublisher;

    public void triggerDataIngestion() {
        log.info("[FORECASTS] Triggering data ingestion");
        var selectedLeagues = leagueForecastApi.getSelectedLeaguesForForecasting();

        var futures = selectedLeagues.stream()
                .map(league -> CompletableFuture.supplyAsync(() -> {
                    var results = resultForecastApi.getResultsForForecasting(league.leagueId(), league.season());
                    var standings = resultForecastApi.getStandingsForForecasting(league.leagueId(), league.season());
                    return new ForecastRawData(league, results, standings);
                 })
                ).toList();

        // Wait for all external calls to finish
        var rawDataList = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // Publish event across Spring Modulith boundary
        eventPublisher.publishEvent(new ForecastCollectedEvent(rawDataList));
    }

}
