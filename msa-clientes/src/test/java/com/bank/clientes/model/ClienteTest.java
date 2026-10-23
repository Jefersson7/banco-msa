package com.bank.clientes.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ClienteTest {

	private static final LocalDateTime FECHA_ALTA = LocalDateTime.of(2026, 1, 1, 9, 0);

	private Cliente clienteDePrueba() {
		DatosPersona datos = new DatosPersona(
				"Ana Perez", Genero.FEMENINO, 30, "12345678", "Calle 1", "3001234567");
		return new Cliente(datos, "CLI-0001", "hash", FECHA_ALTA);
	}

	@Test
	void naceActivoConSusDatos() {
		Cliente cliente = clienteDePrueba();

		assertThat(cliente.getEstado()).isEqualTo(EstadoCliente.ACTIVO);
		assertThat(cliente.estaActivo()).isTrue();
		assertThat(cliente.getClientId()).isEqualTo("CLI-0001");
		assertThat(cliente.getPasswordHash()).isEqualTo("hash");
		assertThat(cliente.getFechaAlta()).isEqualTo(FECHA_ALTA);
		assertThat(cliente.getNombre()).isEqualTo("Ana Perez");
		assertThat(cliente.getIdentificacion()).isEqualTo("12345678");
	}

	@Test
	void desactivarDejaClienteInactivoConservandoSusDatos() {
		Cliente cliente = clienteDePrueba();

		cliente.desactivar();

		assertThat(cliente.getEstado()).isEqualTo(EstadoCliente.INACTIVO);
		assertThat(cliente.estaActivo()).isFalse();

		cliente.desactivar();

		assertThat(cliente.getEstado()).isEqualTo(EstadoCliente.INACTIVO);
		assertThat(cliente.estaActivo()).isFalse();
		assertThat(cliente.getClientId()).isEqualTo("CLI-0001");
		assertThat(cliente.getNombre()).isEqualTo("Ana Perez");
	}

}
