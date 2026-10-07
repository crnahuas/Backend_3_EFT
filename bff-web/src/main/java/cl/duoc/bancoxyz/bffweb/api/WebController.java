package cl.duoc.bancoxyz.bffweb.api;

import cl.duoc.bancoxyz.bffweb.client.WebBackendClient;
import cl.duoc.bancoxyz.bffweb.dto.CrearPagoRequest;
import cl.duoc.bancoxyz.bffweb.dto.DashboardWebResponse;
import cl.duoc.bancoxyz.bffweb.dto.PagoDetalle;
import cl.duoc.bancoxyz.bffweb.dto.ResultadoBackend;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/web")
public class WebController {

    private final WebBackendClient backend;

    public WebController(WebBackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/clientes/{rut}/dashboard")
    public DashboardWebResponse dashboard(@PathVariable String rut) {
        var cliente = backend.obtenerCliente(rut);
        var cuentas = backend.obtenerCuentas(rut);
        var pagos = backend.obtenerPagos(rut);
        List<String> advertencias = Stream.of(cliente, cuentas, pagos)
                .filter(resultado -> !resultado.disponible())
                .map(ResultadoBackend::advertencia)
                .toList();
        return new DashboardWebResponse(
                cliente.datos(), cuentas.datos(), pagos.datos(), advertencias, Instant.now());
    }

    @PostMapping("/pagos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagoDetalle crearPago(@RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                 @Valid @RequestBody CrearPagoRequest request) {
        return backend.crearPago(request, idempotencyKey);
    }
}
