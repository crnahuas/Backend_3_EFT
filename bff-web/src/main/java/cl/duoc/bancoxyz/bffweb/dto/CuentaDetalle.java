package cl.duoc.bancoxyz.bffweb.dto;

import java.math.BigDecimal;

public record CuentaDetalle(
        String numeroCuenta,
        String tipo,
        BigDecimal saldo,
        String moneda,
        String estado) {
}
