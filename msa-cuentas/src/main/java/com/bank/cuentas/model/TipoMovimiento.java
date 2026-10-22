package com.bank.cuentas.model;

/**
 * Tipo del movimiento. No se recibe en la API: lo define el signo del valor
 * (negativo es egreso, positivo o cero es ingreso). Se persiste para poder
 * listar y reportar por tipo sin recalcularlo en cada consulta.
 */
public enum TipoMovimiento {
	INGRESO,
	EGRESO
}