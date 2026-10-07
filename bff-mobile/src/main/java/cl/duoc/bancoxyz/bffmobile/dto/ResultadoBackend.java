package cl.duoc.bancoxyz.bffmobile.dto;

public record ResultadoBackend<T>(T datos, boolean disponible) {

    public static <T> ResultadoBackend<T> disponible(T datos) {
        return new ResultadoBackend<>(datos, true);
    }

    public static <T> ResultadoBackend<T> noDisponible(T valorAlternativo) {
        return new ResultadoBackend<>(valorAlternativo, false);
    }
}
