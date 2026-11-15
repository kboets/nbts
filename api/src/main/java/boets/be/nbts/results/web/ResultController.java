package boets.be.nbts.results.web;

import boets.be.nbts.results.ResultForecastApi;
import boets.be.nbts.results.domain.ResultService;
import boets.be.nbts.results.domain.models.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class ResultController {

    private final ResultService resultService;
    private final ResultForecastApi resultForecastApi;

    @GetMapping("/results/{leagueId}/{season}")
    public ResponseEntity<List<Result>> getResultsByLeagueAndSeason(@PathVariable int leagueId, @PathVariable int season) {
        log.info("Fetching results for league {} and season {}", leagueId, season);
        var results = resultService.getResultsByLeagueAndSeason(leagueId, season);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/currentRound/{leagueId}/{season}")
    public ResponseEntity<Integer> getCurrentRoundForLeagueAndSeason(@PathVariable int leagueId, @PathVariable int season) {
        log.info("Getting current round for league {} and season {}", leagueId, season);
        Optional<Result> currentRound = resultForecastApi.getCurrentRound(leagueId, season);
        return currentRound.map(result -> ResponseEntity.ok(result.round())).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
