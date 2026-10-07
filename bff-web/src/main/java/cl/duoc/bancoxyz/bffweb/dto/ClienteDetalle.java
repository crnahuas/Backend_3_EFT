package cl.duoc.bancoxyz.bffweb.dto;

public record ClienteDetalle(
        String rut,
        String nombres,
        String apellidos,
        String email,
        String telefono,
        String estado) {
}
