package com.bank.cuentas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.cuentas.model.TipoMovimiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Alta de movimiento. El "valor" conserva su signo: positivo para un ingreso y
 * negativo para un egreso. El "tipoMovimiento" se recibe y debe concordar con
 * ese signo; el valor no puede ser cero.
 */
public record CreateMovementRequest(

		@NotBlank(message = "la cuenta es obligatoria")
		@Size(max = 20, message = "la cuenta no puede superar los 20 caracteres")
		String cuenta,

		@NotNull(message = "el tipo de movimiento es obligatorio")
		TipoMovimiento tipoMovimiento,

		@NotNull(message = "el valor es obligatorio")
		BigDecimal valor,

		LocalDateTime fecha,

		@Size(max = 60, message = "la referencia no puede superar los 60 caracteres")
		String referencia) {
}
