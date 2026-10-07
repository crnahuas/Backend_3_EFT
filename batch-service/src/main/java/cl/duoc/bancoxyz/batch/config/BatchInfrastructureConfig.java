package cl.duoc.bancoxyz.batch.config;

import java.time.Duration;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class BatchInfrastructureConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchInfrastructureConfig.class);

    @Bean(name = "batchTaskExecutor")
    ThreadPoolTaskExecutor batchTaskExecutor(
            @Value("${app.batch.partition.threads:4}") int threads) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threads);
        executor.setMaxPoolSize(threads);
        executor.setQueueCapacity(threads * 2);
        executor.setThreadNamePrefix("legacy-partition-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean
    RetryPolicy transientDatabaseRetryPolicy(
            @Value("${app.batch.retry.max-attempts:3}") int retries,
            @Value("${app.batch.retry.delay-ms:500}") long delayMs) {
        return RetryPolicy.builder()
                .includes(TransientDataAccessException.class)
                .maxRetries(retries)
                .delay(Duration.ofMillis(delayMs))
                .build();
    }

    @Bean
    SkipPolicy malformedCsvSkipPolicy(
            @Value("${app.batch.skip-limit:25}") long skipLimit) {
        return (throwable, skipCount) -> {
            boolean skippable = throwable instanceof FlatFileParseException;
            if (skippable && skipCount < skipLimit) {
                FlatFileParseException parseException = (FlatFileParseException) throwable;
                log.warn("Registro CSV omitido line={} input={}",
                        parseException.getLineNumber(), parseException.getInput());
                return true;
            }
            return false;
        };
    }

    @Bean
    JobExecutionListener auditJobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("job_started job={} executionId={} parameters={}",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getId(),
                        jobExecution.getJobParameters());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                log.info("job_finished job={} executionId={} status={} steps={} failures={}",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getId(),
                        jobExecution.getStatus(),
                        jobExecution.getStepExecutions().size(),
                        jobExecution.getFailureExceptions().size());
            }
        };
    }
}

