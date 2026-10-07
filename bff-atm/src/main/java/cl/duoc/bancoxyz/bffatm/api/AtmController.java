package cl.duoc.bancoxyz.bffatm.api;

import cl.duoc.bancoxyz.bffatm.client.AtmBackendClient;
import cl.duoc.bancoxyz.bffatm.dto.RetiroCajeroRequest;
import cl.duoc.bancoxyz.bffatm.dto.RetiroCajeroResponse;
import cl.duoc.bancoxyz.bffatm.dto.SaldoCajeroResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cajero")
public class AtmController {

    private final AtmBackendClient backend;
    private final BigDecimal montoMaximo;

    public AtmController(
            AtmBackendClient backend,
            @Value("${atm.retiro.monto-maximo:200000}") BigDecimal montoMaximo) {
        this.backend = backend;
        this.montoMaximo = montoMaximo;
    }

    @GetMapping("/cuentas/{numeroCuenta}/saldo")
    public SaldoCajeroResponse consultarSaldo(@PathVariable String numeroCuenta) {
        return backend.consultarSaldo(numeroCuenta);
    }

    @PostMapping("/retiros")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RetiroCajeroResponse retirar(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody RetiroCajeroRequest request) {
        if (request.monto().compareTo(montoMaximo) > 0) {
            throw new IllegalArgumentException("El monto supera el maximo permitido por operacion");
        }
        return backend.retirar(request, idempotencyKey);
    }
}
