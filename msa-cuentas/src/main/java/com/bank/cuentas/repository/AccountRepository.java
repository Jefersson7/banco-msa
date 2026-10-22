package com.bank.cuentas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.cuentas.model.Cuenta;
import com.bank.cuentas.model.EstadoCuenta;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Cuenta, Long> {

	/**
	 * Busca la cuenta por su numero. Filtra por el campo {@code numeroCuenta}.
	 */
	@Query("select c from Cuenta c where c.numeroCuenta = :accountNumber")
	Optional<Cuenta> findByAccountNumber(@Param("accountNumber") String accountNumber);

	/**
	 * Lista las cuentas de un cliente. Filtra por el campo {@code clienteId}.
	 */
	@Query("select c from Cuenta c where c.clienteId = :clientId")
	Page<Cuenta> findByClientId(@Param("clientId") String clientId, Pageable pageable);

	/**
	 * Cuentas de un cliente en un estado concreto. Se usa para desactivar las
	 * cuentas activas cuando el cliente se da de baja. Filtra por los campos
	 * {@code clienteId} y {@code estado}.
	 */
	@Query("select c from Cuenta c where c.clienteId = :clientId and c.estado = :status")
	List<Cuenta> findByClientIdAndStatus(@Param("clientId") String clientId, @Param("status") EstadoCuenta status);

	/**
	 * Lectura con lock pesimista de escritura: serializa los movimientos sobre
	 * una misma cuenta cuando se actualice el saldo. Filtra por el campo
	 * {@code numeroCuenta}.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Cuenta c where c.numeroCuenta = :accountNumber")
	Optional<Cuenta> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

}
