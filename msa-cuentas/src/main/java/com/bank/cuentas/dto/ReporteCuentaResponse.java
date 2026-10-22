package com.bank.cuentas.dto;

import java.math.BigDecimal;
import java.util.List;

import com.bank.cuentas.model.TipoCuenta;

public record ReporteCuentaResponse(

		String numeroCuenta,
		TipoCuenta tipoCuenta,
		BigDecimal saldoActual,
		BigDecimal saldoAlInicio,
		List<MovementResponse> movimientos) {
}
