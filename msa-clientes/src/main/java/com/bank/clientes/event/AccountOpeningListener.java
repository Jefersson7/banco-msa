package com.bank.clientes.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.bank.clientes.notification.AccountNotifier;

/**
 * Escucha el alta de un cliente y avisa a msa-cuentas para abrir su cuenta corriente.
 *
 * AFTER_COMMIT: el aviso sale despues del commit, no dentro de la transaccion.
 * Si saliera antes, msa-cuentas podria recibir el evento cuando el cliente
 * todavia no existe.
 */
@Component
public class AccountOpeningListener {

	private final AccountNotifier accountNotifier;

	public AccountOpeningListener(AccountNotifier accountNotifier) {
		this.accountNotifier = accountNotifier;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onClientRegistered(ClientRegisteredEvent event) {
		accountNotifier.notifyAccountOpening(AccountNotifier.SolicitudAperturaCuenta.forClient(
				event.clientId(), event.clientName(), event.identification()));
	}

}
