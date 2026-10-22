package com.bank.cuentas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.cuentas.model.Movimiento; import com.bank.cuentas.model.TipoMovimiento;

public record MovementResponse(

		Long id,
		LocalDateTime fecha,
		TipoMovimiento tipoMovimiento,
		BigDecimal valor,
		BigDecimal saldo,
		String referencia,
		String numeroCuenta,
		String clienteId) {

	public static MovementResponse from(Movimiento movimiento) {
		return new MovementResponse(
				movimiento.getId(),
				movimiento.getFecha(),
				movimiento.getTipoMovimiento(),
				movimiento.getValor(),
				movimiento.getSaldo(),
				movimiento.getReferencia(),
				movimiento.getCuenta().getNumeroCuenta(),
				movimiento.getCuenta().getClienteId());
	}

}
