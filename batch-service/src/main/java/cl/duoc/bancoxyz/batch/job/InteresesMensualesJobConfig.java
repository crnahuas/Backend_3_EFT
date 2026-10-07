package cl.duoc.bancoxyz.batch.job;

import java.util.HashSet;
import java.util.Set;

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
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
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

import cl.duoc.bancoxyz.batch.domain.InteresMensualInput;
import cl.duoc.bancoxyz.batch.domain.InteresMensualResultado;
import cl.duoc.bancoxyz.batch.processor.InteresMensualProcessor;

@Configuration
public class InteresesMensualesJobConfig {

    @Bean
    MultiResourcePartitioner interesesMensualesPartitioner(
            @Value("classpath*:legacy-data/semana_*/intereses_trimestrales.csv")
            Resource[] resources) {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        partitioner.setResources(resources);
        partitioner.setKeyName("file");
        return partitioner;
    }

    @Bean
    Step prepararInteresesMensualesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcTemplate jdbcTemplate) {
        return new StepBuilder("prepararInteresesMensualesStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("DELETE FROM intereses_mensuales_resultado");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    Step interesesMensualesWorkerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<InteresMensualInput> interesMensualReader,
            InteresMensualProcessor processor,
            JdbcBatchItemWriter<InteresMensualResultado> interesMensualWriter,
            SkipPolicy malformedCsvSkipPolicy,
            RetryPolicy transientDatabaseRetryPolicy,
            @Value("${app.batch.chunk-size:100}") int chunkSize) {
        return new ChunkOrientedStepBuilder<InteresMensualInput, InteresMensualResultado>(
                "interesesMensualesWorkerStep", jobRepository, chunkSize)
                .reader(interesMensualReader)
                .processor(processor)
                .writer(interesMensualWriter)
                .transactionManager(transactionManager)
                .faultTolerant()
                .skipPolicy(malformedCsvSkipPolicy)
                .retryPolicy(transientDatabaseRetryPolicy)
                .build();
    }

    @Bean
    Step interesesMensualesPartitionedStep(
            JobRepository jobRepository,
            @Qualifier("interesesMensualesPartitioner") MultiResourcePartitioner partitioner,
            @Qualifier("interesesMensualesWorkerStep") Step workerStep,
            @Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor executor,
            @Value("${app.batch.partition.grid-size:4}") int gridSize) throws Exception {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(executor);
        handler.setStep(workerStep);
        handler.setGridSize(gridSize);
        return new StepBuilder("interesesMensualesPartitionedStep", jobRepository)
                .partitioner("interesesMensualesWorkerStep", partitioner)
                .partitionHandler(handler)
                .build();
    }

    @Bean
    Step marcarInteresesMensualesDuplicadosStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcTemplate jdbcTemplate) {
        return new StepBuilder("marcarInteresesMensualesDuplicadosStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Set<String> claves = new HashSet<>();
                    jdbcTemplate.query("""
                                    SELECT registro_id, cuenta_id, nombre, saldo, edad, tipo
                                    FROM intereses_mensuales_resultado
                                    ORDER BY registro_id
                                    """,
                            resultSet -> {
                                String clave = resultSet.getLong("cuenta_id") + "|"
                                        + resultSet.getString("nombre") + "|"
                                        + resultSet.getBigDecimal("saldo") + "|"
                                        + resultSet.getObject("edad") + "|"
                                        + resultSet.getString("tipo");
                                if (!claves.add(clave)) {
                                    jdbcTemplate.update("""
                                                    UPDATE intereses_mensuales_resultado
                                                    SET estado = 'ANOMALIA',
                                                        observacion = CASE
                                                            WHEN observacion IS NULL OR observacion = ''
                                                            THEN 'Registro duplicado'
                                                            ELSE observacion || '; registro duplicado'
                                                        END
                                                    WHERE registro_id = ?
                                                    """,
                                            resultSet.getLong("registro_id"));
                                }
                            });
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    Job interesesMensualesJob(
            JobRepository jobRepository,
            @Qualifier("prepararInteresesMensualesStep") Step prepare,
            @Qualifier("interesesMensualesPartitionedStep") Step partitions,
            @Qualifier("marcarInteresesMensualesDuplicadosStep") Step duplicates,
            JobExecutionListener auditJobExecutionListener) {
        return new JobBuilder("interesesMensualesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(auditJobExecutionListener)
                .start(prepare)
                .next(partitions)
                .next(duplicates)
                .build();
    }
}
