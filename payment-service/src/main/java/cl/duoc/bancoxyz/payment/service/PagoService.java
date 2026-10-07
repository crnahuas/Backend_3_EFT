package cl.duoc.bancoxyz.payment.service;

import cl.duoc.bancoxyz.payment.api.CrearPagoRequest;
import cl.duoc.bancoxyz.payment.api.RetiroRequest;
import cl.duoc.bancoxyz.payment.api.RetiroResponse;
import cl.duoc.bancoxyz.payment.api.TransferenciaRequest;
import cl.duoc.bancoxyz.payment.client.AccountBackendClient;
import cl.duoc.bancoxyz.payment.client.CuentaBackendResponse;
import cl.duoc.bancoxyz.payment.domain.Pago;
import cl.duoc.bancoxyz.payment.event.PagoEventPublisher;
import cl.duoc.bancoxyz.payment.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PagoService {
    private final PagoRepository repository;
    private final AccountBackendClient cuentas;
    private final PagoEventPublisher publisher;
    private final BigDecimal umbralAlerta;

    public PagoService(PagoRepository repository, AccountBackendClient cuentas, PagoEventPublisher publisher,
                       @Value("${payment.alerta.monto-minimo:1000000}") BigDecimal umbralAlerta) {
        this.repository = repository;
        this.cuentas = cuentas;
        this.publisher = publisher;
        this.umbralAlerta = umbralAlerta;
    }

    public Pago procesar(String idempotencyKey, CrearPagoRequest request) {
        return repository.findByIdempotencyKey(idempotencyKey).orElseGet(() -> {
            String tipo = request.tipo().toUpperCase();
            if (!List.of("PAGO", "TRANSFERENCIA", "DEPOSITO").contains(tipo)) {
                throw new IllegalArgumentException("Tipo de pago invalido");
            }
            if ("TRANSFERENCIA".equals(tipo) && (request.cuentaDestino() == null || request.cuentaDestino().isBlank())) {
                throw new IllegalArgumentException("La transferencia requiere una cuenta destino");
            }
            return ejecutar(idempotencyKey, tipo, request.cuentaOrigen(), request.cuentaDestino(),
                    request.monto(), request.moneda().toUpperCase(), request.descripcion()).pago();
        });
    }

    public Pago transferir(String idempotencyKey, TransferenciaRequest request) {
        return repository.findByIdempotencyKey(idempotencyKey).orElseGet(() -> {
            var origen = cuentas.obtener(request.cuentaOrigen());
            return ejecutar(idempotencyKey, "TRANSFERENCIA", request.cuentaOrigen(), request.cuentaDestino(),
                    request.monto(), origen.moneda(), request.descripcion()).pago();
        });
    }

    public RetiroResponse retirar(String idempotencyKey, RetiroRequest request) {
        var existente = repository.findByIdempotencyKey(idempotencyKey);
        if (existente.isPresent()) {
            var pago = existente.get();
            var cuenta = cuentas.obtener(request.numeroCuenta());
            return new RetiroResponse(pago.getId(), pago.getEstado(), pago.getMonto(), cuenta.saldo(), cuenta.moneda());
        }
        var cuenta = cuentas.obtener(request.numeroCuenta());
        var resultado = ejecutar(idempotencyKey, "RETIRO", request.numeroCuenta(), null,
                request.monto(), cuenta.moneda(), "Retiro en cajero");
        return new RetiroResponse(resultado.pago().getId(), resultado.pago().getEstado(),
                resultado.pago().getMonto(), resultado.saldoFinal(), resultado.pago().getMoneda());
    }

    @Transactional(readOnly = true)
    public List<Pago> listar(String rutCliente, int limite) {
        int limiteSeguro = Math.max(1, Math.min(limite, 100));
        return repository.findByRutClienteOrderByFechaDesc(rutCliente, PageRequest.of(0, limiteSeguro));
    }

    private ResultadoOperacion ejecutar(String idempotencyKey, String tipo, String origenNumero, String destinoNumero,
                                        BigDecimal monto, String moneda, String descripcion) {
        if (monto == null || monto.signum() <= 0) throw new IllegalArgumentException("Monto invalido");
        if (destinoNumero != null && origenNumero.equals(destinoNumero)) {
            throw new IllegalArgumentException("Las cuentas de origen y destino deben ser distintas");
        }
        CuentaBackendResponse origen = cuentas.obtener(origenNumero);
        if (!origen.moneda().equalsIgnoreCase(moneda)) throw new IllegalArgumentException("Moneda incompatible");
        var pago = repository.save(new Pago(idempotencyKey, tipo, origen.rutCliente(), origenNumero,
                destinoNumero, monto, moneda, descripcion));
        CuentaBackendResponse resultadoCuenta = origen;
        boolean debitoRealizado = false;
        try {
            if ("DEPOSITO".equals(tipo)) {
                resultadoCuenta = cuentas.acreditar(origenNumero, monto);
            } else {
                resultadoCuenta = cuentas.debitar(origenNumero, monto);
                debitoRealizado = true;
                if (destinoNumero != null) {
                    var destino = cuentas.obtener(destinoNumero);
                    if (!destino.moneda().equalsIgnoreCase(moneda)) throw new IllegalArgumentException("Moneda destino incompatible");
                    cuentas.acreditar(destinoNumero, monto);
                }
            }
            pago.aprobar();
            publisher.transaccionCompletada(pago);
            if (monto.compareTo(umbralAlerta) >= 0) publisher.alerta(pago, "MONTO_ELEVADO");
        } catch (RuntimeException exception) {
            if (debitoRealizado && destinoNumero != null) {
                try {
                    resultadoCuenta = cuentas.acreditar(origenNumero, monto);
                } catch (RuntimeException compensacionFallida) {
                    publisher.alerta(pago, "COMPENSACION_FALLIDA");
                }
            }
            pago.rechazar(exception.getMessage());
            publisher.alerta(pago, "OPERACION_RECHAZADA");
        }
        return new ResultadoOperacion(pago, resultadoCuenta.saldo());
    }

    private record ResultadoOperacion(Pago pago, BigDecimal saldoFinal) { }
}
