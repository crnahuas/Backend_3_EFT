package cl.duoc.bancoxyz.customer.repository;

import cl.duoc.bancoxyz.customer.domain.AlertaSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertaSeguridadRepository extends JpaRepository<AlertaSeguridad, UUID> {
}
