package cl.duoc.bancoxyz.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alertas_seguridad")
public class AlertaSeguridad {
    @Id
    private UUID eventoId;
    @Column(nullable = false)
    private UUID pagoId;
    @Column(nullable = false, length = 12)
    private String rutCliente;
    @Column(nullable = false, length = 300)
    private String motivo;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;
    @Column(nullable = false)
    private Instant fecha;

    protected AlertaSeguridad() { }

    public AlertaSeguridad(UUID eventoId, UUID pagoId, String rutCliente, String motivo,
                           BigDecimal monto, Instant fecha) {
        this.eventoId = eventoId;
        this.pagoId = pagoId;
        this.rutCliente = rutCliente;
        this.motivo = motivo;
        this.monto = monto;
        this.fecha = fecha;
    }

    public UUID getEventoId() { return eventoId; }
    public UUID getPagoId() { return pagoId; }
    public String getRutCliente() { return rutCliente; }
    public String getMotivo() { return motivo; }
    public BigDecimal getMonto() { return monto; }
    public Instant getFecha() { return fecha; }
}
