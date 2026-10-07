package cl.duoc.bancoxyz.bffatm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RetiroCajeroRequest(
        @NotBlank String numeroCuenta,
        @NotNull @DecimalMin("1000") BigDecimal monto) {
}
