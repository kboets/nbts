package boets.be.nbts.forecasts.domain;

import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.forecasts.domain.calculator.CalculationContext;
import boets.be.nbts.results.domain.models.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastService {

    /**
     * Check if the results should be forecasted based on the current round and the number of available results.
     * There should be at least 6 finished results available before forecasting, and the current round should not be the last round.
     * @param results - List of results for a specific league
     * @return true if the results should be forecasted, false otherwise
     */
    public boolean shouldResultBeForecasted(List<Result> results) {
        // check if current round is not last round, in that case no forecasts are needed anymore
        Result result = results.getLast();
        if (result == null || result.isCurrent()) {
            return false;
        }
        // check if at least 6 finished results are available before forecasting
        return hasEnoughResultsForForecasting(results);
    }

    public List<CalculationContext> prepareDataForForecasting(ForecastRawData rawData) {
        // get unique teams for each league and prepare the data for forecasting
        Set<String> teams = rawData.results().stream()
                .filter(result -> result.round() == 1)
                .flatMap(result -> Stream.of(result.homeTeam(), result.awayTeam()))
                .collect(Collectors.toSet());
        // get last 6 results for each team and prepare the data for forecasting
        List<CalculationContext> calculationContexts = new ArrayList<>();
        for (var team : teams) {
            var lastResults = rawData.results().stream()
                    .filter(result -> result.homeTeam().equals(team) || result.awayTeam().equals(team))
                    .sorted(Comparator.comparing(Result::matchDate).reversed())
                    .limit(6)
                    .toList();
            var nextMatch = getNextRound(rawData.results()).orElse(null);
            String opponent = nextMatch != null ? (nextMatch.homeTeam().equals(team) ? nextMatch.awayTeam() : nextMatch.homeTeam()) : null;
            CalculationContext calculationContext = new CalculationContext(teams.size(), team, opponent, 0,0 , nextMatch, lastResults, rawData.standings());
            calculationContexts.add(calculationContext);
        }

        return calculationContexts;
    }

    private boolean hasEnoughResultsForForecasting(List<Result> results) {
        var currentRound = results.stream().filter(Result::isCurrent).findFirst();
        return currentRound.filter(result -> result.round() >= 7).isPresent();
    }

    private Optional<Result> getNextRound(List<Result> results) {
        var currentRound = results.stream().filter(Result::isCurrent).findFirst();
        return currentRound.flatMap(result -> results.stream().filter(r -> r.round() == result.round() + 1).findFirst());
    }


}
