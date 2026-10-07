package cl.duoc.bancoxyz.bffweb.dto;

import java.time.Instant;
import java.util.List;

public record DashboardWebResponse(
        ClienteDetalle cliente,
        List<CuentaDetalle> cuentas,
        List<PagoDetalle> pagosRecientes,
        List<String> advertencias,
        Instant generadoEn) {
}
