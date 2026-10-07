package cl.duoc.bancoxyz.bffmobile.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenMovilResponse(
        String nombre,
        BigDecimal saldoTotal,
        String moneda,
        int cuentasActivas,
        List<MovimientoBreve> ultimosMovimientos,
        boolean datosParciales) {
}
