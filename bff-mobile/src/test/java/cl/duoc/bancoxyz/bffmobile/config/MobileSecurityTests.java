package cl.duoc.bancoxyz.bffmobile.config;

import cl.duoc.bancoxyz.bffmobile.api.MobileController;
import cl.duoc.bancoxyz.bffmobile.client.MobileBackendClient;
import cl.duoc.bancoxyz.bffmobile.dto.ClienteMovil;
import cl.duoc.bancoxyz.bffmobile.dto.ResultadoBackend;
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
class MobileSecurityTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MobileBackendClient backend;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void rechazaResumenSinAutenticacion() throws Exception {
        mvc.perform(get("/api/mobile/clientes/11111111-1/resumen"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaScopeDeOtroCanal() throws Exception {
        mvc.perform(get("/api/mobile/clientes/11111111-1/resumen")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_web"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void permiteScopeMobile() throws Exception {
        String rut = "11111111-1";
        when(backend.obtenerCliente(rut)).thenReturn(ResultadoBackend.disponible(
                new ClienteMovil(rut, "Ana", "Prueba")));
        when(backend.obtenerCuentas(rut)).thenReturn(ResultadoBackend.disponible(List.of()));
        when(backend.obtenerMovimientos(rut)).thenReturn(ResultadoBackend.disponible(List.of()));

        mvc.perform(get("/api/mobile/clientes/{rut}/resumen", rut)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_mobile"))))
                .andExpect(status().isOk());
    }
}
