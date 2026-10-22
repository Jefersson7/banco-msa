package com.bank.cuentas.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.cuentas.dto.UpdateAccountRequest;
import com.bank.cuentas.dto.AccountResponse;
import com.bank.cuentas.dto.AccountsUpdatedResponse;
import com.bank.cuentas.dto.CreateAccountRequest;
import com.bank.cuentas.dto.PatchAccountRequest;
import com.bank.cuentas.exception.BusinessRuleException;
import com.bank.cuentas.integration.ClientGateway;
import com.bank.cuentas.model.Cuenta;
import com.bank.cuentas.model.EstadoCuenta;
import com.bank.cuentas.repository.AccountRepository;

@Service
public class AccountService {

	private final AccountRepository accountRepository;

	private final ClientGateway clientGateway;

	public AccountService(AccountRepository accountRepository, ClientGateway clientGateway) {
		this.accountRepository = accountRepository;
		this.clientGateway = clientGateway;
	}

	@Transactional
	public AccountResponse create(CreateAccountRequest request) {
		// El estado del cliente es de msa-clientes: se verifica alli que exista y
		// siga activo antes de abrirle una cuenta.
		clientGateway.verificarClienteActivo(request.clienteId());

		Cuenta cuenta = Cuenta.abrir(request.tipoCuenta(), request.saldoInicial(), request.clienteId(),
				request.nombreCliente(), request.identificacion());
		return AccountResponse.from(accountRepository.save(cuenta));
	}

	@Transactional(readOnly = true)
	public AccountResponse findByAccountNumber(String accountNumber) {
		return AccountResponse.from(findExistingAccount(accountNumber));
	}

	@Transactional(readOnly = true)
	public Page<AccountResponse> findAll(String clientId, Pageable pageable) {
		Page<Cuenta> cuentas = (clientId == null || clientId.isBlank())
				? accountRepository.findAll(pageable)
				: accountRepository.findByClientId(clientId, pageable);
		return cuentas.map(AccountResponse::from);
	}

	/**
	 * Actualizacion de cuenta: solo tipo y estado. El saldo no se toca.
	 */
	@Transactional
	public AccountResponse update(String accountNumber, UpdateAccountRequest request) {
		Cuenta cuenta = findExistingAccount(accountNumber);
		cuenta.actualizar(request.tipoCuenta(), request.estado());
		return AccountResponse.from(cuenta);
	}

	/**
	 * Actualizacion parcial de una cuenta (PATCH): solo se aplican los campos
	 * que llegan. El saldo no se toca.
	 */
	@Transactional
	public AccountResponse updatePartially(String accountNumber, PatchAccountRequest request) {
		if (request.sinCambios()) {
			throw new BusinessRuleException("SIN_CAMBIOS",
					"No se indico ningun campo a actualizar", HttpStatus.BAD_REQUEST);
		}
		Cuenta cuenta = findExistingAccount(accountNumber);
		cuenta.actualizarParcialmente(request.tipoCuenta(), request.estado());
		return AccountResponse.from(cuenta);
	}

	/**
	 * Actualizacion parcial de las cuentas de un cliente (PATCH sobre la
	 * coleccion filtrada por clienteId). Solo se tocan las cuentas activas: las
	 * cerradas son un estado terminal y no se reabren. Es lo que msa-clientes
	 * pide de forma asincrona cuando el cliente se da de baja: sus cuentas
	 * activas pasan a INACTIVA.
	 */
	@Transactional
	public AccountsUpdatedResponse updateByClientId(String clientId, PatchAccountRequest request) {
		if (request.estado() == null) {
			throw new BusinessRuleException("SIN_CAMBIOS",
					"En la actualizacion masiva solo se puede cambiar el estado de las cuentas",
					HttpStatus.BAD_REQUEST);
		}
		List<Cuenta> cuentas = accountRepository.findByClientIdAndStatus(clientId, EstadoCuenta.ACTIVA);
		for (Cuenta cuenta : cuentas) {
			cuenta.cambiarEstado(request.estado());
		}
		return new AccountsUpdatedResponse(clientId, cuentas.size(),
				cuentas.stream().map(Cuenta::getNumeroCuenta).toList());
	}

	private Cuenta findExistingAccount(String accountNumber) {
		return accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new BusinessRuleException("CUENTA_NO_ENCONTRADA",
						"No existe la cuenta " + accountNumber, HttpStatus.NOT_FOUND));
	}

}