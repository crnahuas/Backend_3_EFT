package cl.duoc.bancoxyz.bffmobile.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferenciaMovilResponse(UUID id, String estado, BigDecimal monto) {
}
