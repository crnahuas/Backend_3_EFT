package cl.duoc.bancoxyz.account.api;

import cl.duoc.bancoxyz.account.domain.Cuenta;

import java.math.BigDecimal;

public record SaldoResponse(String numeroCuenta, BigDecimal saldoDisponible, String moneda) {
    public static SaldoResponse desde(Cuenta cuenta) {
        return new SaldoResponse(cuenta.getNumeroCuenta(), cuenta.getSaldo(), cuenta.getMoneda());
    }
}
