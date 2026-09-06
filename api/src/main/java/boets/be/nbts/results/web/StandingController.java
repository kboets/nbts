package boets.be.nbts.results.web;

import boets.be.nbts.results.domain.StandingService;
import boets.be.nbts.results.domain.models.Standing;
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
public class StandingController {

    private final StandingService standingService;

    @GetMapping("/standings/{leagueId}/{season}")
    public ResponseEntity<List<Standing>> getStandingsByLeagueAndSeason(@PathVariable int leagueId, @PathVariable int season) {
        log.info("Fetching standings for league {} and season {}", leagueId, season);
        var standings = standingService.getStandingsByLeagueAndSeason(leagueId, season);
        return ResponseEntity.ok(standings);
    }


}
