package com.bank.cuentas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
import jakarta.persistence.Version;

import com.bank.cuentas.exception.SaldoNoDisponibleException;

/**
 * Cuenta de un cliente. Los datos del cliente (clientId, nombreCliente,
 * identificacion) son una copia: msa-cuentas no consulta la base de msa-clientes.
 *
 * La entidad es duena de sus datos: se crea con {@link #abrir} y solo cambia a
 * traves de operaciones de negocio. No tiene setters.
 */
@Entity
@Table(name = "cuenta")
public class Cuenta {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cuenta_seq")
	@SequenceGenerator(name = "cuenta_seq", sequenceName = "cuenta_id_seq", allocationSize = 1)
	private Long id;

	/**
	 * Bloqueo optimista: dos movimientos concurrentes sobre la misma cuenta no
	 * pueden pisarse la actualizacion del saldo.
	 */
	@Version
	private Long version;

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
	 * Constructor de JPA: la entidad se instancia vacia y la base de datos carga
	 * sus datos.
	 */
	protected Cuenta() {
	}

	/**
	 * Alta de cuenta: nace activa y con el saldo actual igual al inicial.
	 */
	public static Cuenta abrir(TipoCuenta tipoCuenta, BigDecimal saldoInicial, String clienteId,
			String nombreCliente, String identificacion) {
		Cuenta cuenta = new Cuenta();
		cuenta.tipoCuenta = tipoCuenta;
		cuenta.saldoInicial = saldoInicial.setScale(2, RoundingMode.HALF_UP);
		cuenta.saldoActual = cuenta.saldoInicial;
		cuenta.estado = EstadoCuenta.ACTIVA;
		cuenta.clienteId = clienteId;
		cuenta.nombreCliente = nombreCliente;
		cuenta.identificacion = identificacion;
		return cuenta;
	}

	/**
	 * Actualizacion completa (PUT): reemplaza el tipo y el estado.
	 */
	public void actualizar(TipoCuenta tipoCuenta, EstadoCuenta estado) {
		this.tipoCuenta = tipoCuenta;
		this.estado = estado;
	}

	/**
	 * Actualizacion parcial (PATCH): solo aplica los campos que llegan. Un null
	 * deja intacto el valor actual.
	 */
	public void actualizarParcialmente(TipoCuenta tipoCuenta, EstadoCuenta estado) {
		if (tipoCuenta != null) {
			this.tipoCuenta = tipoCuenta;
		}
		if (estado != null) {
			this.estado = estado;
		}
	}

	/**
	 * Cambia el estado de la cuenta (baja masiva de un cliente).
	 */
	public void cambiarEstado(EstadoCuenta estado) {
		this.estado = estado;
	}

	/**
	 * Indica si la cuenta admite movimientos: solo las cuentas ACTIVA.
	 */
	public boolean estaActiva() {
		return this.estado == EstadoCuenta.ACTIVA;
	}

	/**
	 * Aplica un movimiento con signo al saldo disponible y devuelve el saldo
	 * resultante. Es el unico camino para modificar el saldo. Un egreso que
	 * dejaria el saldo por debajo de cero se rechaza con
	 * {@link SaldoNoDisponibleException}; si se rechaza, el saldo no cambia.
	 * Un saldo resultante de cero es valido.
	 */
	public BigDecimal aplicarMovimiento(BigDecimal valor) {
		BigDecimal saldoResultante = this.saldoActual.add(valor);
		if (valor.signum() < 0 && saldoResultante.signum() < 0) {
			throw new SaldoNoDisponibleException();
		}
		this.saldoActual = saldoResultante;
		return this.saldoActual;
	}

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

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public TipoCuenta getTipoCuenta() {
		return tipoCuenta;
	}

	public BigDecimal getSaldoInicial() {
		return saldoInicial;
	}

	public BigDecimal getSaldoActual() {
		return saldoActual;
	}

	public EstadoCuenta getEstado() {
		return estado;
	}

	public String getClienteId() {
		return clienteId;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public String getIdentificacion() {
		return identificacion;
	}

}
