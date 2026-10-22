package com.bank.cuentas.dto;

import com.bank.cuentas.model.EstadoCuenta;
import com.bank.cuentas.model.TipoCuenta;

/**
 * Actualizacion parcial (PATCH): solo se aplican los campos que llegan. El
 * saldo no se puede tocar por este endpoint: se mueve con los movimientos.
 */
public record PatchAccountRequest(

		TipoCuenta tipoCuenta,

		EstadoCuenta estado) {

	public boolean sinCambios() {
		return tipoCuenta == null && estado == null;
	}

}
