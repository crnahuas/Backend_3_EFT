package cl.duoc.bancoxyz.customer.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransaccionCompletadaEvent(
        UUID eventoId, UUID pagoId, String rutCliente, String tipo,
        String cuentaOrigen, String cuentaDestino, BigDecimal monto, String moneda, Instant fecha) {
}
