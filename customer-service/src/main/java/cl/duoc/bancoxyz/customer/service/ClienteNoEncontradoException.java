package cl.duoc.bancoxyz.customer.service;

public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException(String rut) {
        super("Cliente no encontrado: " + rut);
    }
}
