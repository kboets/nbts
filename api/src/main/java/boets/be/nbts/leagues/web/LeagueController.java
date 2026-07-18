package boets.be.nbts.leagues.web;

import boets.be.nbts.leagues.domain.LeagueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class LeagueController {

    private final LeagueService leagueService;

    @GetMapping("/currentLeagues")
    public ResponseEntity<List<League>> getSelectedLeagues() {
        log.info("Getting all the persisted leagues");
        return ResponseEntity.ok(leagueService.getSelectedLeagues());
    }

    @GetMapping("/currentLeagues/{countryCode}")
    public ResponseEntity<List<League>> getCurrentLeaguesForCountry(@PathVariable String countryCode) {
        log.info("Getting new leagues for country {}", countryCode);
        List<League> leagues = leagueService.getCurrentLeaguesForCountry(countryCode);
        return ResponseEntity.ok(leagues);
    }

    @GetMapping("/currentLeagues/selected/{countryCode}")
    public ResponseEntity<List<League>> getCurrentSelectedLeaguesForCountry(@PathVariable String countryCode) {
        return ResponseEntity.ok(leagueService.getCurrentSelectedLeaguesForCountry(countryCode));
    }

    @PostMapping("/league")
    public ResponseEntity<League> save(@RequestBody League league) {
        League savedLeague = leagueService.save(league);
        return ResponseEntity.ok(savedLeague);
    }

    @DeleteMapping("/league")
    public ResponseEntity<Boolean> delete(@RequestBody League league) {
        boolean deleted = leagueService.delete(league);
        return ResponseEntity.ok(deleted);
    }

}
