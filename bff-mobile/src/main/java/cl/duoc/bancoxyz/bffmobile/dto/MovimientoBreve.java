package cl.duoc.bancoxyz.bffmobile.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovimientoBreve(UUID id, String tipo, BigDecimal monto, String estado, Instant fecha) {
}
