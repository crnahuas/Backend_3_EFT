package cl.duoc.bancoxyz.batch.job;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import cl.duoc.bancoxyz.batch.domain.MovimientoDiarioInput;
import cl.duoc.bancoxyz.batch.domain.MovimientoDiarioResultado;
import cl.duoc.bancoxyz.batch.processor.MovimientoDiarioProcessor;

@Configuration
public class MovimientosDiariosJobConfig {

    @Bean
    MultiResourcePartitioner movimientosDiariosPartitioner(
            @Value("classpath*:legacy-data/semana_*/movimientos_financieros_diarios.csv")
            Resource[] resources) {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        partitioner.setResources(resources);
        partitioner.setKeyName("file");
        return partitioner;
    }

    @Bean
    Step prepararMovimientosDiariosStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcTemplate jdbcTemplate) {
        return new StepBuilder("prepararMovimientosDiariosStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("DELETE FROM resumen_movimientos_diarios");
                    jdbcTemplate.update("DELETE FROM movimientos_diarios_resultado");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    Step movimientosDiariosWorkerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<MovimientoDiarioInput> movimientoDiarioReader,
            MovimientoDiarioProcessor processor,
            JdbcBatchItemWriter<MovimientoDiarioResultado> movimientoDiarioWriter,
            SkipPolicy malformedCsvSkipPolicy,
            RetryPolicy transientDatabaseRetryPolicy,
            @Value("${app.batch.chunk-size:100}") int chunkSize) {
        return new ChunkOrientedStepBuilder<MovimientoDiarioInput, MovimientoDiarioResultado>(
                "movimientosDiariosWorkerStep", jobRepository, chunkSize)
                .reader(movimientoDiarioReader)
                .processor(processor)
                .writer(movimientoDiarioWriter)
                .transactionManager(transactionManager)
                .faultTolerant()
                .skipPolicy(malformedCsvSkipPolicy)
                .retryPolicy(transientDatabaseRetryPolicy)
                .build();
    }

    @Bean
    Step movimientosDiariosPartitionedStep(
            JobRepository jobRepository,
            @Qualifier("movimientosDiariosPartitioner") MultiResourcePartitioner partitioner,
            @Qualifier("movimientosDiariosWorkerStep") Step workerStep,
            @Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor executor,
            @Value("${app.batch.partition.grid-size:4}") int gridSize) throws Exception {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(executor);
        handler.setStep(workerStep);
        handler.setGridSize(gridSize);
        return new StepBuilder("movimientosDiariosPartitionedStep", jobRepository)
                .partitioner("movimientosDiariosWorkerStep", partitioner)
                .partitionHandler(handler)
                .build();
    }

    @Bean
    Step resumirMovimientosDiariosStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcTemplate jdbcTemplate) {
        return new StepBuilder("resumirMovimientosDiariosStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("""
                            INSERT INTO resumen_movimientos_diarios (
                                fecha, total_registros, total_debitos, total_creditos,
                                total_anomalias, monto_debitos, monto_creditos
                            )
                            SELECT fecha,
                                   COUNT(*),
                                   SUM(CASE WHEN estado = 'VALIDO' AND tipo = 'debito' THEN 1 ELSE 0 END),
                                   SUM(CASE WHEN estado = 'VALIDO' AND tipo = 'credito' THEN 1 ELSE 0 END),
                                   SUM(CASE WHEN estado = 'ANOMALIA' THEN 1 ELSE 0 END),
                                   SUM(CASE WHEN estado = 'VALIDO' AND tipo = 'debito' THEN monto ELSE 0 END),
                                   SUM(CASE WHEN estado = 'VALIDO' AND tipo = 'credito' THEN monto ELSE 0 END)
                            FROM movimientos_diarios_resultado
                            WHERE fecha IS NOT NULL
                            GROUP BY fecha
                            """);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    Job movimientosDiariosJob(
            JobRepository jobRepository,
            @Qualifier("prepararMovimientosDiariosStep") Step prepare,
            @Qualifier("movimientosDiariosPartitionedStep") Step partitions,
            @Qualifier("resumirMovimientosDiariosStep") Step summary,
            JobExecutionListener auditJobExecutionListener) {
        return new JobBuilder("movimientosDiariosJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(auditJobExecutionListener)
                .start(prepare)
                .next(partitions)
                .next(summary)
                .build();
    }
}

