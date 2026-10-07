package cl.duoc.bancoxyz.bffweb.config;

import cl.duoc.bancoxyz.bffweb.api.WebController;
import cl.duoc.bancoxyz.bffweb.client.WebBackendClient;
import cl.duoc.bancoxyz.bffweb.dto.ClienteDetalle;
import cl.duoc.bancoxyz.bffweb.dto.ResultadoBackend;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.service-registry.auto-registration.enabled=false"
})
@AutoConfigureMockMvc
class WebSecurityTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private WebBackendClient backend;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void rechazaDashboardSinAutenticacion() throws Exception {
        mvc.perform(get("/api/web/clientes/11111111-1/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaScopeDeOtroCanal() throws Exception {
        mvc.perform(get("/api/web/clientes/11111111-1/dashboard")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_mobile"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void permiteScopeWeb() throws Exception {
        String rut = "11111111-1";
        when(backend.obtenerCliente(rut)).thenReturn(ResultadoBackend.disponible(
                new ClienteDetalle(rut, "Ana", "Prueba", "ana@correo.cl", "+5691", "ACTIVO")));
        when(backend.obtenerCuentas(rut)).thenReturn(ResultadoBackend.disponible(List.of()));
        when(backend.obtenerPagos(rut)).thenReturn(ResultadoBackend.disponible(List.of()));

        mvc.perform(get("/api/web/clientes/{rut}/dashboard", rut)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_web"))))
                .andExpect(status().isOk());
    }
}
