package com.bank.cuentas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Alta de movimiento. El signo del "valor" define el tipo: en positivo es un
 * ingreso y en negativo un egreso. No hay campo de tipo de movimiento.
 */
public record CreateMovementRequest(

		@NotBlank(message = "la cuenta es obligatoria")
		@Size(max = 20, message = "la cuenta no puede superar los 20 caracteres")
		String cuenta,

		@NotNull(message = "el valor es obligatorio")
		BigDecimal valor,

		LocalDateTime fecha,

		@Size(max = 60, message = "la referencia no puede superar los 60 caracteres")
		String referencia) {
}
