package boets.be.nbts.admin;

import boets.be.nbts.admin.domain.ApiCounterEntity;
import boets.be.nbts.admin.domain.ApiCounterRepository;
import boets.be.nbts.admin.domain.models.ApiCounter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final ApiCounterRepository apiCounterRepository;

    public ApiCounter getCurrentApiCounter() {
        return getApiCounter(LocalDate.now());
    }

    public ApiCounter getApiCounter(LocalDate date) {
        ApiCounterEntity entity = getOrCreateApiCounterEntity(date);
        return new ApiCounter(entity.getCounter());
    }

    public void saveApiCounter(LocalDate date, int counter) {
        ApiCounterEntity entity = apiCounterRepository.findByDate(date);
        if (entity == null) {
            entity = new ApiCounterEntity();
            entity.setDate(date);
        }
        entity.setCounter(counter);
        apiCounterRepository.save(entity);
    }

    private ApiCounterEntity getOrCreateApiCounterEntity(LocalDate date) {
        ApiCounterEntity entity = apiCounterRepository.findByDate(date);
        if (entity == null) {
            entity = new ApiCounterEntity();
            entity.setDate(date);
            entity.setCounter(0);
            apiCounterRepository.save(entity);
        }
        return entity;
    }
}
