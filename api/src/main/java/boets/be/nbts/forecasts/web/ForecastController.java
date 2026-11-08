package boets.be.nbts.forecasts.web;

import boets.be.nbts.forecasts.domain.ForecastService;
import boets.be.nbts.forecasts.domain.models.Forecast;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping("/forecasts/{leagueId}/{round}/{season}")
    public ResponseEntity<List<Forecast>> getForecasts(@PathVariable int leagueId, @PathVariable int round, @PathVariable int season) {
        log.info("Getting forecasts for leagueId: {}, round: {}, season: {}", leagueId, round, season);
        List<Forecast> forecasts = forecastService.getForecasts(leagueId, round, season);
        return ResponseEntity.ok(forecasts);
    }


}
