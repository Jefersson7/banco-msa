package com.bank.clientes.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.bank.clientes.notification.AccountNotifier;

/**
 * Escucha la baja de un cliente y avisa a msa-cuentas para desactivar sus
 * cuentas activas.
 *
 * AFTER_COMMIT: el aviso sale despues del commit, igual que en el alta, para que
 * msa-cuentas nunca reciba el evento antes de que el cliente exista.
 */
@Component
public class AccountDeactivationListener {

	private final AccountNotifier accountNotifier;

	public AccountDeactivationListener(AccountNotifier accountNotifier) {
		this.accountNotifier = accountNotifier;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onClientDeactivated(ClientDeactivatedEvent event) {
		accountNotifier.notifyAccountDeactivation(event.clientId());
	}

}
