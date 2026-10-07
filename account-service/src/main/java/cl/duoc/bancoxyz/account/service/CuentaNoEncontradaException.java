package cl.duoc.bancoxyz.account.service;

public class CuentaNoEncontradaException extends RuntimeException {
    public CuentaNoEncontradaException(String numero) { super("Cuenta no encontrada: " + numero); }
}
