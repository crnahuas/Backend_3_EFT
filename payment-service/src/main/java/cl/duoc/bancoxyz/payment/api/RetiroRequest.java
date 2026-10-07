package cl.duoc.bancoxyz.payment.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RetiroRequest(@NotBlank String numeroCuenta, @NotNull @DecimalMin("1000") BigDecimal monto) {
}
