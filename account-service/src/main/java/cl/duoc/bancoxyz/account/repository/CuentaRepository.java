package cl.duoc.bancoxyz.account.repository;

import cl.duoc.bancoxyz.account.domain.Cuenta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, String> {
    List<Cuenta> findByRutClienteOrderByCreadaEnDesc(String rutCliente);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.numeroCuenta = :numero")
    Optional<Cuenta> bloquearPorNumero(@Param("numero") String numeroCuenta);
}
