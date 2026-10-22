package com.bank.clientes.model;

/**
 * Genero de la persona. Se modela como enum y no como texto libre para que el
 * dato sea validable en el borde de la API.
 */
public enum Genero {
	MASCULINO,
	FEMENINO,
	OTRO
}