package cl.duoc.bancoxyz.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cuentas")
public class Cuenta {
    @Id
    @Column(length = 24)
    private String numeroCuenta;
    @Column(nullable = false, length = 12)
    private String rutCliente;
    @Column(nullable = false, length = 24)
    private String tipo;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;
    @Column(nullable = false, length = 3)
    private String moneda;
    @Column(nullable = false, length = 16)
    private String estado;
    @Column(nullable = false, updatable = false)
    private Instant creadaEn;
    @Column(nullable = false)
    private Instant actualizadaEn;
    @Version
    private long version;

    protected Cuenta() { }

    public Cuenta(String numeroCuenta, String rutCliente, String tipo, BigDecimal saldoInicial, String moneda) {
        this.numeroCuenta = numeroCuenta;
        this.rutCliente = rutCliente;
        this.tipo = tipo;
        this.saldo = saldoInicial;
        this.moneda = moneda;
        this.estado = "ACTIVA";
        this.creadaEn = Instant.now();
        this.actualizadaEn = this.creadaEn;
    }

    public void debitar(BigDecimal monto) {
        validarActiva();
        if (saldo.compareTo(monto) < 0) throw new IllegalStateException("Saldo insuficiente");
        saldo = saldo.subtract(monto);
        actualizadaEn = Instant.now();
    }

    public void acreditar(BigDecimal monto) {
        validarActiva();
        saldo = saldo.add(monto);
        actualizadaEn = Instant.now();
    }

    public void cerrar() {
        if (saldo.compareTo(BigDecimal.ZERO) != 0) throw new IllegalStateException("La cuenta debe tener saldo cero para cerrarse");
        estado = "CERRADA";
        actualizadaEn = Instant.now();
    }

    private void validarActiva() {
        if (!"ACTIVA".equals(estado)) throw new IllegalStateException("La cuenta no esta activa");
    }

    public String getNumeroCuenta() { return numeroCuenta; }
    public String getRutCliente() { return rutCliente; }
    public String getTipo() { return tipo; }
    public BigDecimal getSaldo() { return saldo; }
    public String getMoneda() { return moneda; }
    public String getEstado() { return estado; }
    public Instant getCreadaEn() { return creadaEn; }
    public Instant getActualizadaEn() { return actualizadaEn; }
}
