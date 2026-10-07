package cl.duoc.bancoxyz.bffatm.dto;

import java.math.BigDecimal;

public record SaldoCajeroResponse(String numeroCuenta, BigDecimal saldoDisponible, String moneda) {
}
