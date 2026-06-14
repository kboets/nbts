package boets.be.nbts.leagues.web;

import boets.be.nbts.leagues.domain.LeagueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

}
