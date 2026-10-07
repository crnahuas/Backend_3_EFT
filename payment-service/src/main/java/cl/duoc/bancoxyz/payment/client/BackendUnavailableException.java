package cl.duoc.bancoxyz.payment.client;

public class BackendUnavailableException extends RuntimeException {
    public BackendUnavailableException(String message, Throwable cause) { super(message, cause); }
}
