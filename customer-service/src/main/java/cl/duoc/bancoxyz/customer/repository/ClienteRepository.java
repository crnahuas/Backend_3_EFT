package cl.duoc.bancoxyz.customer.repository;

import cl.duoc.bancoxyz.customer.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    boolean existsByEmailIgnoreCase(String email);
}
