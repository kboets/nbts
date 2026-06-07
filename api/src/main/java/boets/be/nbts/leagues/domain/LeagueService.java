package boets.be.nbts.leagues.domain;

import boets.be.nbts.leagues.web.League;
import boets.be.nbts.leagues.web.LeagueClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeagueService {

    private final LeagueClientService leagueClientService;
    private final LeagueRepository leagueRepository;

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

    public List<League> getSelectedLeagues() {
        List<LeagueEntity> leagueEntityList = leagueRepository.findByCurrent(true);
        if (leagueEntityList.isEmpty()) {
            return List.of();
        }
        return leagueEntityList.stream()
                .map(this::mapToLeague)
                .toList();
    }

    private LeagueEntity mapToEntity(League league) {
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

    private League mapToLeague(LeagueEntity leagueEntity) {
        return new League(leagueEntity.getLeagueId(),
                leagueEntity.getName(), leagueEntity.getLogo(),
                leagueEntity.getCountryCode(), leagueEntity.getSeason(),
                leagueEntity.getStartSeason(), leagueEntity.getEndSeason(), leagueEntity.isCurrent());
    }
}
