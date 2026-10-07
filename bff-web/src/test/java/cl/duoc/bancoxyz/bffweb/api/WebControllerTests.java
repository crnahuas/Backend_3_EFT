package cl.duoc.bancoxyz.bffweb.api;

import cl.duoc.bancoxyz.bffweb.client.WebBackendClient;
import cl.duoc.bancoxyz.bffweb.dto.ClienteDetalle;
import cl.duoc.bancoxyz.bffweb.dto.CuentaDetalle;
import cl.duoc.bancoxyz.bffweb.dto.ResultadoBackend;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebControllerTests {

    @Test
    void dashboardEntregaDatosCompletosYAdvierteUnaDegradacion() {
        var backend = mock(WebBackendClient.class);
        var cliente = new ClienteDetalle("11.111.111-1", "Ana", "Perez", "ana@correo.cl", "+5691", "ACTIVO");
        var cuenta = new CuentaDetalle("1001", "VISTA", new BigDecimal("250000"), "CLP", "ACTIVA");
        when(backend.obtenerCliente(cliente.rut())).thenReturn(ResultadoBackend.disponible(cliente));
        when(backend.obtenerCuentas(cliente.rut())).thenReturn(ResultadoBackend.disponible(List.of(cuenta)));
        when(backend.obtenerPagos(cliente.rut()))
                .thenReturn(ResultadoBackend.noDisponible(List.of(), "Pagos temporalmente no disponibles"));

        var response = new WebController(backend).dashboard(cliente.rut());

        assertThat(response.cliente()).isEqualTo(cliente);
        assertThat(response.cuentas()).containsExactly(cuenta);
        assertThat(response.pagosRecientes()).isEmpty();
        assertThat(response.advertencias()).containsExactly("Pagos temporalmente no disponibles");
    }
}
