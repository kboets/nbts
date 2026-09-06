package boets.be.nbts.leagues.web;

import boets.be.nbts.leagues.domain.CountryService;
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
public class CountryController {

    private final CountryService countryService;
    private final LeagueService leagueService;

    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getCountries() {
        log.info("Getting all the persisted countries");
        return ResponseEntity.ok(countryService.getCountries());
    }

    @GetMapping("/countries/selected")
    public ResponseEntity<List<Country>> getSelectedCountries() {
        log.info("Getting all the selected countries");
        List<String> selectedCountryCodes = leagueService.getSelectedLeaguesCountryCodes();
        List<Country> selectedCountries = countryService.getCountries().stream()
                .filter(country -> selectedCountryCodes.contains(country.countryCode()))
                .toList();
        return ResponseEntity.ok(selectedCountries);
    }

}
