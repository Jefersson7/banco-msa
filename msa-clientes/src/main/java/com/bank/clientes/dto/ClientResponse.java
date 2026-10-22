package com.bank.clientes.dto;

import java.time.LocalDateTime;

import com.bank.clientes.model.Cliente;
import com.bank.clientes.model.EstadoCliente;
import com.bank.clientes.model.Genero;

public record ClientResponse(

		String clientId,
		String nombre,
		Genero genero,
		Integer edad,
		String identificacion,
		String direccion,
		String telefono,
		EstadoCliente estado,
		LocalDateTime fechaAlta) {

	public static ClientResponse from(Cliente cliente) {
		return new ClientResponse(
				cliente.getClientId(),
				cliente.getNombre(),
				cliente.getGenero(),
				cliente.getEdad(),
				cliente.getIdentificacion(),
				cliente.getDireccion(),
				cliente.getTelefono(),
				cliente.getEstado(),
				cliente.getFechaAlta());
	}

}