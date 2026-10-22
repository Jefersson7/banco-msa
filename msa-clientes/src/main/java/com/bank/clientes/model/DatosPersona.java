package com.bank.clientes.model;

/**
 * Datos de la persona que la entidad {@link Persona} puede actualizar. Es un
 * objeto del dominio: el servicio lo construye a partir del DTO de la API y
 * se lo pasa a la entidad, de modo que el dominio no depende de la capa web.
 *
 * En una actualizacion parcial (PATCH) un campo null significa "este dato no
 * se modifica"; en una actualizacion completa (PUT) significa "este dato se
 * deja vacio".
 */
public record DatosPersona(

		String nombre,

		Genero genero,

		Integer edad,

		String identificacion,

		String direccion,

		String telefono) {
}
