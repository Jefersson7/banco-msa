package com.bank.cuentas.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.cuentas.model.Movimiento;

public interface MovementRepository extends JpaRepository<Movimiento, Long> {

	/**
	 * Lista los movimientos de una cuenta. Filtra por {@code cuenta.numeroCuenta}.
	 */
	@Query("select m from Movimiento m where m.cuenta.numeroCuenta = :accountNumber")
	Page<Movimiento> findByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

	/**
	 * Movimientos de todas las cuentas de un cliente dentro de un rango de
	 * fechas. Se trae la cuenta con {@code join fetch} para poder leer sus datos
	 * al construir el reporte sin salir de la transaccion.
	 */
	@Query("select m from Movimiento m join fetch m.cuenta c "
			+ "where c.clienteId = :clientId and m.fecha between :desde and :hasta "
			+ "order by m.fecha asc, m.id asc")
	List<Movimiento> findByClientAndFechaBetween(@Param("clientId") String clientId,
			@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

}
