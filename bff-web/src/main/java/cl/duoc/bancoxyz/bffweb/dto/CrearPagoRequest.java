package cl.duoc.bancoxyz.bffweb.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CrearPagoRequest(
        @NotBlank String tipo,
        @NotBlank String cuentaOrigen,
        String cuentaDestino,
        @NotNull @DecimalMin(value = "0.01") BigDecimal monto,
        @NotBlank @Size(min = 3, max = 3) String moneda,
        @Size(max = 120) String descripcion) {
}
