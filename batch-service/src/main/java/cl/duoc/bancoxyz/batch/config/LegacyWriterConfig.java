package cl.duoc.bancoxyz.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import cl.duoc.bancoxyz.batch.domain.EstadoFinancieroAnualResultado;
import cl.duoc.bancoxyz.batch.domain.InteresMensualResultado;
import cl.duoc.bancoxyz.batch.domain.MovimientoDiarioResultado;

@Configuration
public class LegacyWriterConfig {

    @Bean
    JdbcBatchItemWriter<MovimientoDiarioResultado> movimientoDiarioWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<MovimientoDiarioResultado>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO movimientos_diarios_resultado (
                            source_file, legacy_id, fecha_original, fecha,
                            monto_original, monto, tipo, estado, observacion
                        ) VALUES (
                            :sourceFile, :legacyId, :fechaOriginal, :fecha,
                            :montoOriginal, :monto, :tipo, :estado, :observacion
                        )
                        """)
                .itemSqlParameterSourceProvider(item -> new MapSqlParameterSource()
                        .addValue("sourceFile", item.sourceFile())
                        .addValue("legacyId", item.legacyId())
                        .addValue("fechaOriginal", item.fechaOriginal())
                        .addValue("fecha", item.fecha())
                        .addValue("montoOriginal", item.montoOriginal())
                        .addValue("monto", item.monto())
                        .addValue("tipo", item.tipo())
                        .addValue("estado", item.estado())
                        .addValue("observacion", item.observacion()))
                .build();
    }

    @Bean
    JdbcBatchItemWriter<InteresMensualResultado> interesMensualWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<InteresMensualResultado>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO intereses_mensuales_resultado (
                            source_file, cuenta_id, nombre, saldo_original, saldo,
                            edad_original, edad, tipo, tasa_interes, interes_calculado,
                            saldo_final, estado, observacion
                        ) VALUES (
                            :sourceFile, :cuentaId, :nombre, :saldoOriginal, :saldo,
                            :edadOriginal, :edad, :tipo, :tasaInteres, :interesCalculado,
                            :saldoFinal, :estado, :observacion
                        )
                        """)
                .itemSqlParameterSourceProvider(item -> new MapSqlParameterSource()
                        .addValue("sourceFile", item.sourceFile())
                        .addValue("cuentaId", item.cuentaId())
                        .addValue("nombre", item.nombre())
                        .addValue("saldoOriginal", item.saldoOriginal())
                        .addValue("saldo", item.saldo())
                        .addValue("edadOriginal", item.edadOriginal())
                        .addValue("edad", item.edad())
                        .addValue("tipo", item.tipo())
                        .addValue("tasaInteres", item.tasaInteres())
                        .addValue("interesCalculado", item.interesCalculado())
                        .addValue("saldoFinal", item.saldoFinal())
                        .addValue("estado", item.estado())
                        .addValue("observacion", item.observacion()))
                .build();
    }

    @Bean
    JdbcBatchItemWriter<EstadoFinancieroAnualResultado> estadoFinancieroAnualWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<EstadoFinancieroAnualResultado>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO estados_financieros_anuales_resultado (
                            source_file, cuenta_id, fecha_original, fecha, transaccion,
                            monto_original, monto, descripcion, estado, observacion
                        ) VALUES (
                            :sourceFile, :cuentaId, :fechaOriginal, :fecha, :transaccion,
                            :montoOriginal, :monto, :descripcion, :estado, :observacion
                        )
                        """)
                .itemSqlParameterSourceProvider(item -> new MapSqlParameterSource()
                        .addValue("sourceFile", item.sourceFile())
                        .addValue("cuentaId", item.cuentaId())
                        .addValue("fechaOriginal", item.fechaOriginal())
                        .addValue("fecha", item.fecha())
                        .addValue("transaccion", item.transaccion())
                        .addValue("montoOriginal", item.montoOriginal())
                        .addValue("monto", item.monto())
                        .addValue("descripcion", item.descripcion())
                        .addValue("estado", item.estado())
                        .addValue("observacion", item.observacion()))
                .build();
    }
}
