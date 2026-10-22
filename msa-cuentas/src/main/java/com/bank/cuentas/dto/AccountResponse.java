package com.bank.cuentas.dto;

import java.math.BigDecimal;

import com.bank.cuentas.model.Cuenta;
import com.bank.cuentas.model.EstadoCuenta;
import com.bank.cuentas.model.TipoCuenta;

public record AccountResponse(

		String numeroCuenta,
		TipoCuenta tipoCuenta,
		BigDecimal saldoInicial,
		BigDecimal saldoActual,
		EstadoCuenta estado,
		String clienteId,
		String nombreCliente,
		String identificacion) {

	public static AccountResponse from(Cuenta cuenta) {
		return new AccountResponse(
				cuenta.getNumeroCuenta(),
				cuenta.getTipoCuenta(),
				cuenta.getSaldoInicial(),
				cuenta.getSaldoActual(),
				cuenta.getEstado(),
				cuenta.getClienteId(),
				cuenta.getNombreCliente(),
				cuenta.getIdentificacion());
	}

}
