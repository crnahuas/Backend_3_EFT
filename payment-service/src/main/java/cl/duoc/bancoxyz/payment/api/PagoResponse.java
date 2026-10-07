package cl.duoc.bancoxyz.payment.api;

import cl.duoc.bancoxyz.payment.domain.Pago;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PagoResponse(
        UUID id, String tipo, String cuentaOrigen, String cuentaDestino, BigDecimal monto,
        String moneda, String estado, String descripcion, String error, Instant fecha) {
    public static PagoResponse desde(Pago pago) {
        return new PagoResponse(pago.getId(), pago.getTipo(), pago.getCuentaOrigen(), pago.getCuentaDestino(),
                pago.getMonto(), pago.getMoneda(), pago.getEstado(), pago.getDescripcion(), pago.getError(), pago.getFecha());
    }
}
