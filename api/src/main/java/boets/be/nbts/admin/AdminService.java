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
        ApiCounterEntity entity = apiCounterRepository.findByDate(LocalDate.now());
        if (entity == null) {
            entity = new ApiCounterEntity();
            entity.setDate(LocalDate.now());
            entity.setCounter(0);
            apiCounterRepository.save(entity);
        }
        return new ApiCounter(entity.getCounter());
    }

    public void saveApiCounter(int counter) {
        ApiCounterEntity entity = apiCounterRepository.findByDate(LocalDate.now());
        if (entity == null) {
            entity = new ApiCounterEntity();
            entity.setDate(LocalDate.now());
            entity.setCounter(counter);
        } else {
            entity.setCounter(entity.getCounter() + counter);
        }
        apiCounterRepository.save(entity);
    }
}
