package boets.be.nbts.common;

import boets.be.nbts.admin.AdminService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RapidApiClient {

    private final RestClient restClient;
    private final AtomicInteger counter;
    private final AdminService adminService;

    protected RapidApiClient(RestClient.Builder restClientBuilder, @Value("${nbts.rapidApi.key}") String apiKey, AdminService adminService) {
        String baseUrl = "https://api-football-v1.p.rapidapi.com/";
        this.restClient = restClientBuilder.baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-value", "api-football-v1.p.rapidapi.com")
                .defaultHeader("x-rapidapi-key", apiKey)
                .build();
        this.adminService = adminService;
        counter = new AtomicInteger(adminService.getCurrentApiCounter().counter());
    }

    protected <T> T get(String path, Class<T> responseType) {
        return get(path, responseType, Map.of());
    }

    protected <T> T get(String path, Class<T> responseType, Map<String, ?> queryParams) {
        counter.incrementAndGet();
        this.saveApiCounter();

        return restClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder.path(path);

                    queryParams.forEach(builder::queryParam);

                    return builder.build();
                })
                .retrieve()
                .body(responseType);
    }

    public int getCallCount() {
        return counter.get();
    }

    @PreDestroy
    public void saveApiCounter() {
        adminService.saveApiCounter(counter.get());
    }


}
