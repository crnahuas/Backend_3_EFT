package cl.duoc.bancoxyz.customer.service;

import cl.duoc.bancoxyz.customer.api.ClienteRequest;
import cl.duoc.bancoxyz.customer.domain.Cliente;
import cl.duoc.bancoxyz.customer.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente crear(ClienteRequest request) {
        if (repository.existsById(request.rut())) {
            throw new IllegalArgumentException("El cliente ya existe");
        }
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("El correo ya esta registrado");
        }
        return repository.save(new Cliente(request.rut(), request.nombres(), request.apellidos(),
                request.email(), request.telefono()));
    }

    @Transactional(readOnly = true)
    public Cliente obtener(String rut) {
        return repository.findById(rut).orElseThrow(() -> new ClienteNoEncontradoException(rut));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return repository.findAll();
    }

    public Cliente actualizar(String rut, ClienteRequest request) {
        var cliente = obtener(rut);
        cliente.actualizar(request.nombres(), request.apellidos(), request.email(), request.telefono());
        return cliente;
    }

    public Cliente cambiarEstado(String rut, String estado) {
        if (!List.of("ACTIVO", "BLOQUEADO", "INACTIVO").contains(estado.toUpperCase())) {
            throw new IllegalArgumentException("Estado de cliente invalido");
        }
        var cliente = obtener(rut);
        cliente.cambiarEstado(estado.toUpperCase());
        return cliente;
    }

    public void registrarActividad(String rut, Instant fecha) {
        repository.findById(rut).ifPresent(cliente -> cliente.registrarActividad(fecha));
    }
}
