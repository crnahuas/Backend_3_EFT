package cl.duoc.bancoxyz.batch.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import cl.duoc.bancoxyz.batch.domain.EstadoFinancieroAnualInput;
import cl.duoc.bancoxyz.batch.domain.InteresMensualInput;
import cl.duoc.bancoxyz.batch.domain.MovimientoDiarioInput;

class LegacyProcessorsTests {

    @Test
    void aceptaLosFormatosDeFechaPresentesEnLosArchivosLegacy() throws Exception {
        var processor = new MovimientoDiarioProcessor();

        var iso = processor.process(new MovimientoDiarioInput("semana_1", 1L,
                "2024-01-01", "1000", "debito"));
        var slash = processor.process(new MovimientoDiarioInput("semana_3", 2L,
                "2024/01/04", "800", "credito"));
        var chileno = processor.process(new MovimientoDiarioInput("semana_3", 3L,
                "02/04/2024", "1200", "credito"));

        assertEquals("VALIDO", iso.estado());
        assertEquals("VALIDO", slash.estado());
        assertEquals("VALIDO", chileno.estado());
    }

    @Test
    void conservaYClasificaLosErroresDeMovimientoSinPerderElRegistro() throws Exception {
        var result = new MovimientoDiarioProcessor().process(
                new MovimientoDiarioInput("semana_3", 4L, "fecha-mala", "-200", "invalido"));

        assertEquals("ANOMALIA", result.estado());
        assertTrue(result.observacion().contains("fecha invalida"));
        assertTrue(result.observacion().contains("monto debe ser mayor que cero"));
        assertTrue(result.observacion().contains("tipo debe ser debito o credito"));
    }

    @Test
    void calculaInteresYMarcaEdadesFueraDeRango() throws Exception {
        var processor = new InteresMensualProcessor();
        var valid = processor.process(new InteresMensualInput(
                "semana_1", 101L, "John Doe", "5000", "30", "ahorro"));
        var invalid = processor.process(new InteresMensualInput(
                "semana_3", 139L, "John Doe", "7000", "100", "ahorro"));

        assertEquals(new BigDecimal("50.00"), valid.interesCalculado());
        assertEquals(new BigDecimal("5050.00"), valid.saldoFinal());
        assertEquals("VALIDO", valid.estado());
        assertEquals("ANOMALIA", invalid.estado());
    }

    @Test
    void validaElSignoDeLosMovimientosAnuales() throws Exception {
        var processor = new EstadoFinancieroAnualProcessor();
        var valid = processor.process(new EstadoFinancieroAnualInput(
                "semana_1", 101L, "2024-03-15", "retiro", "-500", "Retiro parcial"));
        var invalid = processor.process(new EstadoFinancieroAnualInput(
                "semana_3", 103L, "2024-05-09", "retiro", "1000", "Ingreso"));

        assertEquals("VALIDO", valid.estado());
        assertEquals("ANOMALIA", invalid.estado());
        assertTrue(invalid.observacion().contains("egreso debe ser negativo"));
    }
}
