package cl.duoc.bancoxyz.bffatm.client;

import cl.duoc.bancoxyz.bffatm.config.BearerTokenRelay;
import cl.duoc.bancoxyz.bffatm.dto.RetiroCajeroRequest;
import cl.duoc.bancoxyz.bffatm.dto.RetiroCajeroResponse;
import cl.duoc.bancoxyz.bffatm.dto.SaldoCajeroResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AtmBackendClient {

    private final RestClient cuentas;
    private final RestClient pagos;

    public AtmBackendClient(
            @Value("${services.cuentas.url}") String cuentasUrl,
            @Value("${services.pagos.url}") String pagosUrl,
            @LoadBalanced RestClient.Builder builder) {
        cuentas = builder.clone().baseUrl(cuentasUrl).build();
        pagos = builder.clone().baseUrl(pagosUrl).build();
    }

    @CircuitBreaker(name = "cuentas", fallbackMethod = "saldoNoDisponible")
    public SaldoCajeroResponse consultarSaldo(String numeroCuenta) {
        return cuentas.get()
                .uri("/api/cuentas/{numeroCuenta}/saldo", numeroCuenta)
                .retrieve()
                .body(SaldoCajeroResponse.class);
    }

    @CircuitBreaker(name = "pagos", fallbackMethod = "retiroNoDisponible")
    public RetiroCajeroResponse retirar(RetiroCajeroRequest request, String idempotencyKey) {
        return pagos.post()
                .uri("/api/pagos/retiros")
                .header("X-Idempotency-Key", idempotencyKey)
                .body(request)
                .retrieve()
                .body(RetiroCajeroResponse.class);
    }

    private SaldoCajeroResponse saldoNoDisponible(String numeroCuenta, Throwable error) {
        throw new BackendUnavailableException("No fue posible consultar el saldo");
    }

    private RetiroCajeroResponse retiroNoDisponible(
            RetiroCajeroRequest request, String idempotencyKey, Throwable error) {
        throw new BackendUnavailableException("No fue posible procesar el retiro; no reintente sin conservar la clave de idempotencia");
    }
}
