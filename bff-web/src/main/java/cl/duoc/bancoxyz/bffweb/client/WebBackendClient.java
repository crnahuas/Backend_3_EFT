package cl.duoc.bancoxyz.bffweb.client;

import cl.duoc.bancoxyz.bffweb.dto.ClienteDetalle;
import cl.duoc.bancoxyz.bffweb.config.BearerTokenRelay;
import cl.duoc.bancoxyz.bffweb.dto.CrearPagoRequest;
import cl.duoc.bancoxyz.bffweb.dto.CuentaDetalle;
import cl.duoc.bancoxyz.bffweb.dto.PagoDetalle;
import cl.duoc.bancoxyz.bffweb.dto.ResultadoBackend;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class WebBackendClient {

    private final RestClient clientes;
    private final RestClient cuentas;
    private final RestClient pagos;

    public WebBackendClient(
            @Value("${services.clientes.url}") String clientesUrl,
            @Value("${services.cuentas.url}") String cuentasUrl,
            @Value("${services.pagos.url}") String pagosUrl,
            @LoadBalanced RestClient.Builder builder) {
        this.clientes = builder.clone().baseUrl(clientesUrl).build();
        this.cuentas = builder.clone().baseUrl(cuentasUrl).build();
        this.pagos = builder.clone().baseUrl(pagosUrl).build();
    }

    @CircuitBreaker(name = "clientes", fallbackMethod = "clienteAlternativo")
    public ResultadoBackend<ClienteDetalle> obtenerCliente(String rut) {
        var response = clientes.get()
                .uri("/api/clientes/{rut}", rut)
                .retrieve()
                .body(ClienteDetalle.class);
        return ResultadoBackend.disponible(response);
    }

    @CircuitBreaker(name = "cuentas", fallbackMethod = "cuentasAlternativas")
    public ResultadoBackend<List<CuentaDetalle>> obtenerCuentas(String rut) {
        var response = cuentas.get()
                .uri(uri -> uri.path("/api/cuentas").queryParam("rutCliente", rut).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CuentaDetalle>>() { });
        return ResultadoBackend.disponible(response == null ? List.of() : response);
    }

    @CircuitBreaker(name = "pagos", fallbackMethod = "pagosAlternativos")
    public ResultadoBackend<List<PagoDetalle>> obtenerPagos(String rut) {
        var response = pagos.get()
                .uri(uri -> uri.path("/api/pagos").queryParam("rutCliente", rut).queryParam("limite", 20).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<PagoDetalle>>() { });
        return ResultadoBackend.disponible(response == null ? List.of() : response);
    }

    @CircuitBreaker(name = "pagos", fallbackMethod = "pagoNoDisponible")
    public PagoDetalle crearPago(CrearPagoRequest request, String idempotencyKey) {
        return pagos.post()
                .uri("/api/pagos")
                .header("X-Idempotency-Key", idempotencyKey)
                .body(request)
                .retrieve()
                .body(PagoDetalle.class);
    }

    private ResultadoBackend<ClienteDetalle> clienteAlternativo(String rut, Throwable error) {
        return ResultadoBackend.noDisponible(null, "Datos personales temporalmente no disponibles");
    }

    private ResultadoBackend<List<CuentaDetalle>> cuentasAlternativas(String rut, Throwable error) {
        return ResultadoBackend.noDisponible(List.of(), "Cuentas temporalmente no disponibles");
    }

    private ResultadoBackend<List<PagoDetalle>> pagosAlternativos(String rut, Throwable error) {
        return ResultadoBackend.noDisponible(List.of(), "Pagos temporalmente no disponibles");
    }

    private PagoDetalle pagoNoDisponible(CrearPagoRequest request, String idempotencyKey, Throwable error) {
        throw new BackendUnavailableException("El servicio de pagos no esta disponible");
    }
}
