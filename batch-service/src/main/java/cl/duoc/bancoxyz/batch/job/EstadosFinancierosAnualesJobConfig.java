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

import cl.duoc.bancoxyz.batch.domain.EstadoFinancieroAnualInput;
import cl.duoc.bancoxyz.batch.domain.EstadoFinancieroAnualResultado;
import cl.duoc.bancoxyz.batch.processor.EstadoFinancieroAnualProcessor;

@Configuration
public class EstadosFinancierosAnualesJobConfig {

    @Bean
    MultiResourcePartitioner estadosFinancierosAnualesPartitioner(
            @Value("classpath*:legacy-data/semana_*/estados_financieros_anuales.csv")
            Resource[] resources) {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        partitioner.setResources(resources);
        partitioner.setKeyName("file");
        return partitioner;
    }

    @Bean
    Step prepararEstadosFinancierosAnualesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcTemplate jdbcTemplate) {
        return new StepBuilder("prepararEstadosFinancierosAnualesStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("DELETE FROM resumen_estados_financieros_anuales");
                    jdbcTemplate.update("DELETE FROM estados_financieros_anuales_resultado");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    Step estadosFinancierosAnualesWorkerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<EstadoFinancieroAnualInput> estadoFinancieroAnualReader,
            EstadoFinancieroAnualProcessor processor,
            JdbcBatchItemWriter<EstadoFinancieroAnualResultado> estadoFinancieroAnualWriter,
            SkipPolicy malformedCsvSkipPolicy,
            RetryPolicy transientDatabaseRetryPolicy,
            @Value("${app.batch.chunk-size:100}") int chunkSize) {
        return new ChunkOrientedStepBuilder<EstadoFinancieroAnualInput, EstadoFinancieroAnualResultado>(
                "estadosFinancierosAnualesWorkerStep", jobRepository, chunkSize)
                .reader(estadoFinancieroAnualReader)
                .processor(processor)
                .writer(estadoFinancieroAnualWriter)
                .transactionManager(transactionManager)
                .faultTolerant()
                .skipPolicy(malformedCsvSkipPolicy)
                .retryPolicy(transientDatabaseRetryPolicy)
                .build();
    }

    @Bean
    Step estadosFinancierosAnualesPartitionedStep(
            JobRepository jobRepository,
            @Qualifier("estadosFinancierosAnualesPartitioner") MultiResourcePartitioner partitioner,
            @Qualifier("estadosFinancierosAnualesWorkerStep") Step workerStep,
            @Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor executor,
            @Value("${app.batch.partition.grid-size:4}") int gridSize) throws Exception {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(executor);
        handler.setStep(workerStep);
        handler.setGridSize(gridSize);
        return new StepBuilder("estadosFinancierosAnualesPartitionedStep", jobRepository)
                .partitioner("estadosFinancierosAnualesWorkerStep", partitioner)
                .partitionHandler(handler)
                .build();
    }

    @Bean
    Step resumirEstadosFinancierosAnualesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcTemplate jdbcTemplate) {
        return new StepBuilder("resumirEstadosFinancierosAnualesStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("""
                            INSERT INTO resumen_estados_financieros_anuales (
                                cuenta_id, total_depositos, total_retiros,
                                total_compras_pagos, movimiento_neto,
                                cantidad_operaciones, operaciones_rechazadas
                            )
                            SELECT cuenta_id,
                                   SUM(CASE WHEN estado = 'VALIDO' AND transaccion = 'deposito'
                                            THEN monto ELSE 0 END),
                                   SUM(CASE WHEN estado = 'VALIDO' AND transaccion = 'retiro'
                                            THEN ABS(monto) ELSE 0 END),
                                   SUM(CASE WHEN estado = 'VALIDO' AND transaccion IN ('compra', 'pago')
                                            THEN ABS(monto) ELSE 0 END),
                                   SUM(CASE WHEN estado = 'VALIDO' THEN monto ELSE 0 END),
                                   COUNT(*),
                                   SUM(CASE WHEN estado = 'ANOMALIA' THEN 1 ELSE 0 END)
                            FROM estados_financieros_anuales_resultado
                            WHERE cuenta_id IS NOT NULL
                            GROUP BY cuenta_id
                            """);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    Job estadosFinancierosAnualesJob(
            JobRepository jobRepository,
            @Qualifier("prepararEstadosFinancierosAnualesStep") Step prepare,
            @Qualifier("estadosFinancierosAnualesPartitionedStep") Step partitions,
            @Qualifier("resumirEstadosFinancierosAnualesStep") Step summary,
            JobExecutionListener auditJobExecutionListener) {
        return new JobBuilder("estadosFinancierosAnualesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(auditJobExecutionListener)
                .start(prepare)
                .next(partitions)
                .next(summary)
                .build();
    }
}

