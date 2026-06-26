package boets.be.nbts.leagues.web;

import boets.be.nbts.leagues.domain.LeagueService;
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
public class LeagueController {

    //private final LeagueClientService leagueClientService;
    private final LeagueService leagueService;

    @GetMapping("/currentLeagues")
    public ResponseEntity<List<League>> getSelectedLeagues() {
        log.info("Getting leagues");
        return ResponseEntity.ok(leagueService.getSelectedLeagues());
    }

    @GetMapping("/currentLeagues/{countryCode}")
    public ResponseEntity<List<League>> getCurrentLeaguesForCountry(@PathVariable String countryCode) {
        return ResponseEntity.ok(leagueService.getCurrentLeaguesForCountry(countryCode));
    }

    @GetMapping("/currentLeagues/selected/{countryCode}")
    public ResponseEntity<List<League>> getCurrentSelectedLeaguesForCountry(@PathVariable String countryCode) {
        return ResponseEntity.ok(leagueService.getCurrentSelectedLeaguesForCountry(countryCode));
    }
}
