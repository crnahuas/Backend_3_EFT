package cl.duoc.bancoxyz.account.integration;

import cl.duoc.bancoxyz.account.api.AbrirCuentaRequest;
import cl.duoc.bancoxyz.account.repository.CuentaRepository;
import cl.duoc.bancoxyz.account.service.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.service-registry.auto-registration.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://127.0.0.1:1/jwks"
})
class CuentaPostgresIntegrationTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("account_test")
            .withUsername("bancoxyz")
            .withPassword("bancoxyz");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CuentaService service;

    @Autowired
    private CuentaRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void persisteDebitoYCreditoEnPostgresql() {
        var cuenta = service.abrir(new AbrirCuentaRequest(
                "11111111-1", "VISTA", new BigDecimal("100000"), "CLP"));

        service.debitar(cuenta.getNumeroCuenta(), new BigDecimal("25000"));
        service.acreditar(cuenta.getNumeroCuenta(), new BigDecimal("5000"));

        var persistida = repository.findById(cuenta.getNumeroCuenta()).orElseThrow();
        assertThat(persistida.getSaldo()).isEqualByComparingTo("80000");
        assertThat(persistida.getEstado()).isEqualTo("ACTIVA");
    }
}
