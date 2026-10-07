package cl.duoc.bancoxyz.payment.service;

import cl.duoc.bancoxyz.payment.api.TransferenciaRequest;
import cl.duoc.bancoxyz.payment.client.AccountBackendClient;
import cl.duoc.bancoxyz.payment.client.BackendUnavailableException;
import cl.duoc.bancoxyz.payment.client.CuentaBackendResponse;
import cl.duoc.bancoxyz.payment.event.PagoEventPublisher;
import cl.duoc.bancoxyz.payment.repository.PagoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PagoServiceTests {
    @Test
    void compensaElDebitoSiFallaElAbonoDestino() {
        var repository = mock(PagoRepository.class);
        var cuentas = mock(AccountBackendClient.class);
        var publisher = mock(PagoEventPublisher.class);
        var origen = new CuentaBackendResponse("1001", "11.111.111-1", "VISTA", new BigDecimal("100000"), "CLP", "ACTIVA");
        var destino = new CuentaBackendResponse("2002", "22.222.222-2", "VISTA", new BigDecimal("50000"), "CLP", "ACTIVA");
        when(repository.findByIdempotencyKey("tx-1")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cuentas.obtener("1001")).thenReturn(origen);
        when(cuentas.obtener("2002")).thenReturn(destino);
        when(cuentas.debitar("1001", new BigDecimal("10000")))
                .thenReturn(new CuentaBackendResponse("1001", origen.rutCliente(), "VISTA", new BigDecimal("90000"), "CLP", "ACTIVA"));
        when(cuentas.acreditar("2002", new BigDecimal("10000")))
                .thenThrow(new BackendUnavailableException("sin servicio", new RuntimeException()));
        when(cuentas.acreditar("1001", new BigDecimal("10000"))).thenReturn(origen);

        var pago = new PagoService(repository, cuentas, publisher, new BigDecimal("1000000"))
                .transferir("tx-1", new TransferenciaRequest("1001", "2002", new BigDecimal("10000"), "prueba"));

        assertThat(pago.getEstado()).isEqualTo("RECHAZADO");
        verify(cuentas).acreditar("1001", new BigDecimal("10000"));
        verify(publisher).alerta(pago, "OPERACION_RECHAZADA");
    }
}
