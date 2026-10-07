package cl.duoc.bancoxyz.account.api;

import cl.duoc.bancoxyz.account.domain.Cuenta;

import java.math.BigDecimal;
import java.time.Instant;

public record CuentaResponse(
        String numeroCuenta, String rutCliente, String tipo, BigDecimal saldo,
        String moneda, String estado, Instant creadaEn, Instant actualizadaEn) {
    public static CuentaResponse desde(Cuenta cuenta) {
        return new CuentaResponse(cuenta.getNumeroCuenta(), cuenta.getRutCliente(), cuenta.getTipo(),
                cuenta.getSaldo(), cuenta.getMoneda(), cuenta.getEstado(), cuenta.getCreadaEn(), cuenta.getActualizadaEn());
    }
}
