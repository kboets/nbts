package boets.be.nbts.forecasts.domain;

import boets.be.nbts.leagues.LeagueForecastApi;
import boets.be.nbts.results.ResultForecastApi;
import boets.be.nbts.results.domain.models.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastService {

    private final LeagueForecastApi leagueForecastApi;
    private final ResultForecastApi resultForecastApi;

    // calculate forecasts
    public void triggerForecasts() {
        log.info("Triggering forecasts");
        //get all selected leagues
        var selectedLeagues = leagueForecastApi.getSelectedLeaguesForForecasting();
        Map<String, List<Result>> resultsForLeagues = new HashMap<>();

        for (var league : selectedLeagues) {
            var results = resultForecastApi.getResultsForForecasting(
                    league.leagueId(),
                    league.season()
            );
            if (!results.isEmpty()) {
                // get latest results for each league
                Result lastRoundResult = results.getLast();
                // check if current = last round, in that case no forecasts are needed anymore
                if (!lastRoundResult.isCurrent()) {
                    resultsForLeagues.put(league.name(), results);
                }
            }
        }
        calculateForecasts(resultsForLeagues);
    }

    //
    private void calculateForecasts(Map<String, List<Result>> resultsForLeagues) {
        log.info("Calculating forecasts for leagues: {}", resultsForLeagues.keySet());
        // check if next result has round number higher as 7, otherwise no forecasts are needed
        for (var league : resultsForLeagues.keySet()) {
            var results = resultsForLeagues.get(league);
            // get current round
            var currentRound = results.stream().filter(Result::isCurrent).findFirst();

        }

    }


}
