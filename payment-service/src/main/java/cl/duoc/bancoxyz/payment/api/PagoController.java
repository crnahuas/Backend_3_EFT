package cl.duoc.bancoxyz.payment.api;

import cl.duoc.bancoxyz.payment.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {
    private final PagoService service;

    public PagoController(PagoService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PagoResponse crear(@RequestHeader("X-Idempotency-Key") String key,
                              @Valid @RequestBody CrearPagoRequest request) {
        return PagoResponse.desde(service.procesar(key, request));
    }

    @PostMapping("/transferencias")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PagoResponse transferir(@RequestHeader("X-Idempotency-Key") String key,
                                   @Valid @RequestBody TransferenciaRequest request) {
        return PagoResponse.desde(service.transferir(key, request));
    }

    @PostMapping("/retiros")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RetiroResponse retirar(@RequestHeader("X-Idempotency-Key") String key,
                                  @Valid @RequestBody RetiroRequest request) {
        return service.retirar(key, request);
    }

    @GetMapping
    public List<PagoResponse> listar(@RequestParam String rutCliente,
                                     @RequestParam(defaultValue = "20") int limite) {
        return service.listar(rutCliente, limite).stream().map(PagoResponse::desde).toList();
    }
}
