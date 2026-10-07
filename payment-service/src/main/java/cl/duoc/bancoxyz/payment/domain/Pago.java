package cl.duoc.bancoxyz.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pagos")
public class Pago {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;
    @Column(nullable = false, length = 20)
    private String tipo;
    @Column(nullable = false, length = 12)
    private String rutCliente;
    @Column(length = 24)
    private String cuentaOrigen;
    @Column(length = 24)
    private String cuentaDestino;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;
    @Column(nullable = false, length = 3)
    private String moneda;
    @Column(nullable = false, length = 16)
    private String estado;
    @Column(length = 120)
    private String descripcion;
    @Column(length = 300)
    private String error;
    @Column(nullable = false, updatable = false)
    private Instant fecha;
    private Instant completadoEn;

    protected Pago() { }

    public Pago(String idempotencyKey, String tipo, String rutCliente, String cuentaOrigen,
                String cuentaDestino, BigDecimal monto, String moneda, String descripcion) {
        this.id = UUID.randomUUID();
        this.idempotencyKey = idempotencyKey;
        this.tipo = tipo;
        this.rutCliente = rutCliente;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
        this.moneda = moneda;
        this.descripcion = descripcion;
        this.estado = "PENDIENTE";
        this.fecha = Instant.now();
    }

    public void aprobar() { estado = "APROBADO"; completadoEn = Instant.now(); }
    public void rechazar(String causa) { estado = "RECHAZADO"; error = causa; completadoEn = Instant.now(); }

    public UUID getId() { return id; }
    public String getTipo() { return tipo; }
    public String getRutCliente() { return rutCliente; }
    public String getCuentaOrigen() { return cuentaOrigen; }
    public String getCuentaDestino() { return cuentaDestino; }
    public BigDecimal getMonto() { return monto; }
    public String getMoneda() { return moneda; }
    public String getEstado() { return estado; }
    public String getDescripcion() { return descripcion; }
    public String getError() { return error; }
    public Instant getFecha() { return fecha; }
}
