package boets.be.nbts.leagues.domain;

import boets.be.nbts.leagues.web.Country;
import boets.be.nbts.leagues.web.CountryClientService;
import boets.be.nbts.leagues.web.CountryResponse;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
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
    public void onApplicationEvent() {
        loadCountries();
    }

    public void loadCountries() {
        // check if all regions are loaded in the database
        for (String region : allowedRegions) {
            if (countryRepository.findByRegion(region).isEmpty()) {
                List<CountryResponse> countriesByRegion = countryClientService.getCountriesByRegion(region);
                List<CountryEntity> countryEntities = new ArrayList<>();
                countriesByRegion.forEach(countryResponse -> countryEntities.add(mapToCountryEntity(countryResponse, region)));
                countryRepository.saveAll(countryEntities);
            }
        }
    }

    public List<Country> getCountries() {
        List<CountryEntity> countryEntities = countryRepository.findByRegion(allowedRegions.getFirst());
        // sort by name
        countryEntities.sort(Comparator.comparing(CountryEntity::getDutchName));
        List<Country> countries = new ArrayList<>();
        countryEntities.forEach(countryEntity -> countries.add(mapToCountry(countryEntity)));
        return countries;
    }

    private CountryEntity mapToCountryEntity(CountryResponse countryResponse, String region) {
        CountryEntity countryEntity = new CountryEntity();
        countryEntity.setCountryCode(countryResponse.cca2());
        countryEntity.setName(countryResponse.name().common());
        countryEntity.setDutchName(countryResponse.translations().nld().common());
        countryEntity.setFlagUrl(countryResponse.flags().png());
        countryEntity.setRegion(region);
        return countryEntity;
    }

    private Country mapToCountry(CountryEntity countryEntity) {
        return new Country(countryEntity.getId(), countryEntity.getCountryCode(), countryEntity.getDutchName(), countryEntity.getName(), countryEntity.getFlagUrl(), countryEntity.getRegion());
    }

}
