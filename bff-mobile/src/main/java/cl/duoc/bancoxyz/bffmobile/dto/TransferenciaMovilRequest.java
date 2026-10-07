package cl.duoc.bancoxyz.bffmobile.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferenciaMovilRequest(
        @NotBlank String cuentaOrigen,
        @NotBlank String cuentaDestino,
        @NotNull @DecimalMin("0.01") BigDecimal monto,
        @Size(max = 80) String descripcion) {
}
