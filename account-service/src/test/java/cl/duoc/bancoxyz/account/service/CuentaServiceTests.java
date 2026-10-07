package cl.duoc.bancoxyz.account.service;

import cl.duoc.bancoxyz.account.domain.Cuenta;
import cl.duoc.bancoxyz.account.repository.CuentaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CuentaServiceTests {
    @Test
    void debitaConBloqueoYConservaSaldoCorrecto() {
        var repository = mock(CuentaRepository.class);
        var cuenta = new Cuenta("1001", "11.111.111-1", "VISTA", new BigDecimal("100000"), "CLP");
        when(repository.bloquearPorNumero("1001")).thenReturn(Optional.of(cuenta));

        var resultado = new CuentaService(repository).debitar("1001", new BigDecimal("25000"));

        assertThat(resultado.getSaldo()).isEqualByComparingTo("75000");
    }

    @Test
    void impideSobregiro() {
        var repository = mock(CuentaRepository.class);
        var cuenta = new Cuenta("1001", "11.111.111-1", "VISTA", new BigDecimal("1000"), "CLP");
        when(repository.bloquearPorNumero("1001")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> new CuentaService(repository).debitar("1001", new BigDecimal("1001")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Saldo insuficiente");
    }
}
