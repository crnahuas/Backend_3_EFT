package cl.duoc.bancoxyz.payment.repository;

import cl.duoc.bancoxyz.payment.domain.Pago;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagoRepository extends JpaRepository<Pago, UUID> {
    Optional<Pago> findByIdempotencyKey(String idempotencyKey);
    List<Pago> findByRutClienteOrderByFechaDesc(String rutCliente, Pageable pageable);
}
