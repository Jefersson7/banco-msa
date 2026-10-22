package com.bank.clientes.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.clientes.model.Cliente;

public interface ClientRepository extends JpaRepository<Cliente, Long> {

	Optional<Cliente> findByClientId(String clientId);

	boolean existsByClientId(String clientId);

	/**
	 * Cuenta los clientes con esa identificacion. Se usa para detectar
	 * identificaciones duplicadas. Filtra por el campo {@code identificacion}.
	 */
	@Query("select count(c) from Cliente c where c.identificacion = :identification")
	long countByIdentification(@Param("identification") String identification);

}
