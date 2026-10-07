package cl.duoc.bancoxyz.bffmobile.api;

import cl.duoc.bancoxyz.bffmobile.client.MobileBackendClient;
import cl.duoc.bancoxyz.bffmobile.dto.CuentaMovil;
import cl.duoc.bancoxyz.bffmobile.dto.ResumenMovilResponse;
import cl.duoc.bancoxyz.bffmobile.dto.TransferenciaMovilRequest;
import cl.duoc.bancoxyz.bffmobile.dto.TransferenciaMovilResponse;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/mobile")
public class MobileController {

    private final MobileBackendClient backend;

    public MobileController(MobileBackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/clientes/{rut}/resumen")
    public ResumenMovilResponse resumen(@PathVariable String rut) {
        var cliente = backend.obtenerCliente(rut);
        var cuentas = backend.obtenerCuentas(rut);
        var movimientos = backend.obtenerMovimientos(rut);
        var activas = cuentas.datos().stream().filter(cuenta -> "ACTIVA".equalsIgnoreCase(cuenta.estado())).toList();
        BigDecimal saldo = activas.stream()
                .map(CuentaMovil::saldo)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String nombre = cliente.datos() == null
                ? null
                : (cliente.datos().nombres() + " " + cliente.datos().apellidos()).trim();
        String moneda = activas.stream().map(CuentaMovil::moneda).findFirst().orElse("CLP");
        boolean parcial = !cliente.disponible() || !cuentas.disponible() || !movimientos.disponible();
        return new ResumenMovilResponse(
                nombre, saldo, moneda, activas.size(), movimientos.datos(), parcial);
    }

    @PostMapping("/transferencias")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransferenciaMovilResponse transferir(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferenciaMovilRequest request) {
        return backend.transferir(request, idempotencyKey);
    }
}
