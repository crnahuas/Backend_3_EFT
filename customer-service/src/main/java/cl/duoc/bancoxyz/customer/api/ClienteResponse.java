package cl.duoc.bancoxyz.customer.api;

import cl.duoc.bancoxyz.customer.domain.Cliente;

import java.time.Instant;

public record ClienteResponse(
        String rut, String nombres, String apellidos, String email, String telefono,
        String estado, Instant creadoEn, Instant actualizadoEn, Instant ultimaActividad) {

    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(cliente.getRut(), cliente.getNombres(), cliente.getApellidos(),
                cliente.getEmail(), cliente.getTelefono(), cliente.getEstado(), cliente.getCreadoEn(),
                cliente.getActualizadoEn(), cliente.getUltimaActividad());
    }
}
