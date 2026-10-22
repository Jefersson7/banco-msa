package com.bank.cuentas.exception;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

/**
 * Manejo de excepciones unificado con ProblemDetail (RFC 7807).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessRuleException.class)
	public ProblemDetail handleBusinessRuleViolation(BusinessRuleException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
		problem.setTitle(ex.getStatus().getReasonPhrase());
		problem.setProperty("code", ex.getCode());
		return problem;
	}

	/**
	 * Dos actualizaciones concurrentes sobre la misma cuenta: la segunda
	 * encuentra la version cambiada y se rechaza para que reintente con el
	 * estado vigente.
	 */
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"El saldo de la cuenta cambio mientras se registraba el movimiento; reintente la operacion");
		problem.setTitle(HttpStatus.CONFLICT.getReasonPhrase());
		problem.setProperty("code", "SALDO_ACTUALIZADO_CONFLICTO");
		return problem;
	}

	@ExceptionHandler(PessimisticLockingFailureException.class)
	public ProblemDetail handlePessimisticLock(PessimisticLockingFailureException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"La cuenta esta siendo actualizada por otra operacion; reintente");
		problem.setTitle(HttpStatus.CONFLICT.getReasonPhrase());
		problem.setProperty("code", "BLOQUEO_CONFLICTO");
		return problem;
	}

	/**
	 * Egreso sin saldo suficiente: la peticion es valida pero el estado de la
	 * cuenta impide procesarla. Se devuelve el mensaje exacto de la regla.
	 */
	@ExceptionHandler(SaldoNoDisponibleException.class)
	public ProblemDetail handleSaldoNoDisponible(SaldoNoDisponibleException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT,
				ex.getMessage());
		problem.setTitle("Saldo insuficiente");
		problem.setProperty("code", "SALDO_NO_DISPONIBLE");
		return problem;
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"El parametro '" + ex.getName() + "' tiene un valor invalido");
		problem.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
		problem.setProperty("code", "PARAMETRO_INVALIDO");
		return problem;
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
		List<Map<String, String>> errores = ex.getConstraintViolations().stream()
				.map(violation -> Map.of(
						"campo", violation.getPropertyPath().toString(),
						"mensaje", Objects.requireNonNullElse(violation.getMessage(), "valor invalido")))
				.toList();
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Uno o mas campos son invalidos");
		problem.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
		problem.setProperty("code", "VALIDACION");
		problem.setProperty("fieldErrors", errores);
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidationError(MethodArgumentNotValidException ex) {
		List<Map<String, String>> errores = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> Map.of(
						"campo", error.getField(),
						"mensaje", Objects.requireNonNullElse(error.getDefaultMessage(), "valor invalido")))
				.toList();
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Uno o mas campos son invalidos");
		problem.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
		problem.setProperty("code", "VALIDACION");
		problem.setProperty("fieldErrors", errores);
		return problem;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleMalformedJson(HttpMessageNotReadableException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"El cuerpo de la peticion no es un JSON valido");
		problem.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
		problem.setProperty("code", "JSON_INVALIDO");
		return problem;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpectedError(Exception ex) {
		// Los errores que ya son del framework (ruta inexistente, metodo no permitido,
		// tipo de contenido no soportado) se devuelven tal cual.
		if (ex instanceof ErrorResponse errorResponse) {
			ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorResponse.getStatusCode(),
					errorResponse.getBody().getDetail());
			problem.setTitle(errorResponse.getBody().getTitle());
			problem.setProperty("code", "ERROR_" + errorResponse.getStatusCode().value());
			return problem;
		}
		log.error("Error no controlado en msa-cuentas", ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Ocurrio un error inesperado");
		problem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		problem.setProperty("code", "ERROR_INTERNO");
		return problem;
	}

}
