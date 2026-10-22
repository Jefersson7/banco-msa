package com.bank.cuentas.exception;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
		problem.setType(URI.create("urn:banco:error:" + ex.getCode().toLowerCase()));
		problem.setProperty("code", ex.getCode());
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
		problem.setType(URI.create("urn:banco:error:validacion"));
		problem.setProperty("code", "VALIDACION");
		problem.setProperty("fieldErrors", errores);
		return problem;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleMalformedJson(HttpMessageNotReadableException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"El cuerpo de la peticion no es un JSON valido");
		problem.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
		problem.setType(URI.create("urn:banco:error:json_invalido"));
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
		problem.setType(URI.create("urn:banco:error:error_interno"));
		problem.setProperty("code", "ERROR_INTERNO");
		return problem;
	}

}
