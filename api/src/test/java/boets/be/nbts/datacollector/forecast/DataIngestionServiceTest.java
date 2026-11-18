package boets.be.nbts.datacollector.forecast;

import boets.be.nbts.config.ForecastIngestionExecutorConfiguration;
import boets.be.nbts.datacollector.models.ForecastCollectedEvent;
import boets.be.nbts.datacollector.models.ForecastRawData;
import boets.be.nbts.leagues.LeagueForecastApi;
import boets.be.nbts.leagues.domain.models.ForecastLeague;
import boets.be.nbts.results.ResultForecastApi;
import boets.be.nbts.results.domain.models.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataIngestionServiceTest {

    private final LeagueForecastApi leagues = mock(LeagueForecastApi.class);
    private final ResultForecastApi results = mock(ResultForecastApi.class);
    private final ConcurrentLinkedQueue<ForecastCollectedEvent> events = new ConcurrentLinkedQueue<>();
    private AnnotationConfigApplicationContext context;
    private DataIngestionService service;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.registerBean(LeagueForecastApi.class, () -> leagues);
        context.registerBean(ResultForecastApi.class, () -> results);
        context.registerBean("otherExecutor", Executor.class, () -> Runnable::run);
        context.register(ForecastIngestionExecutorConfiguration.class, DataIngestionService.class);
        context.addApplicationListener(event -> {
            if (event instanceof PayloadApplicationEvent<?> payloadEvent
                    && payloadEvent.getPayload() instanceof ForecastCollectedEvent collectedEvent) {
                events.add(collectedEvent);
            }
        });
        context.refresh();
        service = context.getBean(DataIngestionService.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void ingestionUsesApplicationClassLoaderOnManagedWorkerAndPublishesCollectedData() {
        ForecastLeague league = new ForecastLeague(1, "League", 2026, "BE");
        Result currentRound = mock(Result.class);
        when(currentRound.round()).thenReturn(12);
        when(leagues.getSelectedLeaguesForForecasting()).thenReturn(List.of(league));
        ConcurrentLinkedQueue<ClassLoader> workerClassLoaders = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> workerNames = new ConcurrentLinkedQueue<>();
        when(results.getResultsForForecasting(1, 2026)).thenAnswer(invocation -> {
            workerClassLoaders.add(Thread.currentThread().getContextClassLoader());
            workerNames.add(Thread.currentThread().getName());
            return List.of(currentRound);
        });
        when(results.getStandingsForForecasting(1, 2026)).thenReturn(List.of());
        when(results.getCurrentRound(1, 2026)).thenReturn(Optional.of(currentRound));

        Thread submittingThread = Thread.currentThread();
        ClassLoader originalClassLoader = submittingThread.getContextClassLoader();
        try {
            submittingThread.setContextClassLoader(new ClassLoader(null) {});
            service.triggerDataIngestion();
        } finally {
            submittingThread.setContextClassLoader(originalClassLoader);
        }

        assertThat(workerNames).singleElement().asString().startsWith("forecast-ingestion-");
        assertThat(workerClassLoaders).containsExactly(ForecastIngestionExecutorConfiguration.class.getClassLoader());
        assertThat(events).containsExactly(new ForecastCollectedEvent(
                List.of(new ForecastRawData(league, List.of(currentRound), List.of(), 12))));
    }

    @Test
    void ingestionPropagatesWorkerFailureWithoutPublishingAnEvent() {
        when(leagues.getSelectedLeaguesForForecasting())
                .thenReturn(List.of(new ForecastLeague(1, "League", 2026, "BE")));
        when(results.getResultsForForecasting(1, 2026)).thenThrow(new IllegalStateException("API failed"));

        assertThatThrownBy(service::triggerDataIngestion)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("API failed");
        assertThat(events).isEmpty();
    }
}
