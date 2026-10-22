package com.bank.cuentas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;

/**
 * Actualizacion de movimiento (PUT): solo la fecha y la referencia.
 *
 * "valor" se recibe para poder responder con un error claro cuando se intenta
 * cambiar un dato contable: un movimiento registrado es inmutable, se corrige
 * con un movimiento en contra.
 */
public record UpdateMovementRequest(

		LocalDateTime fecha,

		@Size(max = 60, message = "la referencia no puede superar los 60 caracteres")
		String referencia,

		BigDecimal valor) {
}
