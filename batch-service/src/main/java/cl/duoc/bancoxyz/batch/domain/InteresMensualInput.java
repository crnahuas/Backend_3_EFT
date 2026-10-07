package cl.duoc.bancoxyz.batch.domain;

public record InteresMensualInput(
        String sourceFile,
        Long cuentaId,
        String nombre,
        String saldo,
        String edad,
        String tipo) {
}
