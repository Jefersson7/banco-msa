package com.bank.clientes.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.clientes.dto.PatchClientRequest;
import com.bank.clientes.dto.UpdateClientRequest;
import com.bank.clientes.dto.ClientResponse;
import com.bank.clientes.dto.CreateClientRequest;
import com.bank.clientes.event.ClientDeactivatedEvent;
import com.bank.clientes.event.ClientRegisteredEvent;
import com.bank.clientes.exception.BusinessRuleException;
import com.bank.clientes.model.Cliente;
import com.bank.clientes.model.DatosPersona;
import com.bank.clientes.repository.ClientRepository;
import com.bank.clientes.security.PasswordHasher;

@Service
public class ClientService {

	private static final String CLIENT_ID_PREFIX = "CLI-";

	private static final int MAX_CLIENT_ID_ATTEMPTS = 5;

	private final ClientRepository clientRepository;

	private final PasswordHasher passwordHasher;

	private final ApplicationEventPublisher eventPublisher;

	public ClientService(ClientRepository clientRepository, PasswordHasher passwordHasher,
			ApplicationEventPublisher eventPublisher) {
		this.clientRepository = clientRepository;
		this.passwordHasher = passwordHasher;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public ClientResponse create(CreateClientRequest request) {
		if (clientRepository.countByIdentification(request.identificacion()) > 0) {
			throw new BusinessRuleException("IDENTIFICACION_DUPLICADA",
					"Ya existe un cliente con la identificacion " + request.identificacion(), HttpStatus.CONFLICT);
		}

		Cliente cliente = new Cliente(datosDe(request), generateClientId(),
				passwordHasher.hashPassword(request.password()), LocalDateTime.now());

		Cliente clienteGuardado = clientRepository.save(cliente);

		// El evento se publica dentro de la transaccion; el listener lo consume
		// despues del commit.
		eventPublisher.publishEvent(new ClientRegisteredEvent(clienteGuardado.getClientId(),
				clienteGuardado.getNombre(), clienteGuardado.getIdentificacion()));

		return ClientResponse.from(clienteGuardado);
	}

	@Transactional(readOnly = true)
	public ClientResponse findByClientId(String clientId) {
		return ClientResponse.from(findExistingClient(clientId));
	}

	@Transactional(readOnly = true)
	public Page<ClientResponse> findAll(Pageable pageable) {
		return clientRepository.findAll(pageable).map(ClientResponse::from);
	}

	@Transactional
	public ClientResponse update(String clientId, UpdateClientRequest request) {
		Cliente cliente = findExistingClient(clientId);
		// La unicidad se comprueba antes de tocar la entidad: si se comprobara
		// despues, el cliente ya tendria la identificacion nueva y el chequeo
		// no detectaria el duplicado.
		ensureIdentificationIsUnique(cliente, request.identificacion());
		cliente.actualizar(datosDe(request));
		return ClientResponse.from(clientRepository.save(cliente));
	}

	@Transactional
	public ClientResponse updatePartially(String clientId, PatchClientRequest request) {
		Cliente cliente = findExistingClient(clientId);
		if (request.identificacion() != null) {
			ensureIdentificationIsUnique(cliente, request.identificacion());
		}
		cliente.actualizarParcialmente(datosDe(request));
		return ClientResponse.from(clientRepository.save(cliente));
	}

	/**
	 * Baja logica: el cliente no se borra, pasa a INACTIVO. Sus cuentas activas
	 * se desactivan de forma asincrona en msa-cuentas.
	 */
	@Transactional
	public void deactivate(String clientId) {
		Cliente cliente = findExistingClient(clientId);
		cliente.desactivar();
		clientRepository.save(cliente);

		// El evento se publica dentro de la transaccion; el listener lo consume
		// despues del commit.
		eventPublisher.publishEvent(new ClientDeactivatedEvent(cliente.getClientId()));
	}

	private Cliente findExistingClient(String clientId) {
		return clientRepository.findByClientId(clientId)
				.orElseThrow(() -> new BusinessRuleException("CLIENTE_NO_ENCONTRADO",
						"No existe el cliente " + clientId, HttpStatus.NOT_FOUND));
	}

	private void ensureIdentificationIsUnique(Cliente cliente, String identification) {
		if (!cliente.getIdentificacion().equals(identification)
				&& clientRepository.countByIdentification(identification) > 0) {
			throw new BusinessRuleException("IDENTIFICACION_DUPLICADA",
					"Ya existe un cliente con la identificacion " + identification, HttpStatus.CONFLICT);
		}
	}

	/**
	 * El clientId lo genera el servidor: "CLI-" seguido de 8 hexadecimales.
	 */
	private String generateClientId() {
		for (int attempt = 0; attempt < MAX_CLIENT_ID_ATTEMPTS; attempt++) {
			String clientId = CLIENT_ID_PREFIX + UUID.randomUUID().toString().substring(0, 8);
			if (!clientRepository.existsByClientId(clientId)) {
				return clientId;
			}
		}
		throw new BusinessRuleException("CLIENT_ID_NO_GENERADO",
				"No se pudo generar un identificador unico para el cliente", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Traduce el DTO de la API al objeto del dominio. El mapeo vive en el
	 * servicio: la entidad no conoce la capa web.
	 */
	private DatosPersona datosDe(CreateClientRequest request) {
		return new DatosPersona(request.nombre(), request.genero(), request.edad(), request.identificacion(),
				request.direccion(), request.telefono());
	}

	private DatosPersona datosDe(UpdateClientRequest request) {
		return new DatosPersona(request.nombre(), request.genero(), request.edad(), request.identificacion(),
				request.direccion(), request.telefono());
	}

	private DatosPersona datosDe(PatchClientRequest request) {
		return new DatosPersona(request.nombre(), request.genero(), request.edad(), request.identificacion(),
				request.direccion(), request.telefono());
	}

}
