package cl.duoc.bancoxyz.batch.recovery;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FailedJobRecoveryServiceTests {

    @Test
    void reiniciaAutomaticamenteLaUltimaEjecucionFallida() throws Exception {
        JobOperator operator = mock(JobOperator.class);
        JobRepository repository = mock(JobRepository.class);
        JobInstance instance = new JobInstance(10L, "movimientosDiariosJob");
        JobExecution failed = new JobExecution(20L, instance, new JobParameters());
        failed.setStatus(BatchStatus.FAILED);

        when(repository.getLastJobInstance("movimientosDiariosJob")).thenReturn(instance);
        when(repository.getJobExecutions(instance)).thenReturn(List.of(failed));

        var recovery = new FailedJobRecoveryService(operator, repository, true, 2);
        recovery.restartLatestFailedExecutions();

        verify(operator).restart(failed);
    }
}
