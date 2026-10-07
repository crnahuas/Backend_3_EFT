package cl.duoc.bancoxyz.batch.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
public class BatchJobController {

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Map<String, Job> jobs;

    public BatchJobController(
            JobOperator jobOperator,
            JobRepository jobRepository,
            @Qualifier("movimientosDiariosJob") Job movimientosDiariosJob,
            @Qualifier("interesesMensualesJob") Job interesesMensualesJob,
            @Qualifier("estadosFinancierosAnualesJob") Job estadosFinancierosAnualesJob) {
        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.jobs = Map.of(
                "movimientos-diarios", movimientosDiariosJob,
                "intereses-mensuales", interesesMensualesJob,
                "estados-financieros-anuales", estadosFinancierosAnualesJob);
    }

    @PostMapping("/jobs/{jobName}")
    public ResponseEntity<Map<String, Object>> launch(@PathVariable String jobName) throws Exception {
        Job job = jobs.get(jobName);
        if (job == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message", "Job desconocido",
                            "availableJobs", jobs.keySet()));
        }

        JobExecution execution = jobOperator.startNextInstance(job);

        return ResponseEntity.accepted().body(toResponse(execution));
    }

    @GetMapping("/executions/{executionId}")
    public ResponseEntity<Map<String, Object>> execution(@PathVariable Long executionId) {
        JobExecution execution = jobRepository.getJobExecution(executionId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(execution));
    }

    private Map<String, Object> toResponse(JobExecution execution) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("executionId", execution.getId());
        response.put("job", execution.getJobInstance().getJobName());
        response.put("status", execution.getStatus().name());
        response.put("startTime", execution.getStartTime());
        response.put("endTime", execution.getEndTime());
        response.put("exitCode", execution.getExitStatus().getExitCode());
        response.put("stepCount", execution.getStepExecutions().size());
        response.put("failures", execution.getFailureExceptions().stream()
                .map(Throwable::getMessage)
                .toList());
        return response;
    }
}
