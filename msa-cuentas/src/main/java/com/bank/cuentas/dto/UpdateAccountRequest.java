package com.bank.cuentas.dto;

import com.bank.cuentas.model.EstadoCuenta;
import com.bank.cuentas.model.TipoCuenta;

import jakarta.validation.constraints.NotNull;

/**
 * Actualizacion de cuenta (PUT): solo el tipo y el estado. Los saldos no se
 * modifican por endpoint, se mueven con los movimientos.
 */
public record UpdateAccountRequest(

		@NotNull(message = "el tipo de cuenta es obligatorio")
		TipoCuenta tipoCuenta,

		@NotNull(message = "el estado de la cuenta es obligatorio")
		EstadoCuenta estado) {
}
