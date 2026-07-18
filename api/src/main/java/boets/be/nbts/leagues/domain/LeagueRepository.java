package boets.be.nbts.leagues.domain;

import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends CrudRepository<LeagueEntity, Integer> {
    List<LeagueEntity> findByCountryCodeAndSeason(String countryCode, int season);
    List<LeagueEntity> findByCountryCode(String countryCode);
    List<LeagueEntity> findByCurrent(boolean current);
    Optional<LeagueEntity> findByLeagueId(int leagueId);
}
