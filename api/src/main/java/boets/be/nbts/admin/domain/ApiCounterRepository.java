package boets.be.nbts.admin.domain;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface ApiCounterRepository extends CrudRepository<ApiCounterEntity, Integer> {
    ApiCounterEntity findByDate(LocalDate date);
}
