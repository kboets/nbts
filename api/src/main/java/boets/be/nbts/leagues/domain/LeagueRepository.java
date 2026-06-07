package boets.be.nbts.leagues.domain;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface LeagueRepository extends CrudRepository<LeagueEntity, Integer> {
    List<LeagueEntity> findByCountryCodeAndSeason(String countryCode, int season);
    List<LeagueEntity> findByCurrent(boolean current);
}
