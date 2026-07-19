package boets.be.nbts.common;

import boets.be.nbts.admin.AdminService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class RapidApiClient {

    private final RestClient restClient;
    private final AtomicInteger counter;
    private final AdminService adminService;
    private LocalDate counterDate;

    protected RapidApiClient(RestClient.Builder restClientBuilder, @Value("${nbts.rapidApi.key}") String apiKey, AdminService adminService) {
        String baseUrl = "https://api-football-v1.p.rapidapi.com/";
        this.restClient = restClientBuilder.baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-value", "api-football-v1.p.rapidapi.com")
                .defaultHeader("x-rapidapi-key", apiKey)
                .build();
        this.adminService = adminService;
        counter = new AtomicInteger();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public synchronized void resetCounter() {
        ensureCounterLoadedFor(currentDate());
    }


    protected <T> T get(String path, Class<T> responseType) {
        return get(path, responseType, Map.of());
    }

    protected <T> T get(String path, Class<T> responseType, Map<String, ?> queryParams) {
        incrementAndPersistCounter();

        return restClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder.path(path);

                    queryParams.forEach(builder::queryParam);

                    return builder.build();
                })
                .retrieve()
                .body(responseType);
    }

    public synchronized int getCallCount() {
        ensureCounterLoadedFor(currentDate());
        return counter.get();
    }

    @PreDestroy
    public synchronized void saveApiCounter() {
        if (counterDate == null) {
            return;
        }
        adminService.saveApiCounter(counterDate, counter.get());
    }

    protected LocalDate currentDate() {
        return LocalDate.now();
    }

    private void ensureCounterLoadedFor(LocalDate date) {
        if (date.equals(counterDate)) {
            return;
        }

        log.info("Synchronizing API counter for {}", date);
        counter.set(adminService.getApiCounter(date).counter());
        counterDate = date;
    }

    private synchronized void incrementAndPersistCounter() {
        LocalDate today = currentDate();
        ensureCounterLoadedFor(today);
        int currentCounter = counter.incrementAndGet();
        adminService.saveApiCounter(today, currentCounter);
    }


}
