package cl.duoc.bancoxyz.bffatm.client;

public class BackendUnavailableException extends RuntimeException {

    public BackendUnavailableException(String message) {
        super(message);
    }
}
