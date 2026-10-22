package com.bank.cuentas.dto;

import java.util.List;

public record ReporteEstadoCuentaResponse(

		String clienteId,
		String nombreCliente,
		List<ReporteCuentaResponse> cuentas) {
}
