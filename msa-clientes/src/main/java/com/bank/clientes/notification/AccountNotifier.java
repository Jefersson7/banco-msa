package com.bank.clientes.notification;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente HTTP de msa-cuentas. La llamada es asincrona (@Async) y fire-and-forget:
 * si msa-cuentas no esta disponible, el alta del cliente no se cae.
 */
@Component
public class AccountNotifier {

	private static final Logger log = LoggerFactory.getLogger(AccountNotifier.class);

	private final RestClient restClient;

	public AccountNotifier(RestClient.Builder restClientBuilder,
			@Value("${app.ms.cuentas.base-url}") String baseUrl) {
		this.restClient = restClientBuilder.baseUrl(baseUrl).build();
	}

	@Async
	public void notifyAccountOpening(SolicitudAperturaCuenta solicitud) {
		try {
			restClient.post()
					.uri("/cuentas")
					.body(solicitud)
					.retrieve()
					.toBodilessEntity();
			log.info("Cuenta corriente abierta para el cliente {}", solicitud.clienteId());
		}
		catch (RestClientException ex) {
			log.error("No se pudo notificar la apertura de cuenta del cliente {}", solicitud.clienteId(), ex);
		}
	}

	/**
	 * Avisa a msa-cuentas para que desactive las cuentas activas del cliente.
	 * Misma politica que la apertura: asincrono y fire-and-forget.
	 */
	@Async
	public void notifyAccountDeactivation(String clientId) {
		try {
			restClient.patch()
					.uri(uriBuilder -> uriBuilder.path("/cuentas").queryParam("clienteId", clientId).build())
					.body(new ActualizacionCuentas("INACTIVA"))
					.retrieve()
					.toBodilessEntity();
			log.info("Cuentas desactivadas para el cliente {}", clientId);
		}
		catch (RestClientException ex) {
			log.error("No se pudo notificar la desactivacion de cuentas del cliente {}", clientId, ex);
		}
	}

	/**
	 * Payload del contrato de apertura de cuenta. Los nombres de los componentes
	 * son los del contrato JSON con msa-cuentas.
	 */
	public record SolicitudAperturaCuenta(
			String tipoCuenta,
			BigDecimal saldoInicial,
			String clienteId,
			String nombreCliente,
			String identificacion) {

		public static SolicitudAperturaCuenta forClient(String clientId, String clientName, String identification) {
			return new SolicitudAperturaCuenta("CORRIENTE", BigDecimal.ZERO.setScale(2), clientId, clientName,
					identification);
		}
	}

	/**
	 * Payload del contrato de actualizacion de cuentas: se aplica a las cuentas
	 * activas del cliente (PATCH /cuentas?clienteId=...).
	 */
	public record ActualizacionCuentas(String estado) {
	}

}
