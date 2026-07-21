package boets.be.nbts.admin;

import boets.be.nbts.admin.domain.ApiCounterEntity;
import boets.be.nbts.admin.domain.ApiCounterRepository;
import boets.be.nbts.admin.domain.models.ApiCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ApiCounterRepository apiCounterRepository;

    @InjectMocks
    private AdminService adminService;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
    }

    @Test
    void getCurrentApiCounter_shouldReturnCounterForToday() {
        ApiCounterEntity entity = new ApiCounterEntity();
        entity.setDate(today);
        entity.setCounter(10);
        when(apiCounterRepository.findByDate(today)).thenReturn(entity);

        ApiCounter result = adminService.getCurrentApiCounter();

        assertThat(result.counter()).isEqualTo(10);
        verify(apiCounterRepository).findByDate(today);
    }

    @Test
    void getApiCounter_whenEntityExists_shouldReturnCounter() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        ApiCounterEntity entity = new ApiCounterEntity();
        entity.setDate(date);
        entity.setCounter(5);
        when(apiCounterRepository.findByDate(date)).thenReturn(entity);

        ApiCounter result = adminService.getApiCounter(date);

        assertThat(result.counter()).isEqualTo(5);
        verify(apiCounterRepository).findByDate(date);
        verify(apiCounterRepository, never()).save(any());
    }

    @Test
    void getApiCounter_whenEntityDoesNotExist_shouldCreateAndReturnZero() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        when(apiCounterRepository.findByDate(date)).thenReturn(null);

        ApiCounter result = adminService.getApiCounter(date);

        assertThat(result.counter()).isEqualTo(0);
        
        ArgumentCaptor<ApiCounterEntity> captor = ArgumentCaptor.forClass(ApiCounterEntity.class);
        verify(apiCounterRepository).save(captor.capture());
        
        ApiCounterEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getDate()).isEqualTo(date);
        assertThat(savedEntity.getCounter()).isEqualTo(0);
    }

    @Test
    void saveApiCounter_whenEntityExists_shouldUpdateCounter() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        ApiCounterEntity entity = new ApiCounterEntity();
        entity.setDate(date);
        entity.setCounter(5);
        when(apiCounterRepository.findByDate(date)).thenReturn(entity);

        adminService.saveApiCounter(date, 20);

        assertThat(entity.getCounter()).isEqualTo(20);
        verify(apiCounterRepository).save(entity);
    }

    @Test
    void saveApiCounter_whenEntityDoesNotExist_shouldCreateNewAndSave() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        when(apiCounterRepository.findByDate(date)).thenReturn(null);

        adminService.saveApiCounter(date, 15);

        ArgumentCaptor<ApiCounterEntity> captor = ArgumentCaptor.forClass(ApiCounterEntity.class);
        verify(apiCounterRepository).save(captor.capture());
        
        ApiCounterEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getDate()).isEqualTo(date);
        assertThat(savedEntity.getCounter()).isEqualTo(15);
    }
}
