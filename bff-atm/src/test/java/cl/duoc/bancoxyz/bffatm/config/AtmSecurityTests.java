package cl.duoc.bancoxyz.bffatm.config;

import cl.duoc.bancoxyz.bffatm.api.AtmController;
import cl.duoc.bancoxyz.bffatm.client.AtmBackendClient;
import cl.duoc.bancoxyz.bffatm.dto.RetiroCajeroResponse;
import cl.duoc.bancoxyz.bffatm.dto.SaldoCajeroResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.service-registry.auto-registration.enabled=false"
})
@AutoConfigureMockMvc
class AtmSecurityTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AtmBackendClient backend;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void rechazaSaldoSinAutenticacion() throws Exception {
        mvc.perform(get("/api/cajero/cuentas/1001/saldo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaScopeDeOtroCanal() throws Exception {
        mvc.perform(get("/api/cajero/cuentas/1001/saldo")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_mobile"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void permiteConsultaConScopeLectura() throws Exception {
        when(backend.consultarSaldo("1001"))
                .thenReturn(new SaldoCajeroResponse("1001", new BigDecimal("100000"), "CLP"));

        mvc.perform(get("/api/cajero/cuentas/1001/saldo")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_atm.read"))))
                .andExpect(status().isOk());
    }

    @Test
    void scopeLecturaNoPermiteRetirar() throws Exception {
        mvc.perform(post("/api/cajero/retiros")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_atm.read")))
                        .header("X-Idempotency-Key", "retiro-seguridad-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"1001\",\"monto\":20000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void permiteRetiroConScopeEscritura() throws Exception {
        when(backend.retirar(any(), eq("retiro-seguridad-2"))).thenReturn(
                new RetiroCajeroResponse(UUID.randomUUID(), "APROBADO", new BigDecimal("20000"),
                        new BigDecimal("80000"), "CLP"));

        mvc.perform(post("/api/cajero/retiros")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_atm.withdraw")))
                        .header("X-Idempotency-Key", "retiro-seguridad-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuenta\":\"1001\",\"monto\":20000}"))
                .andExpect(status().isAccepted());
    }
}
