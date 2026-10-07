package cl.duoc.bancoxyz.payment.client;

import java.math.BigDecimal;

public record CuentaBackendResponse(
        String numeroCuenta, String rutCliente, String tipo, BigDecimal saldo, String moneda, String estado) {
}
