package cl.duoc.bancoxyz.bffmobile.api;

import cl.duoc.bancoxyz.bffmobile.client.MobileBackendClient;
import cl.duoc.bancoxyz.bffmobile.dto.ClienteMovil;
import cl.duoc.bancoxyz.bffmobile.dto.CuentaMovil;
import cl.duoc.bancoxyz.bffmobile.dto.ResultadoBackend;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MobileControllerTests {

    @Test
    void resumenSoloSumaCuentasActivasYMarcaDatosParciales() {
        var backend = mock(MobileBackendClient.class);
        String rut = "11.111.111-1";
        when(backend.obtenerCliente(rut))
                .thenReturn(ResultadoBackend.disponible(new ClienteMovil(rut, "Ana", "Perez")));
        when(backend.obtenerCuentas(rut)).thenReturn(ResultadoBackend.disponible(List.of(
                new CuentaMovil("1001", new BigDecimal("1000"), "CLP", "ACTIVA"),
                new CuentaMovil("1002", new BigDecimal("5000"), "CLP", "CERRADA"))));
        when(backend.obtenerMovimientos(rut)).thenReturn(ResultadoBackend.noDisponible(List.of()));

        var response = new MobileController(backend).resumen(rut);

        assertThat(response.nombre()).isEqualTo("Ana Perez");
        assertThat(response.saldoTotal()).isEqualByComparingTo("1000");
        assertThat(response.cuentasActivas()).isOne();
        assertThat(response.datosParciales()).isTrue();
    }
}
