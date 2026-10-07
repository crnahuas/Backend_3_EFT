package cl.duoc.bancoxyz.account.service;

import cl.duoc.bancoxyz.account.api.AbrirCuentaRequest;
import cl.duoc.bancoxyz.account.domain.Cuenta;
import cl.duoc.bancoxyz.account.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CuentaService {
    private final CuentaRepository repository;

    public CuentaService(CuentaRepository repository) { this.repository = repository; }

    public Cuenta abrir(AbrirCuentaRequest request) {
        String tipo = request.tipo().toUpperCase();
        if (!List.of("VISTA", "CORRIENTE", "AHORRO").contains(tipo)) {
            throw new IllegalArgumentException("Tipo de cuenta invalido");
        }
        String numero = "XYZ" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return repository.save(new Cuenta(numero, request.rutCliente(), tipo,
                request.saldoInicial(), request.moneda().toUpperCase()));
    }

    @Transactional(readOnly = true)
    public Cuenta obtener(String numero) {
        return repository.findById(numero).orElseThrow(() -> new CuentaNoEncontradaException(numero));
    }

    @Transactional(readOnly = true)
    public List<Cuenta> listarPorCliente(String rutCliente) {
        return repository.findByRutClienteOrderByCreadaEnDesc(rutCliente);
    }

    public Cuenta debitar(String numero, BigDecimal monto) {
        validarMonto(monto);
        var cuenta = repository.bloquearPorNumero(numero)
                .orElseThrow(() -> new CuentaNoEncontradaException(numero));
        cuenta.debitar(monto);
        return cuenta;
    }

    public Cuenta acreditar(String numero, BigDecimal monto) {
        validarMonto(monto);
        var cuenta = repository.bloquearPorNumero(numero)
                .orElseThrow(() -> new CuentaNoEncontradaException(numero));
        cuenta.acreditar(monto);
        return cuenta;
    }

    public Cuenta cerrar(String numero) {
        var cuenta = repository.bloquearPorNumero(numero)
                .orElseThrow(() -> new CuentaNoEncontradaException(numero));
        cuenta.cerrar();
        return cuenta;
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) throw new IllegalArgumentException("El monto debe ser positivo");
    }
}
