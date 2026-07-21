package boets.be.nbts.common;

import boets.be.nbts.admin.AdminService;
import boets.be.nbts.admin.domain.models.ApiCounter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RapidApiClientTest {

    @Test
    void constructor_shouldNotLoadApiCounterImmediately() {
        AdminService adminService = mock(AdminService.class);

        new TestRapidApiClient(RestClient.builder(), "test-api-key", adminService, LocalDate.of(2026, 7, 19));

        verifyNoInteractions(adminService);
    }

    @Test
    void getCallCount_shouldReloadCounterWhenDayChanges() {
        AdminService adminService = mock(AdminService.class);
        LocalDate firstDay = LocalDate.of(2026, 7, 19);
        LocalDate secondDay = firstDay.plusDays(1);

        when(adminService.getApiCounter(firstDay)).thenReturn(new ApiCounter(4));
        when(adminService.getApiCounter(secondDay)).thenReturn(new ApiCounter(0));

        TestRapidApiClient client = new TestRapidApiClient(RestClient.builder(), "test-api-key", adminService, firstDay);

        assertThat(client.getCallCount()).isEqualTo(4);

        client.setCurrentDate(secondDay);

        assertThat(client.getCallCount()).isZero();
        verify(adminService).getApiCounter(firstDay);
        verify(adminService).getApiCounter(secondDay);
    }

    @Test
    void saveApiCounter_shouldPersistCounterForLoadedDate() {
        AdminService adminService = mock(AdminService.class);
        LocalDate firstDay = LocalDate.of(2026, 7, 19);
        LocalDate secondDay = firstDay.plusDays(1);

        when(adminService.getApiCounter(firstDay)).thenReturn(new ApiCounter(3));

        TestRapidApiClient client = new TestRapidApiClient(RestClient.builder(), "test-api-key", adminService, firstDay);

        assertThat(client.getCallCount()).isEqualTo(3);

        client.setCurrentDate(secondDay);
        client.saveApiCounter();

        verify(adminService).saveApiCounter(firstDay, 3);
        verify(adminService, never()).saveApiCounter(secondDay, 3);
    }

    private static final class TestRapidApiClient extends RapidApiClient {

        private LocalDate currentDate;

        private TestRapidApiClient(RestClient.Builder restClientBuilder,
                                   String apiKey,
                                   AdminService adminService,
                                   LocalDate currentDate) {
            super(restClientBuilder, apiKey, adminService);
            this.currentDate = currentDate;
        }

        @Override
        protected LocalDate currentDate() {
            return currentDate;
        }

        private void setCurrentDate(LocalDate currentDate) {
            this.currentDate = currentDate;
        }
    }
}
