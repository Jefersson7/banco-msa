package com.bank.cuentas.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.bank.cuentas.exception.BusinessRuleException;

/**
 * Cliente HTTP de msa-clientes. El estado del cliente es un dato de otro
 * microservicio: msa-cuentas no lo tiene en su base de datos, asi que antes de
 * abrir una cuenta consulta al dueno del dato y se asegura de que el cliente
 * exista y este activo.
 */
@Component
public class ClientGateway {

	private static final Logger log = LoggerFactory.getLogger(ClientGateway.class);

	private static final String ESTADO_ACTIVO = "ACTIVO";

	private final RestClient restClient;

	public ClientGateway(RestClient.Builder restClientBuilder,
			@Value("${app.ms.clientes.base-url}") String baseUrl) {
		this.restClient = restClientBuilder.baseUrl(baseUrl).build();
	}

	/**
	 * Verifica que el cliente exista y este activo. Falla de forma cerrada: si
	 * msa-clientes no esta disponible no se abre la cuenta, porque no se puede
	 * saber si el cliente sigue activo.
	 */
	public void verificarClienteActivo(String clientId) {
		ClienteRemoto cliente = verificarClienteExiste(clientId);
		if (!ESTADO_ACTIVO.equals(cliente.estado())) {
			throw new BusinessRuleException("CLIENTE_INACTIVO",
					"El cliente " + clientId + " esta inactivo: no se pueden abrir cuentas a su nombre",
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * Verifica que el cliente exista en msa-clientes y devuelve su proyeccion.
	 * No exige que este activo: el reporte de estado de cuenta debe poder
	 * consultarse aunque el cliente se haya dado de baja.
	 */
	public ClienteRemoto verificarClienteExiste(String clientId) {
		ClienteRemoto cliente;
		try {
			cliente = restClient.get()
					.uri("/clientes/{clientId}", clientId)
					.retrieve()
					.body(ClienteRemoto.class);
		}
		catch (HttpClientErrorException.NotFound ex) {
			throw new BusinessRuleException("CLIENTE_NO_ENCONTRADO",
					"No existe el cliente " + clientId, HttpStatus.NOT_FOUND);
		}
		catch (RestClientException ex) {
			log.error("No se pudo consultar el cliente {} en msa-clientes", clientId, ex);
			throw new BusinessRuleException("MS_CLIENTES_NO_DISPONIBLE",
					"No se pudo verificar el estado del cliente " + clientId + ": msa-clientes no responde",
					HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (cliente == null) {
			throw new BusinessRuleException("MS_CLIENTES_NO_DISPONIBLE",
					"No se pudo verificar el estado del cliente " + clientId + ": msa-clientes no responde",
					HttpStatus.SERVICE_UNAVAILABLE);
		}
		return cliente;
	}

	/**
	 * Proyeccion minima del cliente de msa-clientes: solo los campos que
	 * necesita msa-cuentas.
	 */
	public record ClienteRemoto(String clientId, String nombre, String identificacion, String estado) {
	}

}
