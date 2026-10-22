package com.bank.cuentas.repository;

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

}
