package boets.be.nbts.leagues.domain;

import boets.be.nbts.leagues.web.CountryClientService;
import boets.be.nbts.leagues.web.CountryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryClientService countryClientService;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryService countryService;

    @Test
    void loadCountries_savesCountriesWhenRegionIsNotLoadedYet() {
        when(countryRepository.findByRegion("Europe")).thenReturn(List.of());

        CountryResponse belgium = mock(CountryResponse.class, RETURNS_DEEP_STUBS);
        when(belgium.cca2()).thenReturn("BE");
        when(belgium.name().common()).thenReturn("Belgium");
        when(belgium.translations().nld().common()).thenReturn("België");
        when(belgium.flags().png()).thenReturn("https://flagcdn.com/w320/be.png");

        when(countryClientService.getCountriesByRegion("Europe"))
                .thenReturn(List.of(belgium));

        countryService.loadCountries();

        ArgumentCaptor<List<CountryEntity>> captor = ArgumentCaptor.forClass(List.class);

        verify(countryRepository).saveAll(captor.capture());

        List<CountryEntity> savedCountries = captor.getValue();

        assertThat(savedCountries).hasSize(1);
        assertThat(savedCountries.getFirst().getCountryCode()).isEqualTo("BE");
        assertThat(savedCountries.getFirst().getName()).isEqualTo("Belgium");
        assertThat(savedCountries.getFirst().getDutchName()).isEqualTo("België");
        assertThat(savedCountries.getFirst().getFlagUrl()).isEqualTo("https://flagcdn.com/w320/be.png");
    }

    @Test
    void loadCountries_doesNothingWhenRegionAlreadyExists() {
        CountryEntity existingCountry = new CountryEntity();
        existingCountry.setCountryCode("BE");
        existingCountry.setName("Belgium");

        when(countryRepository.findByRegion("Europe"))
                .thenReturn(List.of(existingCountry));

        countryService.loadCountries();

        verify(countryClientService, never()).getCountriesByRegion(anyString());
        verify(countryRepository, never()).saveAll(any());
    }

}
