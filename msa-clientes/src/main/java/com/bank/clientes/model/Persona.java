package com.bank.clientes.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Persona: entidad raiz de la jerarquia, con su propia clave primaria en la
 * tabla "persona". Cliente la hereda con estrategia JOINED, de modo que cada
 * entidad de la jerarquia tiene su tabla y sus propias restricciones:
 *
 * - persona: id (PK), nombre, genero, edad, identificacion (unica), direccion, telefono
 * - cliente: id (PK y FK a persona), cliente_id (unica), password_hash, estado, fecha_alta
 *
 * La clave primaria de Cliente es la que hereda de Persona: una sola identidad
 * para la persona y para su ficha de cliente. "clienteId" es la clave de
 * negocio unica con la que se expone el recurso en la API.
 *
 * Los datos de la persona solo se pueden cambiar a traves de
 * {@link #actualizar(DatosPersona)} (reemplazo completo) y
 * {@link #actualizarParcialmente(DatosPersona)} (solo los campos presentes):
 * la entidad es la dueña de sus datos y el servicio no los escribe campo a
 * campo.
 */
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_persona", length = 20)
public abstract class Persona {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Genero genero;

	@Column
	private Integer edad;

	@Column(nullable = false, unique = true, length = 30)
	private String identificacion;

	@Column(length = 200)
	private String direccion;

	@Column(length = 30)
	private String telefono;

	/**
	 * Constructor de JPA: la entidad se instancia vacia y la base de datos
	 * carga sus datos.
	 */
	protected Persona() {
	}

	public Persona(DatosPersona datos) {
		actualizar(datos);
	}

	/**
	 * Actualizacion completa (PUT): reemplaza todos los datos de la persona.
	 * Un dato ausente queda vacio.
	 */
	public void actualizar(DatosPersona datos) {
		this.nombre = datos.nombre();
		this.genero = datos.genero();
		this.edad = datos.edad();
		this.identificacion = datos.identificacion();
		this.direccion = datos.direccion();
		this.telefono = datos.telefono();
	}

	/**
	 * Actualizacion parcial (PATCH): solo se aplican los datos que llegan. Un
	 * dato null se interpreta como "no se modifica" y deja intacto el valor
	 * actual.
	 */
	public void actualizarParcialmente(DatosPersona datos) {
		if (datos.nombre() != null) {
			this.nombre = datos.nombre();
		}
		if (datos.genero() != null) {
			this.genero = datos.genero();
		}
		if (datos.edad() != null) {
			this.edad = datos.edad();
		}
		if (datos.identificacion() != null) {
			this.identificacion = datos.identificacion();
		}
		if (datos.direccion() != null) {
			this.direccion = datos.direccion();
		}
		if (datos.telefono() != null) {
			this.telefono = datos.telefono();
		}
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public Genero getGenero() {
		return genero;
	}

	public Integer getEdad() {
		return edad;
	}

	public String getIdentificacion() {
		return identificacion;
	}

	public String getDireccion() {
		return direccion;
	}

	public String getTelefono() {
		return telefono;
	}

}
