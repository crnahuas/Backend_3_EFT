package cl.duoc.bancoxyz.payment.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AccountBackendClient {
    private final RestClient cuentas;

    public AccountBackendClient(@LoadBalanced RestClient.Builder builder,
                                @Value("${services.cuentas.url}") String cuentasUrl) {
        this.cuentas = builder.clone().baseUrl(cuentasUrl).build();
    }

    @CircuitBreaker(name = "cuentas", fallbackMethod = "cuentaNoDisponible")
    public CuentaBackendResponse obtener(String numero) {
        return cuentas.get().uri("/api/cuentas/{numero}", numero).retrieve().body(CuentaBackendResponse.class);
    }

    @CircuitBreaker(name = "cuentas", fallbackMethod = "movimientoNoDisponible")
    public CuentaBackendResponse debitar(String numero, BigDecimal monto) {
        return cuentas.post().uri("/api/cuentas/{numero}/debitar", numero)
                .body(Map.of("monto", monto)).retrieve().body(CuentaBackendResponse.class);
    }

    @CircuitBreaker(name = "cuentas", fallbackMethod = "movimientoNoDisponible")
    public CuentaBackendResponse acreditar(String numero, BigDecimal monto) {
        return cuentas.post().uri("/api/cuentas/{numero}/acreditar", numero)
                .body(Map.of("monto", monto)).retrieve().body(CuentaBackendResponse.class);
    }

    private CuentaBackendResponse cuentaNoDisponible(String numero, Throwable error) {
        throw new BackendUnavailableException("Servicio de cuentas no disponible", error);
    }

    private CuentaBackendResponse movimientoNoDisponible(String numero, BigDecimal monto, Throwable error) {
        throw new BackendUnavailableException("No fue posible modificar la cuenta", error);
    }
}
