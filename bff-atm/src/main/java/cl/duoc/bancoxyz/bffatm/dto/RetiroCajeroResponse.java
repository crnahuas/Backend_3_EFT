package cl.duoc.bancoxyz.bffatm.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RetiroCajeroResponse(
        UUID operacionId,
        String estado,
        BigDecimal monto,
        BigDecimal saldoDisponible,
        String moneda) {
}
