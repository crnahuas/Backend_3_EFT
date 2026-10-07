package cl.duoc.bancoxyz.payment.api;

import java.math.BigDecimal;
import java.util.UUID;

public record RetiroResponse(UUID operacionId, String estado, BigDecimal monto, BigDecimal saldoDisponible, String moneda) {
}
