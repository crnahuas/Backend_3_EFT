package cl.duoc.bancoxyz.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @Column(length = 12, nullable = false)
    private String rut;
    @Column(nullable = false, length = 80)
    private String nombres;
    @Column(nullable = false, length = 80)
    private String apellidos;
    @Column(nullable = false, unique = true, length = 160)
    private String email;
    @Column(length = 24)
    private String telefono;
    @Column(nullable = false, length = 20)
    private String estado;
    @Column(nullable = false, updatable = false)
    private Instant creadoEn;
    @Column(nullable = false)
    private Instant actualizadoEn;
    private Instant ultimaActividad;
    @Version
    private long version;

    protected Cliente() { }

    public Cliente(String rut, String nombres, String apellidos, String email, String telefono) {
        this.rut = rut;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.estado = "ACTIVO";
        this.creadoEn = Instant.now();
        this.actualizadoEn = this.creadoEn;
    }

    public void actualizar(String nombres, String apellidos, String email, String telefono) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.actualizadoEn = Instant.now();
    }

    public void cambiarEstado(String estado) {
        this.estado = estado;
        this.actualizadoEn = Instant.now();
    }

    public void registrarActividad(Instant fecha) {
        this.ultimaActividad = fecha;
        this.actualizadoEn = Instant.now();
    }

    public String getRut() { return rut; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getEstado() { return estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public Instant getUltimaActividad() { return ultimaActividad; }
}
