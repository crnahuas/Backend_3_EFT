package cl.duoc.bancoxyz.batch.domain;

import java.math.BigDecimal;

public record InteresMensualResultado(
        String sourceFile,
        Long cuentaId,
        String nombre,
        String saldoOriginal,
        BigDecimal saldo,
        String edadOriginal,
        Integer edad,
        String tipo,
        BigDecimal tasaInteres,
        BigDecimal interesCalculado,
        BigDecimal saldoFinal,
        String estado,
        String observacion) {
}
