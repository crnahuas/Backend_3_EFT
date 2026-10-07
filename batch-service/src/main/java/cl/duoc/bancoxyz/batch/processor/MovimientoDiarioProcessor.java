package cl.duoc.bancoxyz.batch.processor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import cl.duoc.bancoxyz.batch.domain.MovimientoDiarioInput;
import cl.duoc.bancoxyz.batch.domain.MovimientoDiarioResultado;
import cl.duoc.bancoxyz.batch.support.LegacyParsing;

@Component
public class MovimientoDiarioProcessor
        implements ItemProcessor<MovimientoDiarioInput, MovimientoDiarioResultado> {

    @Override
    public MovimientoDiarioResultado process(MovimientoDiarioInput input) {
        LocalDate fecha = LegacyParsing.dateOrNull(input.fecha());
        BigDecimal monto = LegacyParsing.decimalOrNull(input.monto());
        String tipo = LegacyParsing.normalizedLower(input.tipo());
        List<String> errores = new ArrayList<>();

        if (input.id() == null || input.id() <= 0) {
            errores.add("identificador invalido");
        }
        if (fecha == null) {
            errores.add("fecha invalida");
        }
        if (monto == null) {
            errores.add("monto ausente o no numerico");
        } else if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("monto debe ser mayor que cero");
        }
        if (!"debito".equals(tipo) && !"credito".equals(tipo)) {
            errores.add("tipo debe ser debito o credito");
        }

        return new MovimientoDiarioResultado(
                input.sourceFile(),
                input.id(),
                input.fecha(),
                fecha,
                input.monto(),
                monto,
                tipo,
                errores.isEmpty() ? "VALIDO" : "ANOMALIA",
                errores.isEmpty() ? "Sin observaciones" : String.join("; ", errores));
    }
}

