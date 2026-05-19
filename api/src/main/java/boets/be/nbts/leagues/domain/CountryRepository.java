package boets.be.nbts.leagues.domain;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface CountryRepository extends CrudRepository<CountryEntity, Integer> {

    List<CountryEntity> findByRegion(String region);

}
