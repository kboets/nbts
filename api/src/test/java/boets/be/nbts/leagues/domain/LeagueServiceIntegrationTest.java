package boets.be.nbts.leagues.domain;

import boets.be.nbts.CleanFlywayTestConfiguration;
import boets.be.nbts.TestcontainersConfiguration;
import boets.be.nbts.leagues.domain.models.LeagueSavedEvent;
import boets.be.nbts.leagues.web.League;
import boets.be.nbts.leagues.web.LeagueClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TestcontainersConfiguration.class,
        CleanFlywayTestConfiguration.class,
        LeagueService.class
})
@Testcontainers
class LeagueServiceIntegrationTest {

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private LeagueRepository leagueRepository;

    @MockitoBean
    private LeagueClientService leagueClientService;

    @Autowired
    private TestEventListener testEventListener;

    @BeforeEach
    void setUp() {
        testEventListener.clear();
    }

    @TestConfiguration
    static class TestEventListenerConfig {
        @Bean
        public TestEventListener testEventListener() {
            return new TestEventListener();
        }
    }

    static class TestEventListener {
        private final List<LeagueSavedEvent> events = new ArrayList<>();

        @EventListener
        public void handle(LeagueSavedEvent event) {
            events.add(event);
        }

        public List<LeagueSavedEvent> getEvents() {
            return events;
        }

        public void clear() {
            events.clear();
        }
    }


    @Test
    @Sql("/db/testdata/insert_test_data.sql")
    @DisplayName("getCurrentLeaguesForCountry - should filter out leagues already saved in the database")
    void getCurrentLeaguesForCountry_shouldFilterOutExistingLeagues() {
        String countryCode = "ES";

        // Mock external API response returning two leagues:
        // - League 2 is already saved in insert_test_data.sql (La Liga, ES)
        // - League 4 is a new league
        League leagueAlreadyInDb = League.builder()
                .leagueId(2)
                .name("La Liga")
                .countryCode(countryCode)
                .season(2025)
                .logo("http:www.premierleague.es")
                .start(LocalDate.of(2025, 8, 10))
                .end(LocalDate.of(2026, 5, 28))
                .current(true)
                .build();

        League newLeague = League.builder()
                .leagueId(4)
                .name("Segunda División")
                .countryCode(countryCode)
                .season(2025)
                .logo("http:www.segunda.es")
                .start(LocalDate.of(2025, 8, 15))
                .end(LocalDate.of(2026, 6, 1))
                .current(true)
                .build();

        when(leagueClientService.getLeaguesByCountry(countryCode)).thenReturn(List.of(leagueAlreadyInDb, newLeague));

        List<League> result = leagueService.getCurrentLeaguesForCountry(countryCode);

        assertThat(result)
                .hasSize(1)
                .containsExactly(newLeague);
    }

    @Test
    @Sql("/db/testdata/insert_test_data.sql")
    @DisplayName("getCurrentLeaguesForCountry - should return all leagues when none are saved in the database")
    void getCurrentLeaguesForCountry_shouldReturnAllLeaguesWhenNoneExist() {
        String countryCode = "BE"; // 'BE' country is in test data, but no leagues with country_code 'BE' are in DB

        League belgianLeague = League.builder()
                .leagueId(10)
                .name("Jupiler Pro League")
                .countryCode(countryCode)
                .season(2025)
                .logo("http:www.jupiler.be")
                .start(LocalDate.of(2025, 7, 25))
                .end(LocalDate.of(2026, 3, 31))
                .current(true)
                .build();

        when(leagueClientService.getLeaguesByCountry(countryCode)).thenReturn(List.of(belgianLeague));

        List<League> result = leagueService.getCurrentLeaguesForCountry(countryCode);

        assertThat(result)
                .hasSize(1)
                .containsExactly(belgianLeague);
    }

    @Test
    @Sql("/db/testdata/insert_test_data.sql")
    @DisplayName("save - should persist new league and publish a LeagueSavedEvent")
    void save_shouldPersistLeagueAndPublishEvent() {
        String countryCode = "BE";
        League newLeague = League.builder()
                .leagueId(10)
                .name("Jupiler Pro League")
                .countryCode(countryCode)
                .season(2025)
                .logo("http:www.jupiler.be")
                .start(LocalDate.of(2025, 7, 25))
                .end(LocalDate.of(2026, 3, 31))
                .current(true)
                .build();

        // 1. Save league via Service
        League savedLeague = leagueService.save(newLeague);

        // 2. Assert it is returned correctly
        assertThat(savedLeague.leagueId()).isEqualTo(10);
        assertThat(savedLeague.name()).isEqualTo("Jupiler Pro League");

        // 3. Assert it is actually persisted in database
        List<LeagueEntity> dbLeagues = leagueRepository.findByCountryCode(countryCode);
        assertThat(dbLeagues).hasSize(1);
        assertThat(dbLeagues.getFirst().getLeagueId()).isEqualTo(10);
        assertThat(dbLeagues.getFirst().isCurrent()).isTrue();

        // 4. Assert that the event was published correctly using ApplicationEvents
        List<LeagueSavedEvent> events = testEventListener.getEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().countryCode()).isEqualTo(countryCode);
        assertThat(events.getFirst().leagueId()).isEqualTo(10);
        assertThat(events.getFirst().season()).isEqualTo(2025);
    }
}
