package cl.duoc.bancoxyz.batch.domain;

public record MovimientoDiarioInput(
        String sourceFile,
        Long id,
        String fecha,
        String monto,
        String tipo) {
}

