package com.bank.cuentas.service;

import java.math.BigDecimal;
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
import com.bank.cuentas.model.Movimiento;
import com.bank.cuentas.model.TipoMovimiento;
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
		// Lock pesimista: los movimientos concurrentes de una misma cuenta se
		// serializan y ninguno pierde su actualizacion de saldo.
		Cuenta cuenta = accountRepository.findByAccountNumberForUpdate(request.cuenta())
				.orElseThrow(() -> new BusinessRuleException("CUENTA_NO_ENCONTRADA",
						"No existe la cuenta " + request.cuenta(), HttpStatus.NOT_FOUND));

		BigDecimal valor = request.valor().setScale(2, RoundingMode.HALF_UP);
		validarTipoYSigno(request.tipoMovimiento(), valor);
		if (!cuenta.estaActiva()) {
			throw new BusinessRuleException("CUENTA_NO_ACTIVA",
					"No se pueden registrar movimientos en la cuenta " + request.cuenta()
							+ " porque no esta activa",
					HttpStatus.CONFLICT);
		}

		// El saldo se actualiza en la misma transaccion que registra el
		// movimiento: o se guardan los dos, o no se guarda ninguno.
		BigDecimal saldoResultante = cuenta.aplicarMovimiento(valor);
		Movimiento movimiento = Movimiento.registrar(cuenta, request.tipoMovimiento(), valor,
				saldoResultante, request.fecha() != null ? request.fecha() : LocalDateTime.now(),
				request.referencia());

		return MovementResponse.from(movementRepository.save(movimiento));
	}

	/**
	 * El tipo y el signo del valor son dos caras del mismo dato: un ingreso
	 * positivo o un egreso negativo. El cero no representa ninguna operacion.
	 */
	private void validarTipoYSigno(TipoMovimiento tipo, BigDecimal valor) {
		if (valor.signum() == 0) {
			throw new BusinessRuleException("VALOR_INVALIDO",
					"El valor del movimiento no puede ser cero", HttpStatus.BAD_REQUEST);
		}
		boolean concuerda = (tipo == TipoMovimiento.INGRESO && valor.signum() > 0)
				|| (tipo == TipoMovimiento.EGRESO && valor.signum() < 0);
		if (!concuerda) {
			throw new BusinessRuleException("TIPO_MOVIMIENTO_INVALIDO",
					"El tipo " + tipo + " no concuerda con el signo del valor " + valor,
					HttpStatus.BAD_REQUEST);
		}
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
		movimiento.actualizarMetadatos(request.fecha(), request.referencia());
		return MovementResponse.from(movimiento);
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
