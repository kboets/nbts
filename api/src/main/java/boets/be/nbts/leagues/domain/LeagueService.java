package boets.be.nbts.leagues.domain;

import boets.be.nbts.leagues.domain.models.LeagueDeletedEvent;
import boets.be.nbts.leagues.domain.models.LeagueSavedEvent;
import boets.be.nbts.leagues.web.League;
import boets.be.nbts.leagues.web.LeagueClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueService {

    private final LeagueClientService leagueClientService;
    private final LeagueRepository leagueRepository;
    private final ApplicationEventPublisher eventPublisher;

//    public List<League> getLeaguesForCountryAndSeason(String countryCode, int season, boolean force) {
//        List<LeagueEntity> leagues = leagueRepository.findByCountryCodeAndSeason(countryCode, season);
//        if (force) {
//            List<League> apiLeagues = leagueClientService.getLeaguesByCountryAndSeason(countryCode, season);
//            List<LeagueEntity> leagueEntities = apiLeagues.stream()
//                    .map(this::mapToEntity)
//                    .filter(leagueEntity -> !leagues.contains(leagueEntity))
//                    .toList();
//            leagues.addAll((List<LeagueEntity>) leagueRepository.saveAll(leagueEntities));
//        }
//        return leagues.stream()
//                .map(this::mapToLeague)
//                .toList();
//    }

    /**
     * Retrieves the leagues that are not yet persisted. A call to the API is made to retrieve the leagues.
     * @param countryCode - country code
     * @return - list of leagues
     */
    @Cacheable(value = "leagues", key = "#countryCode")
    public List<League> getCurrentLeaguesForCountry(String countryCode) {
        List<League> leagues = leagueClientService.getLeaguesByCountry(countryCode);
        List<LeagueEntity> selectedLeagues = leagueRepository.findByCountryCode(countryCode);
        Set<Integer> selectedLeagueIds = selectedLeagues.stream()
                .map(LeagueEntity::getLeagueId)
                .collect(java.util.stream.Collectors.toSet());
        return leagues.stream()
                .filter(league -> !selectedLeagueIds.contains(league.leagueId()))
                .toList();
    }

    /**
     * Retrieves the leagues that are already persisted
     * @param countryCode - country code
     * @return - list of leagues
     */
    public List<League> getCurrentSelectedLeaguesForCountry(String countryCode) {
        List<LeagueEntity> selectedLeagues = leagueRepository.findByCountryCode(countryCode);
        return selectedLeagues.stream()
                .map(this::mapToLeague)
                .toList();
    }

    public List<League> getSelectedLeagues() {
        List<LeagueEntity> leagueEntityList = leagueRepository.findByCurrent(true);
        if (leagueEntityList.isEmpty()) {
            return List.of();
        }
        //check if current season is still active, otherwise set to false
        boolean needRefresh = false;
        for (LeagueEntity leagueEntity : leagueEntityList) {
            if (leagueEntity.getEndSeason().isBefore(LocalDate.now())) {
                leagueEntity.setCurrent(false);
                needRefresh = true;
            }
        }
        if (needRefresh) {
            leagueRepository.saveAll(leagueEntityList);
            leagueEntityList = leagueRepository.findByCurrent(true);
        }

        return leagueEntityList.stream()
                .map(this::mapToLeague)
                .toList();
    }

    @Cacheable(value = "selectedLeagues", key = "'countryCode'")
    public List<String> getSelectedLeaguesCountryCodes() {
        return leagueRepository.findByCurrent(true).stream()
                .map(LeagueEntity::getCountryCode)
                .toList();
    }

    /**
     * Saves the league in the database.
     * Removes the cached leagues for the country code.
     * Will trigger an event.
     * @param league - league to save
     * @return - the saved league
     */
    @Caching(evict = {
            @CacheEvict(value = "leagues", key = "#league.countryCode"),
            @CacheEvict(value = "selectedLeagues", key = "'countryCode'")
    })

    public League save(League league) {
        LeagueEntity savedLeague = leagueRepository.save(mapToEntity(league));
        League persistedLeague = mapToLeague(savedLeague);
        log.info("New league persisted {}, trigger an event", persistedLeague.name());
        LeagueSavedEvent leagueSavedEvent = new LeagueSavedEvent(persistedLeague.countryCode(), persistedLeague.leagueId(), persistedLeague.season());
        eventPublisher.publishEvent(leagueSavedEvent);
        return persistedLeague;
    }

    /**
     * Deletes the league from the database.
     * Removes the cached leagues for the country code.
     * Will trigger an event.
     * @param league - league to delete
     * @return - true if the league was deleted, false otherwise
     */
    @Caching(evict = {
            @CacheEvict(value = "leagues", key = "#league.countryCode"),
            @CacheEvict(value = "selectedLeagues", key = "'countryCode'")
    })
    public boolean delete(League league) {
        Optional<LeagueEntity> leagueEntity = leagueRepository.findByLeagueId(league.leagueId());
        if (leagueEntity.isEmpty()) {
            log.warn("League with id {} not found, cannot delete", league.leagueId());
            return false;
        }
        log.info("League with id {} deleted, trigger an event", league.leagueId());
        LeagueDeletedEvent leagueDeletedEvent = new LeagueDeletedEvent(league.countryCode(), league.leagueId(), league.season());
        leagueRepository.delete(leagueEntity.get());
        eventPublisher.publishEvent(leagueDeletedEvent);
        return true;
    }

    protected LeagueEntity mapToEntity(League league) {
        LeagueEntity entity = new LeagueEntity();
        entity.setLeagueId(league.leagueId());
        entity.setName(league.name());
        entity.setLogo(league.logo());
        entity.setCountryCode(league.countryCode());
        entity.setSeason(league.season());
        entity.setStartSeason(league.start());
        entity.setEndSeason(league.end());
        entity.setCurrent(league.current());
        return entity;
    }

    protected League mapToLeague(LeagueEntity leagueEntity) {
        return new League(leagueEntity.getLeagueId(),
                leagueEntity.getName(), leagueEntity.getLogo(),
                leagueEntity.getCountryCode(), leagueEntity.getSeason(),
                leagueEntity.getStartSeason(), leagueEntity.getEndSeason(), leagueEntity.isCurrent());
    }
}
