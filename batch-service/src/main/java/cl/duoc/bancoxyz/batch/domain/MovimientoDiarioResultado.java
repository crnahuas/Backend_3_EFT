package cl.duoc.bancoxyz.batch.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoDiarioResultado(
        String sourceFile,
        Long legacyId,
        String fechaOriginal,
        LocalDate fecha,
        String montoOriginal,
        BigDecimal monto,
        String tipo,
        String estado,
        String observacion) {
}

