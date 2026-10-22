package com.bank.clientes.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Cliente: hereda de Persona, de la que aporta nombre, identificacion,
 * direccion y telefono. La clave primaria la hereda tambien (id), que en la
 * tabla "cliente" es a la vez PK y FK a "persona"; clientId es la clave unica
 * de negocio que usa la API.
 *
 * El estado es del dominio: un cliente nace activo y se da de baja con
 * {@link #desactivar()}, nunca asignando el estado a mano.
 */
@Entity
@Table(name = "cliente")
@DiscriminatorValue("CLIENTE")
public class Cliente extends Persona {

	@Column(name = "cliente_id", nullable = false, unique = true, length = 20)
	private String clientId;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoCliente estado;

	@Column(name = "fecha_alta", nullable = false)
	private LocalDateTime fechaAlta;

	/**
	 * Constructor de JPA: la entidad se instancia vacia y la base de datos
	 * carga sus datos.
	 */
	protected Cliente() {
	}

	/**
	 * Alta de cliente: nace activo y con la fecha de alta del momento.
	 */
	public Cliente(DatosPersona datos, String clientId, String passwordHash, LocalDateTime fechaAlta) {
		super(datos);
		this.clientId = clientId;
		this.passwordHash = passwordHash;
		this.fechaAlta = fechaAlta;
		this.estado = EstadoCliente.ACTIVO;
	}

	/**
	 * Baja logica: el cliente pasa a INACTIVO. No se borra, porque sus cuentas
	 * y movimientos deben conservar a su dueno.
	 */
	public void desactivar() {
		this.estado = EstadoCliente.INACTIVO;
	}

	public boolean estaActivo() {
		return this.estado == EstadoCliente.ACTIVO;
	}

	public String getClientId() {
		return clientId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public EstadoCliente getEstado() {
		return estado;
	}

	public LocalDateTime getFechaAlta() {
		return fechaAlta;
	}

}
