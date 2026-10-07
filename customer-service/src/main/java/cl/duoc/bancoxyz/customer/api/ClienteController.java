package cl.duoc.bancoxyz.customer.api;

import cl.duoc.bancoxyz.customer.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.desde(service.crear(request));
    }

    @GetMapping("/{rut}")
    public ClienteResponse obtener(@PathVariable String rut) {
        return ClienteResponse.desde(service.obtener(rut));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar().stream().map(ClienteResponse::desde).toList();
    }

    @PutMapping("/{rut}")
    public ClienteResponse actualizar(@PathVariable String rut, @Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.desde(service.actualizar(rut, request));
    }

    @PatchMapping("/{rut}/estado/{estado}")
    public ClienteResponse cambiarEstado(@PathVariable String rut, @PathVariable String estado) {
        return ClienteResponse.desde(service.cambiarEstado(rut, estado));
    }
}
