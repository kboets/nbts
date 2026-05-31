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

//    public List<League> getLeaguesForCountryAndSeason(String countryCode, int season) {
//        List<LeagueEntity> leagues = leagueRepository.findByCountryCodeAndSeason(countryCode, season);
//        if (leagues.isEmpty()) {
//            List<League> apiLeagues = leagueClientService.getLeaguesByCountryAndSeason(countryCode, season);
//            List<LeagueEntity> leagueEntities = apiLeagues.stream()
//                    .map(this::mapToEntity)
//                    .toList();
//            return (List<LeagueEntity>) leagueRepository.saveAll(leagueEntities);
//        }
//        return leagues;
//    }

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
}
