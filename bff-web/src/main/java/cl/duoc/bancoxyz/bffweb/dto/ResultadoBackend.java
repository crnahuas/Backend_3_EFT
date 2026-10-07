package cl.duoc.bancoxyz.bffweb.dto;

public record ResultadoBackend<T>(T datos, boolean disponible, String advertencia) {

    public static <T> ResultadoBackend<T> disponible(T datos) {
        return new ResultadoBackend<>(datos, true, null);
    }

    public static <T> ResultadoBackend<T> noDisponible(T valorAlternativo, String advertencia) {
        return new ResultadoBackend<>(valorAlternativo, false, advertencia);
    }
}
