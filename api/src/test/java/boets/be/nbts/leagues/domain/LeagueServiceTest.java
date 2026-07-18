package boets.be.nbts.leagues.domain;

import boets.be.nbts.leagues.web.League;
import boets.be.nbts.leagues.web.LeagueClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueServiceTest {

    @Mock
    private LeagueClientService leagueClientService;

    @Mock
    private LeagueRepository leagueRepository;

    @InjectMocks
    private LeagueService leagueService;

    private League league1;
    private League league2;

    @BeforeEach
    void setUp() {
        league1 = League.builder()
                .leagueId(1)
                .name("Jupiler Pro League")
                .countryCode("BE")
                .season(2024)
                .start(LocalDate.of(2024, 7, 1))
                .end(LocalDate.of(2025, 6, 30))
                .current(true)
                .build();

        league2 = League.builder()
                .leagueId(2)
                .name("Challenger Pro League")
                .countryCode("BE")
                .season(2024)
                .start(LocalDate.of(2024, 7, 1))
                .end(LocalDate.of(2025, 6, 30))
                .current(true)
                .build();
    }

    @Test
    void getCurrentLeaguesForCountry_shouldReturnAllLeaguesWhenNoneInDb() {
        String countryCode = "BE";
        when(leagueClientService.getLeaguesByCountry(countryCode)).thenReturn(List.of(league1, league2));
        when(leagueRepository.findByCountryCode(countryCode)).thenReturn(List.of());

        List<League> result = leagueService.getCurrentLeaguesForCountry(countryCode);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(league1, league2);
    }

    @Test
    void getCurrentLeaguesForCountry_shouldFilterOutExistingLeagues() {
        String countryCode = "BE";
        LeagueEntity leagueEntity1 = new LeagueEntity();
        leagueEntity1.setLeagueId(1);
        leagueEntity1.setCountryCode(countryCode);

        when(leagueClientService.getLeaguesByCountry(countryCode)).thenReturn(List.of(league1, league2));
        when(leagueRepository.findByCountryCode(countryCode)).thenReturn(List.of(leagueEntity1));

        List<League> result = leagueService.getCurrentLeaguesForCountry(countryCode);

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(league2);
    }

    @Test
    void getCurrentLeaguesForCountry_shouldReturnEmptyListWhenNoLeaguesFound() {
        String countryCode = "BE";
        when(leagueClientService.getLeaguesByCountry(countryCode)).thenReturn(List.of());
        when(leagueRepository.findByCountryCode(countryCode)).thenReturn(List.of());

        List<League> result = leagueService.getCurrentLeaguesForCountry(countryCode);

        assertThat(result).isEmpty();
    }

    @Test
    void getSelectedLeagues_shouldReturnEmptyListWhenNoneFound() {
        when(leagueRepository.findByCurrent(true)).thenReturn(List.of());

        List<League> result = leagueService.getSelectedLeagues();

        assertThat(result).isEmpty();
        verify(leagueRepository, never()).saveAll(anyList());
    }

    @Test
    void getSelectedLeagues_shouldReturnLeaguesWhenFoundAndNotExpired() {
        LeagueEntity leagueEntity = new LeagueEntity();
        leagueEntity.setLeagueId(1);
        leagueEntity.setEndSeason(LocalDate.now().plusDays(10));
        leagueEntity.setCurrent(true);

        when(leagueRepository.findByCurrent(true)).thenReturn(List.of(leagueEntity));

        List<League> result = leagueService.getSelectedLeagues();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().leagueId()).isEqualTo(1);
        verify(leagueRepository, never()).saveAll(anyList());
    }

    @Test
    void getSelectedLeagues_shouldRefreshWhenLeaguesExpired() {
        LeagueEntity expiredLeague = new LeagueEntity();
        expiredLeague.setLeagueId(1);
        expiredLeague.setEndSeason(LocalDate.now().minusDays(1));
        expiredLeague.setCurrent(true);

        LeagueEntity activeLeague = new LeagueEntity();
        activeLeague.setLeagueId(2);
        activeLeague.setEndSeason(LocalDate.now().plusDays(10));
        activeLeague.setCurrent(true);

        when(leagueRepository.findByCurrent(true)).thenReturn(List.of(expiredLeague, activeLeague), List.of(activeLeague));

        List<League> result = leagueService.getSelectedLeagues();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().leagueId()).isEqualTo(2);
        assertThat(expiredLeague.isCurrent()).isFalse();
        verify(leagueRepository).saveAll(anyList());
        verify(leagueRepository, times(2)).findByCurrent(true);
    }
}
