package com.bank.cuentas.dto;

import java.util.List;

/**
 * Resultado de actualizar en bloque las cuentas de un cliente.
 */
public record AccountsUpdatedResponse(

		String clienteId,
		int total,
		List<String> numerosCuenta) {
}
