package cl.duoc.bancoxyz.account.api;

import cl.duoc.bancoxyz.account.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {
    private final CuentaService service;

    public CuentaController(CuentaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse abrir(@Valid @RequestBody AbrirCuentaRequest request) {
        return CuentaResponse.desde(service.abrir(request));
    }

    @GetMapping
    public List<CuentaResponse> listar(@RequestParam String rutCliente) {
        return service.listarPorCliente(rutCliente).stream().map(CuentaResponse::desde).toList();
    }

    @GetMapping("/{numero}")
    public CuentaResponse obtener(@PathVariable String numero) {
        return CuentaResponse.desde(service.obtener(numero));
    }

    @GetMapping("/{numero}/saldo")
    public SaldoResponse saldo(@PathVariable String numero) {
        return SaldoResponse.desde(service.obtener(numero));
    }

    @PostMapping("/{numero}/debitar")
    public CuentaResponse debitar(@PathVariable String numero, @Valid @RequestBody MovimientoCuentaRequest request) {
        return CuentaResponse.desde(service.debitar(numero, request.monto()));
    }

    @PostMapping("/{numero}/acreditar")
    public CuentaResponse acreditar(@PathVariable String numero, @Valid @RequestBody MovimientoCuentaRequest request) {
        return CuentaResponse.desde(service.acreditar(numero, request.monto()));
    }

    @DeleteMapping("/{numero}")
    public CuentaResponse cerrar(@PathVariable String numero) {
        return CuentaResponse.desde(service.cerrar(numero));
    }
}
