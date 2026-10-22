package com.bank.clientes.dto;

import com.bank.clientes.model.Genero;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Actualizacion parcial (PATCH): solo se aplican los campos que llegan.
 */
public record PatchClientRequest(

		@Size(max = 120, message = "el nombre no puede superar los 120 caracteres")
		String nombre,

		Genero genero,

		@Min(value = 0, message = "la edad no puede ser negativa")
		@Max(value = 120, message = "la edad no puede superar los 120 anios")
		Integer edad,

		@Size(max = 30, message = "la identificacion no puede superar los 30 caracteres")
		@Pattern(regexp = "\\d+", message = "la identificacion debe contener solo numeros")
		String identificacion,

		@Size(max = 200, message = "la direccion no puede superar los 200 caracteres")
		String direccion,

		@Size(max = 30, message = "el telefono no puede superar los 30 caracteres")
		String telefono) {
}