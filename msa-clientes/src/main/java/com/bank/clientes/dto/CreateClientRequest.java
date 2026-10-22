package com.bank.clientes.dto;

import com.bank.clientes.model.Genero;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(

		@NotBlank(message = "el nombre es obligatorio")
		@Size(max = 120, message = "el nombre no puede superar los 120 caracteres")
		String nombre,

		@NotNull(message = "el genero es obligatorio")
		Genero genero,

		@NotNull(message = "la edad es obligatoria")
		@Min(value = 0, message = "la edad no puede ser negativa")
		@Max(value = 120, message = "la edad no puede superar los 120 anios")
		Integer edad,

		@NotBlank(message = "la identificacion es obligatoria")
		@Size(max = 30, message = "la identificacion no puede superar los 30 caracteres")
		@Pattern(regexp = "\\d+", message = "la identificacion debe contener solo numeros")
		String identificacion,

		@Size(max = 200, message = "la direccion no puede superar los 200 caracteres")
		String direccion,

		@Size(max = 30, message = "el telefono no puede superar los 30 caracteres")
		String telefono,

		@NotBlank(message = "la contrasena es obligatoria")
		@Size(min = 8, max = 72, message = "la contrasena debe tener entre 8 y 72 caracteres")
		String password) {
}