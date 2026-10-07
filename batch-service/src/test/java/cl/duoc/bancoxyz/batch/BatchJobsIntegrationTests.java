package cl.duoc.bancoxyz.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:eft-integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.batch.job.enabled=false",
        "app.batch.recovery.enabled=false"
})
class BatchJobsIntegrationTests {

    private final JobOperator jobOperator;
    private final JdbcTemplate jdbcTemplate;
    private final Job movimientosJob;
    private final Job interesesJob;
    private final Job estadosJob;

    @Autowired
    BatchJobsIntegrationTests(
            JobOperator jobOperator,
            JdbcTemplate jdbcTemplate,
            @Qualifier("movimientosDiariosJob") Job movimientosJob,
            @Qualifier("interesesMensualesJob") Job interesesJob,
            @Qualifier("estadosFinancierosAnualesJob") Job estadosJob) {
        this.jobOperator = jobOperator;
        this.jdbcTemplate = jdbcTemplate;
        this.movimientosJob = movimientosJob;
        this.interesesJob = interesesJob;
        this.estadosJob = estadosJob;
    }

    @Test
    void procesaLosNueveArchivosLegacyYPermiteReejecucionIdempotente() {
        assertEquals(BatchStatus.COMPLETED,
                jobOperator.startNextInstance(movimientosJob).getStatus());
        assertEquals(BatchStatus.COMPLETED,
                jobOperator.startNextInstance(interesesJob).getStatus());
        assertEquals(BatchStatus.COMPLETED,
                jobOperator.startNextInstance(estadosJob).getStatus());

        assertEquals(1020, count("movimientos_diarios_resultado"));
        assertEquals(1016, count("intereses_mensuales_resultado"));
        assertEquals(1018, count("estados_financieros_anuales_resultado"));
        assertEquals(339, count("resumen_movimientos_diarios"));
        assertEquals(20, count("resumen_estados_financieros_anuales"));

        assertEquals(BatchStatus.COMPLETED,
                jobOperator.startNextInstance(movimientosJob).getStatus());
        assertEquals(1020, count("movimientos_diarios_resultado"));
    }

    private long count(String table) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
        return count == null ? 0 : count;
    }
}
