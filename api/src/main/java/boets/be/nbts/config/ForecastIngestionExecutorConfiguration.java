package boets.be.nbts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ForkJoinPool;

@Configuration(proxyBeanMethods = false)
public class ForecastIngestionExecutorConfiguration {

    @Bean(defaultCandidate = false)
    ThreadPoolTaskExecutor forecastIngestionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int parallelism = ForkJoinPool.getCommonPoolParallelism();
        executor.setCorePoolSize(parallelism);
        executor.setMaxPoolSize(parallelism);

        ClassLoader applicationClassLoader = ForecastIngestionExecutorConfiguration.class.getClassLoader();
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("forecast-ingestion-");
        executor.setThreadFactory(task -> {
            Thread thread = threadFactory.newThread(task);
            thread.setContextClassLoader(applicationClassLoader);
            return thread;
        });
        return executor;
    }
}
