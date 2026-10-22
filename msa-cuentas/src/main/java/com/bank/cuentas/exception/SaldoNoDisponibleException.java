package com.bank.cuentas.exception;

/**
 * Se lanza cuando un egreso dejaria el saldo de la cuenta por debajo de cero.
 * El mensaje es fijo: es el que exige la regla de negocio ("Saldo no disponible").
 */
public class SaldoNoDisponibleException extends RuntimeException {

	public SaldoNoDisponibleException() {
		super("Saldo no disponible");
	}

}
