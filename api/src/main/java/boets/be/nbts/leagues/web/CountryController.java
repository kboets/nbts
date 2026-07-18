package boets.be.nbts.leagues.web;

import boets.be.nbts.leagues.domain.CountryService;
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

    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getCountries() {
        log.info("Getting all the persisted countries");
        return ResponseEntity.ok(countryService.getCountries());
    }

}
