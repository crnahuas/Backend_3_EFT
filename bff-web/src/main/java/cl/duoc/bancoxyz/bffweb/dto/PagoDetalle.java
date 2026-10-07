package cl.duoc.bancoxyz.bffweb.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PagoDetalle(
        UUID id,
        String tipo,
        String cuentaOrigen,
        String cuentaDestino,
        BigDecimal monto,
        String moneda,
        String estado,
        String descripcion,
        Instant fecha) {
}
