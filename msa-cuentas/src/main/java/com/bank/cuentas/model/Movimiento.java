package com.bank.cuentas.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Movimiento registrado en una cuenta. El valor conserva su signo: negativo
 * para un egreso y positivo para un ingreso. "tipoMovimiento" llega en la API
 * y debe concordar con ese signo. "saldo" es el saldo resultante de la cuenta
 * despues de aplicar el valor.
 *
 * Los datos contables (cuenta, tipo, valor y saldo) se fijan al registrar el
 * movimiento y no cambian; solo los metadatos (fecha y referencia) se pueden
 * actualizar. No tiene setters.
 */
@Entity
@Table(name = "movimiento")
public class Movimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDateTime fecha;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_movimiento", nullable = false, length = 20)
	private TipoMovimiento tipoMovimiento;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal saldo;

	@Column(length = 60)
	private String referencia;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cuenta_id", nullable = false)
	private Cuenta cuenta;

	/**
	 * Constructor de JPA: la entidad se instancia vacia y la base de datos carga
	 * sus datos.
	 */
	protected Movimiento() {
	}

	/**
	 * Registra un movimiento completo con el saldo resultante que deja la
	 * operacion en la cuenta.
	 */
	public static Movimiento registrar(Cuenta cuenta, TipoMovimiento tipoMovimiento, BigDecimal valor,
			BigDecimal saldo, LocalDateTime fecha, String referencia) {
		Movimiento movimiento = new Movimiento();
		movimiento.cuenta = cuenta;
		movimiento.tipoMovimiento = tipoMovimiento;
		movimiento.valor = valor;
		movimiento.saldo = saldo;
		movimiento.fecha = fecha;
		movimiento.referencia = referencia;
		return movimiento;
	}

	/**
	 * Actualiza los datos no contables del movimiento. Un null deja intacto el
	 * valor actual.
	 */
	public void actualizarMetadatos(LocalDateTime fecha, String referencia) {
		if (fecha != null) {
			this.fecha = fecha;
		}
		if (referencia != null) {
			this.referencia = referencia;
		}
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public TipoMovimiento getTipoMovimiento() {
		return tipoMovimiento;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public String getReferencia() {
		return referencia;
	}

	public Cuenta getCuenta() {
		return cuenta;
	}

}
