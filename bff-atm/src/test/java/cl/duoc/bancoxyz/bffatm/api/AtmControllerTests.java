package cl.duoc.bancoxyz.bffatm.api;

import cl.duoc.bancoxyz.bffatm.client.AtmBackendClient;
import cl.duoc.bancoxyz.bffatm.dto.RetiroCajeroRequest;
import cl.duoc.bancoxyz.bffatm.dto.RetiroCajeroResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtmControllerTests {

    @Test
    void retiroConservaClaveDeIdempotencia() {
        var backend = mock(AtmBackendClient.class);
        var request = new RetiroCajeroRequest("1001", new BigDecimal("20000"));
        var expected = new RetiroCajeroResponse(
                UUID.randomUUID(), "ACEPTADO", request.monto(), new BigDecimal("80000"), "CLP");
        when(backend.retirar(request, "atm-001")).thenReturn(expected);

        var response = new AtmController(backend, new BigDecimal("200000")).retirar("atm-001", request);

        assertThat(response).isEqualTo(expected);
        verify(backend).retirar(request, "atm-001");
    }

    @Test
    void rechazaRetiroSobreElMaximoAntesDeInvocarBackend() {
        var backend = mock(AtmBackendClient.class);
        var controller = new AtmController(backend, new BigDecimal("200000"));
        var request = new RetiroCajeroRequest("1001", new BigDecimal("200001"));

        assertThatThrownBy(() -> controller.retirar("atm-002", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximo permitido");
    }
}
