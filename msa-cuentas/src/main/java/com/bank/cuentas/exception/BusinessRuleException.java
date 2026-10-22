package com.bank.cuentas.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepcion de negocio: unica para todos los casos de regla, cada uno con su
 * codigo y su status. El codigo viaja en el ProblemDetail de la respuesta.
 */
public class BusinessRuleException extends RuntimeException {

	private final String codigo;

	private final HttpStatus status;

	public BusinessRuleException(String codigo, String mensaje, HttpStatus status) {
		super(mensaje);
		this.codigo = codigo;
		this.status = status;
	}

	public String getCode() {
		return codigo;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
