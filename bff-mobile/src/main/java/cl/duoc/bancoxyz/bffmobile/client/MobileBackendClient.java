package cl.duoc.bancoxyz.bffmobile.client;

import cl.duoc.bancoxyz.bffmobile.config.BearerTokenRelay;
import cl.duoc.bancoxyz.bffmobile.dto.ClienteMovil;
import cl.duoc.bancoxyz.bffmobile.dto.CuentaMovil;
import cl.duoc.bancoxyz.bffmobile.dto.MovimientoBreve;
import cl.duoc.bancoxyz.bffmobile.dto.ResultadoBackend;
import cl.duoc.bancoxyz.bffmobile.dto.TransferenciaMovilRequest;
import cl.duoc.bancoxyz.bffmobile.dto.TransferenciaMovilResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MobileBackendClient {

    private final RestClient clientes;
    private final RestClient cuentas;
    private final RestClient pagos;

    public MobileBackendClient(
            @Value("${services.clientes.url}") String clientesUrl,
            @Value("${services.cuentas.url}") String cuentasUrl,
            @Value("${services.pagos.url}") String pagosUrl,
            @LoadBalanced RestClient.Builder builder) {
        clientes = builder.clone().baseUrl(clientesUrl).build();
        cuentas = builder.clone().baseUrl(cuentasUrl).build();
        pagos = builder.clone().baseUrl(pagosUrl).build();
    }

    @CircuitBreaker(name = "clientes", fallbackMethod = "clienteAlternativo")
    public ResultadoBackend<ClienteMovil> obtenerCliente(String rut) {
        return ResultadoBackend.disponible(clientes.get()
                .uri("/api/clientes/{rut}", rut)
                .retrieve()
                .body(ClienteMovil.class));
    }

    @CircuitBreaker(name = "cuentas", fallbackMethod = "cuentasAlternativas")
    public ResultadoBackend<List<CuentaMovil>> obtenerCuentas(String rut) {
        var response = cuentas.get()
                .uri(uri -> uri.path("/api/cuentas").queryParam("rutCliente", rut).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CuentaMovil>>() { });
        return ResultadoBackend.disponible(response == null ? List.of() : response);
    }

    @CircuitBreaker(name = "pagos", fallbackMethod = "movimientosAlternativos")
    public ResultadoBackend<List<MovimientoBreve>> obtenerMovimientos(String rut) {
        var response = pagos.get()
                .uri(uri -> uri.path("/api/pagos").queryParam("rutCliente", rut).queryParam("limite", 5).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<MovimientoBreve>>() { });
        return ResultadoBackend.disponible(response == null ? List.of() : response);
    }

    @CircuitBreaker(name = "pagos", fallbackMethod = "transferenciaNoDisponible")
    public TransferenciaMovilResponse transferir(TransferenciaMovilRequest request, String idempotencyKey) {
        return pagos.post()
                .uri("/api/pagos/transferencias")
                .header("X-Idempotency-Key", idempotencyKey)
                .body(request)
                .retrieve()
                .body(TransferenciaMovilResponse.class);
    }

    private ResultadoBackend<ClienteMovil> clienteAlternativo(String rut, Throwable error) {
        return ResultadoBackend.noDisponible(null);
    }

    private ResultadoBackend<List<CuentaMovil>> cuentasAlternativas(String rut, Throwable error) {
        return ResultadoBackend.noDisponible(List.of());
    }

    private ResultadoBackend<List<MovimientoBreve>> movimientosAlternativos(String rut, Throwable error) {
        return ResultadoBackend.noDisponible(List.of());
    }

    private TransferenciaMovilResponse transferenciaNoDisponible(
            TransferenciaMovilRequest request, String idempotencyKey, Throwable error) {
        throw new BackendUnavailableException("No fue posible procesar la transferencia");
    }
}
