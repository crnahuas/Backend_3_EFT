package cl.duoc.bancoxyz.batch.processor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import cl.duoc.bancoxyz.batch.domain.EstadoFinancieroAnualInput;
import cl.duoc.bancoxyz.batch.domain.EstadoFinancieroAnualResultado;
import cl.duoc.bancoxyz.batch.support.LegacyParsing;

@Component
public class EstadoFinancieroAnualProcessor
        implements ItemProcessor<EstadoFinancieroAnualInput, EstadoFinancieroAnualResultado> {

    private static final Set<String> TIPOS_PERMITIDOS =
            Set.of("deposito", "retiro", "compra", "pago");

    @Override
    public EstadoFinancieroAnualResultado process(EstadoFinancieroAnualInput input) {
        LocalDate fecha = LegacyParsing.dateOrNull(input.fecha());
        BigDecimal monto = LegacyParsing.decimalOrNull(input.monto());
        String transaccion = LegacyParsing.normalizedLower(input.transaccion());
        String descripcion = LegacyParsing.trimToNull(input.descripcion());
        List<String> errores = new ArrayList<>();

        if (input.cuentaId() == null || input.cuentaId() <= 0) {
            errores.add("cuenta invalida");
        }
        if (fecha == null) {
            errores.add("fecha invalida");
        }
        if (!TIPOS_PERMITIDOS.contains(transaccion)) {
            errores.add("tipo de transaccion no soportado");
        }
        if (monto == null) {
            errores.add("monto ausente o no numerico");
        } else if (monto.compareTo(BigDecimal.ZERO) == 0) {
            errores.add("monto no puede ser cero");
        } else if ("deposito".equals(transaccion) && monto.signum() < 0) {
            errores.add("deposito debe ser positivo");
        } else if (("retiro".equals(transaccion)
                || "compra".equals(transaccion)
                || "pago".equals(transaccion)) && monto.signum() > 0) {
            errores.add("egreso debe ser negativo");
        }
        if (descripcion == null) {
            errores.add("descripcion ausente");
        }

        return new EstadoFinancieroAnualResultado(
                input.sourceFile(), input.cuentaId(), input.fecha(), fecha,
                transaccion, input.monto(), monto, descripcion,
                errores.isEmpty() ? "VALIDO" : "ANOMALIA",
                errores.isEmpty() ? "Sin observaciones" : String.join("; ", errores));
    }
}

