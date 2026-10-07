package cl.duoc.bancoxyz.batch.processor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import cl.duoc.bancoxyz.batch.domain.InteresMensualInput;
import cl.duoc.bancoxyz.batch.domain.InteresMensualResultado;
import cl.duoc.bancoxyz.batch.support.LegacyParsing;

@Component
public class InteresMensualProcessor
        implements ItemProcessor<InteresMensualInput, InteresMensualResultado> {

    private static final BigDecimal TASA_AHORRO = new BigDecimal("0.01");
    private static final BigDecimal TASA_PRESTAMO = new BigDecimal("0.02");

    @Override
    public InteresMensualResultado process(InteresMensualInput input) {
        BigDecimal saldo = LegacyParsing.decimalOrNull(input.saldo());
        Integer edad = LegacyParsing.integerOrNull(input.edad());
        String tipo = LegacyParsing.normalizedLower(input.tipo());
        String nombre = LegacyParsing.trimToNull(input.nombre());
        List<String> errores = new ArrayList<>();

        if (input.cuentaId() == null || input.cuentaId() <= 0) {
            errores.add("cuenta invalida");
        }
        if (nombre == null) {
            errores.add("nombre ausente");
        }
        if (saldo == null) {
            errores.add("saldo ausente o no numerico");
        } else if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("saldo debe ser mayor que cero");
        }
        if (edad == null) {
            errores.add("edad ausente o no numerica");
        } else if (edad < 18 || edad > 90) {
            errores.add("edad fuera de rango 18 a 90");
        }
        if (!"ahorro".equals(tipo) && !"prestamo".equals(tipo)) {
            errores.add("tipo de cuenta no soportado");
        }

        BigDecimal tasa = "ahorro".equals(tipo) ? TASA_AHORRO
                : "prestamo".equals(tipo) ? TASA_PRESTAMO : BigDecimal.ZERO;
        BigDecimal interes = saldo == null ? BigDecimal.ZERO
                : saldo.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldoFinal = saldo == null ? null
                : saldo.add(interes).setScale(2, RoundingMode.HALF_UP);

        return new InteresMensualResultado(
                input.sourceFile(), input.cuentaId(), nombre,
                input.saldo(), saldo, input.edad(), edad, tipo,
                tasa, interes, saldoFinal,
                errores.isEmpty() ? "VALIDO" : "ANOMALIA",
                errores.isEmpty() ? "Interes calculado" : String.join("; ", errores));
    }
}
