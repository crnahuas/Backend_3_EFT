package cl.duoc.bancoxyz.batch.domain;

public record EstadoFinancieroAnualInput(
        String sourceFile,
        Long cuentaId,
        String fecha,
        String transaccion,
        String monto,
        String descripcion) {
}

