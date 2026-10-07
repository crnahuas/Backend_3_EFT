package cl.duoc.bancoxyz.batch.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EstadoFinancieroAnualResultado(
        String sourceFile,
        Long cuentaId,
        String fechaOriginal,
        LocalDate fecha,
        String transaccion,
        String montoOriginal,
        BigDecimal monto,
        String descripcion,
        String estado,
        String observacion) {
}

