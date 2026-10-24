package boets.be.nbts.forecasts.domain;

import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.forecasts.domain.calculator.CalculationContext;
import boets.be.nbts.forecasts.domain.models.Forecast;
import boets.be.nbts.forecasts.domain.models.ForecastRound;
import boets.be.nbts.results.domain.models.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class ForecastService {

    private final ConcurrentHashMap<ForecastRound, List<Forecast>> forecastCache = new ConcurrentHashMap<>();


    public List<Forecast> getForecasts(int leagueId, int round, int season) {
        ForecastRound forecastRound = new ForecastRound(round, leagueId, season);
        return forecastCache.get(forecastRound);
    }

    public void saveForecastCache(ForecastRound forecastRound, List<Forecast> forecasts) {
        forecastCache.put(forecastRound, forecasts);
    }

    /**
     * Check if the forecast for the given league and round has already been calculated and stored in the cache.
     * @param rawData -
     * @return true if the forecast has already been calculated, false otherwise
     */
    public boolean isForecastAlreadyCalculated(ForecastRawData rawData) {
        Map<ForecastRound, List<Forecast>> forecastsCache = getForecastCache();
        Set<ForecastRound> forecastRounds = forecastsCache.keySet();
        Optional<ForecastRound> optionalForecastRound = forecastRounds.stream()
                .filter(forecastRound -> forecastRound.leagueId() == rawData.league().leagueId())
                .filter(forecastRound -> forecastRound.round()  == rawData.currentRound()+1)
                .filter(forecastRound -> forecastRound.season() == rawData.league().season())
                .findFirst();
        return optionalForecastRound.isPresent();
    }

    /**
     * Check if the results should be forecasted based on the current round and the number of available results.
     * There should be at least 6 finished results available before forecasting, and the current round should not be the last round.
     * @param results - List of results for a specific league
     * @return true if the results should be forecasted, false otherwise
     */
    public boolean canForecastResults(List<Result> results) {
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
                .flatMap(result -> Stream.of(result.homeTeam(), result.awayTeam()))
                .collect(Collectors.toSet());
        // get last 6 results for each team and prepare the data for forecasting
        List<CalculationContext> calculationContexts = new ArrayList<>();
        for (var team : teams) {
            var lastResults = rawData.results().stream()
                    .filter(result -> result.homeTeam().equals(team) || result.awayTeam().equals(team))
                    .filter(result -> result.round() <= rawData.currentRound())
                    .sorted(Comparator.comparing(Result::matchDate).reversed())
                    .limit(6)
                    .toList();
            var nextMatch = rawData.results().stream()
                    .filter(result -> result.round() == rawData.currentRound() + 1)
                    .filter(result -> result.homeTeam().equals(team) || result.awayTeam().equals(team))
                    .findFirst()
                    .orElse(null);
            String opponent = nextMatch != null ? (nextMatch.homeTeam().equals(team) ? nextMatch.awayTeam() : nextMatch.homeTeam()) : null;
            //int teamScore = nextMatch != null ? (nextMatch.homeTeam().equals(team) ? nextMatch.homeTeamScore() : nextMatch.awayTeamScore()) : 0;
            int teamScore = 0;
            //int opponentScore = nextMatch != null ? (nextMatch.homeTeam().equals(team) ? nextMatch.awayTeamScore() : nextMatch.homeTeamScore()) : 0;
            int opponentScore = 0;

            if (lastResults.size() >= 6) {
                CalculationContext calculationContext = new CalculationContext(teams.size(), team, opponent, teamScore, opponentScore, nextMatch, lastResults, rawData.standings());
                calculationContexts.add(calculationContext);
            }
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
