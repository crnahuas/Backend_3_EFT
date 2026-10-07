package cl.duoc.bancoxyz.account.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AbrirCuentaRequest(
        @NotBlank String rutCliente,
        @NotBlank String tipo,
        @NotNull @DecimalMin("0.00") BigDecimal saldoInicial,
        @NotBlank @Size(min = 3, max = 3) String moneda) {
}
