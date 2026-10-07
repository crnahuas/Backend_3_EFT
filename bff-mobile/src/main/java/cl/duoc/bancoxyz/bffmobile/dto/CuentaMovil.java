package cl.duoc.bancoxyz.bffmobile.dto;

import java.math.BigDecimal;

public record CuentaMovil(String numeroCuenta, BigDecimal saldo, String moneda, String estado) {
}
