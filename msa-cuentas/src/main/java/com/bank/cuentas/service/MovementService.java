package com.bank.cuentas.service;

import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.cuentas.dto.UpdateMovementRequest;
import com.bank.cuentas.dto.CreateMovementRequest;
import com.bank.cuentas.dto.MovementResponse;
import com.bank.cuentas.exception.BusinessRuleException;
import com.bank.cuentas.model.Cuenta;
import com.bank.cuentas.model.Movimiento; import com.bank.cuentas.model.TipoMovimiento;
import com.bank.cuentas.repository.AccountRepository;
import com.bank.cuentas.repository.MovementRepository;

@Service
public class MovementService {

	private final MovementRepository movementRepository;

	private final AccountRepository accountRepository;

	public MovementService(MovementRepository movementRepository, AccountRepository accountRepository) {
		this.movementRepository = movementRepository;
		this.accountRepository = accountRepository;
	}

	@Transactional
	public MovementResponse create(CreateMovementRequest request) {
		Cuenta cuenta = accountRepository.findByAccountNumber(request.cuenta())
				.orElseThrow(() -> new BusinessRuleException("CUENTA_NO_ENCONTRADA",
						"No existe la cuenta " + request.cuenta(), HttpStatus.NOT_FOUND));

		Movimiento movimiento = new Movimiento();
		movimiento.setCuenta(cuenta);
		movimiento.setValor(request.valor().setScale(2, RoundingMode.HALF_UP));
		// El tipo no se recibe: lo define el signo del valor.
		movimiento.setTipoMovimiento(request.valor().signum() < 0 ? TipoMovimiento.EGRESO : TipoMovimiento.INGRESO);
		// En F1 el movimiento guarda el saldo que tiene la cuenta; el saldo
		// disponible se actualiza con la regla de negocio de la iteracion F2.
		movimiento.setSaldo(cuenta.getSaldoActual());
		movimiento.setFecha(request.fecha() != null ? request.fecha() : LocalDateTime.now());
		movimiento.setReferencia(request.referencia());

		return MovementResponse.from(movementRepository.save(movimiento));
	}

	@Transactional(readOnly = true)
	public MovementResponse findById(Long id) {
		return MovementResponse.from(findExistingMovement(id));
	}

	@Transactional(readOnly = true)
	public Page<MovementResponse> findAll(String accountNumber, Pageable pageable) {
		Page<Movimiento> movimientos = (accountNumber == null || accountNumber.isBlank())
				? movementRepository.findAll(pageable)
				: movementRepository.findByAccountNumber(accountNumber, pageable);
		return movimientos.map(MovementResponse::from);
	}

	/**
	 * Actualizacion de movimiento: solo la fecha y la referencia. El valor no se
	 * puede cambiar: un movimiento registrado es inmutable y se corrige con un
	 * movimiento en contra.
	 */
	@Transactional
	public MovementResponse update(Long id, UpdateMovementRequest request) {
		Movimiento movimiento = findExistingMovement(id);
		ensureAccountingDataUnchanged(movimiento, request);
		if (request.fecha() != null) {
			movimiento.setFecha(request.fecha());
		}
		if (request.referencia() != null) {
			movimiento.setReferencia(request.referencia());
		}
		return MovementResponse.from(movementRepository.save(movimiento));
	}

	private void ensureAccountingDataUnchanged(Movimiento movimiento, UpdateMovementRequest request) {
		if (request.valor() != null && request.valor().compareTo(movimiento.getValor()) != 0) {
			throw new BusinessRuleException("MOVIMIENTO_NO_MODIFICABLE",
					"El valor de un movimiento registrado no se puede modificar; se corrige con un movimiento en contra",
					HttpStatus.CONFLICT);
		}
	}

	private Movimiento findExistingMovement(Long id) {
		return movementRepository.findById(id)
				.orElseThrow(() -> new BusinessRuleException("MOVIMIENTO_NO_ENCONTRADO",
						"No existe el movimiento " + id, HttpStatus.NOT_FOUND));
	}

}
