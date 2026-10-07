package cl.duoc.bancoxyz.batch.recovery;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class FailedJobRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(FailedJobRecoveryService.class);
    private static final List<String> MANAGED_JOBS = List.of(
            "movimientosDiariosJob",
            "interesesMensualesJob",
            "estadosFinancierosAnualesJob");

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final boolean enabled;
    private final int maxRestarts;
    private final Set<Long> claimedExecutions = ConcurrentHashMap.newKeySet();

    public FailedJobRecoveryService(
            JobOperator jobOperator,
            JobRepository jobRepository,
            @Value("${app.batch.recovery.enabled:true}") boolean enabled,
            @Value("${app.batch.recovery.max-restarts:2}") int maxRestarts) {
        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.enabled = enabled;
        this.maxRestarts = maxRestarts;
    }

    @Scheduled(fixedDelayString = "${app.batch.recovery.poll-ms:5000}")
    public void restartLatestFailedExecutions() {
        if (!enabled) {
            return;
        }

        for (String jobName : MANAGED_JOBS) {
            JobInstance instance = jobRepository.getLastJobInstance(jobName);
            if (instance == null) {
                continue;
            }

            var executions = jobRepository.getJobExecutions(instance);
            JobExecution latest = executions.stream()
                    .max(Comparator.comparing(JobExecution::getId))
                    .orElse(null);

            if (latest == null || latest.getStatus() != BatchStatus.FAILED) {
                continue;
            }

            int restartsAlreadyAttempted = Math.max(0, executions.size() - 1);
            if (restartsAlreadyAttempted >= maxRestarts
                    || !claimedExecutions.add(latest.getId())) {
                continue;
            }

            try {
                log.warn("job_automatic_restart job={} failedExecutionId={} attempt={}/{}",
                        jobName,
                        latest.getId(),
                        restartsAlreadyAttempted + 1,
                        maxRestarts);
                jobOperator.restart(latest);
            } catch (Exception exception) {
                claimedExecutions.remove(latest.getId());
                log.error("job_automatic_restart_failed job={} executionId={}",
                        jobName, latest.getId(), exception);
            }
        }
    }
}
