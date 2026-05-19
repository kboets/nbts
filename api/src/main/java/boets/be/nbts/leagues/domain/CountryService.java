package boets.be.nbts.leagues.domain;

import boets.be.nbts.leagues.web.CountryClientService;
import boets.be.nbts.leagues.web.CountryResponse;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retrieves the countries from the country client service on startup. Loads the result in the database.
 *
 */
@Service
@AllArgsConstructor
public class CountryService {

    private final static List<String> allowedRegions;

    private final CountryClientService countryClientService;
    private final CountryRepository countryRepository;



    static {
        allowedRegions = List.of("Europe");
    }

    @EventListener(ApplicationReadyEvent.class)
    public List<CountryEntity> loadCountries() {
        // check if all regions are loaded in the database
        for (String region : allowedRegions) {
            if (countryRepository.findByRegion(region).isEmpty()) {
                List<CountryResponse> countriesByRegion = countryClientService.getCountriesByRegion(region);
                // map to countryEntity
                // save to database
            }
            continue;

        }
        return null;
    }

}
