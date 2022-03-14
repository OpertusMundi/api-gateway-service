package eu.opertusmundi.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfiguration {

    @Value("${opertusmundi.task-executor.core-pool-size:4}")
    Integer corePoolSize;

    @Value("${opertusmundi.task-executor.max-pool-size:8}")
    Integer maxPoolSize;

    @Value("${opertusmundi.task-executor.await-termination-timeout:10}")
    Integer awaitTerminationTimeout;

    @Bean
    TaskExecutor taskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("tasks-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationTimeout);

        return executor;
    }

    @Bean
    TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(corePoolSize);
        scheduler.setThreadNamePrefix("cron-");

        return scheduler;
    }

}
