package com.bank.cuentas.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Cuenta de un cliente. Los datos del cliente (clientId, nombreCliente,
 * identificacion) son una copia: msa-cuentas no consulta la base de msa-clientes.
 */
@Entity
@Table(name = "cuenta")
public class Cuenta {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cuenta_seq")
	@SequenceGenerator(name = "cuenta_seq", sequenceName = "cuenta_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
	private String numeroCuenta;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_cuenta", nullable = false, length = 20)
	private TipoCuenta tipoCuenta;

	@Column(name = "saldo_inicial", nullable = false, precision = 19, scale = 2)
	private BigDecimal saldoInicial;

	@Column(name = "saldo_actual", nullable = false, precision = 19, scale = 2)
	private BigDecimal saldoActual;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoCuenta estado;

	@Column(name = "cliente_id", nullable = false, length = 20)
	private String clienteId;

	@Column(name = "nombre_cliente", nullable = false, length = 120)
	private String nombreCliente;

	@Column(name = "identificacion", length = 30)
	private String identificacion;

	/**
	 * El numero de cuenta se deriva del id de la secuencia: 10 digitos.
	 */
	@PrePersist
	void assignAccountNumber() {
		if (numeroCuenta == null && id != null) {
			numeroCuenta = String.format("%010d", id);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public TipoCuenta getTipoCuenta() {
		return tipoCuenta;
	}

	public void setTipoCuenta(TipoCuenta tipoCuenta) {
		this.tipoCuenta = tipoCuenta;
	}

	public BigDecimal getSaldoInicial() {
		return saldoInicial;
	}

	public void setSaldoInicial(BigDecimal saldoInicial) {
		this.saldoInicial = saldoInicial;
	}

	public BigDecimal getSaldoActual() {
		return saldoActual;
	}

	public void setSaldoActual(BigDecimal saldoActual) {
		this.saldoActual = saldoActual;
	}

	public EstadoCuenta getEstado() {
		return estado;
	}

	public void setEstado(EstadoCuenta estado) {
		this.estado = estado;
	}

	public String getClienteId() {
		return clienteId;
	}

	public void setClienteId(String clienteId) {
		this.clienteId = clienteId;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public String getIdentificacion() {
		return identificacion;
	}

	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}

}
