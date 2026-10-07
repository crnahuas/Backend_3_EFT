package cl.duoc.bancoxyz.customer.service;

import cl.duoc.bancoxyz.customer.api.ClienteRequest;
import cl.duoc.bancoxyz.customer.repository.ClienteRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClienteServiceTests {
    @Test
    void creaClienteActivo() {
        var repository = mock(ClienteRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var request = new ClienteRequest("11.111.111-1", "Ana", "Perez", "ana@correo.cl", "+5691");

        var cliente = new ClienteService(repository).crear(request);

        assertThat(cliente.getRut()).isEqualTo(request.rut());
        assertThat(cliente.getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    void rechazaRutDuplicado() {
        var repository = mock(ClienteRepository.class);
        when(repository.existsById("11.111.111-1")).thenReturn(true);
        var request = new ClienteRequest("11.111.111-1", "Ana", "Perez", "ana@correo.cl", null);

        assertThatThrownBy(() -> new ClienteService(repository).crear(request))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ya existe");
    }
}
