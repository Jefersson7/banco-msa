package com.bank.cuentas.dto;

import java.math.BigDecimal;

import com.bank.cuentas.model.TipoCuenta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

		@NotNull(message = "el tipo de cuenta es obligatorio")
		TipoCuenta tipoCuenta,

		@NotNull(message = "el saldo inicial es obligatorio")
		@DecimalMin(value = "0.00", message = "el saldo inicial no puede ser negativo")
		BigDecimal saldoInicial,

		@NotBlank(message = "el cliente es obligatorio")
		@Size(max = 20, message = "el cliente no puede superar los 20 caracteres")
		String clienteId,

		@NotBlank(message = "el nombre del cliente es obligatorio")
		@Size(max = 120, message = "el nombre del cliente no puede superar los 120 caracteres")
		String nombreCliente,

		@Size(max = 30, message = "la identificacion no puede superar los 30 caracteres")
		String identificacion) {
}