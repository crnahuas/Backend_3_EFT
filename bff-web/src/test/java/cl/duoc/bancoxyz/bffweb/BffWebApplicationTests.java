package cl.duoc.bancoxyz.bffweb;

import cl.duoc.bancoxyz.bffweb.client.WebBackendClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "services.clientes.url=http://127.0.0.1:1",
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.service-registry.auto-registration.enabled=false"
})
class BffWebApplicationTests {

    @Autowired
    private WebBackendClient backend;

    @Test
    void contextoCarga() {
    }

    @Test
    void circuitBreakerEntregaRespuestaAlternativaAnteFalla() {
        var resultado = backend.obtenerCliente("11.111.111-1");

        assertThat(resultado.disponible()).isFalse();
        assertThat(resultado.advertencia()).contains("temporalmente");
    }
}
