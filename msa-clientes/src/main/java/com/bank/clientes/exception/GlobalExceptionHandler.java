package com.bank.clientes.exception;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		if (isUniqueViolation(ex)) {
			ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
					"Ya existe un registro con los mismos datos");
			problem.setTitle(HttpStatus.CONFLICT.getReasonPhrase());
			problem.setProperty("code", "CONFLICTO_DATOS");
			return problem;
		}
		log.error("Error de integridad no controlado en msa-clientes", ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Ocurrio un error inesperado");
		problem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		problem.setProperty("code", "ERROR_INTERNO");
		return problem;
	}

	private boolean isUniqueViolation(DataIntegrityViolationException ex) {
		Throwable cause = ex.getMostSpecificCause();
		return cause instanceof SQLException sqlException
				&& "23505".equals(sqlException.getSQLState());
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
		log.error("Error no controlado en msa-clientes", ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Ocurrio un error inesperado");
		problem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		problem.setProperty("code", "ERROR_INTERNO");
		return problem;
	}

}
