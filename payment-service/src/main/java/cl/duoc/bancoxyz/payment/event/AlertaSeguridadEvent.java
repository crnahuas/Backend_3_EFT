package cl.duoc.bancoxyz.payment.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AlertaSeguridadEvent(
        UUID eventoId, UUID pagoId, String rutCliente, String motivo, BigDecimal monto, Instant fecha) {
}
