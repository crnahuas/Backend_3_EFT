package cl.duoc.bancoxyz.account.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimientoCuentaRequest(@NotNull @DecimalMin("0.01") BigDecimal monto) {
}
