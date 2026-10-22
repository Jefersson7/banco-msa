package com.bank.clientes.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Hash de la contrasena con BCrypt. Se usa solo el modulo spring-security-crypto
 * para no instalar la cadena de filtros de Spring Security.
 */
@Component
public class PasswordHasher {

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	public String hashPassword(String passwordEnClaro) {
		return encoder.encode(passwordEnClaro);
	}

}
