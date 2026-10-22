package com.bank.clientes.event;

/**
 * Evento de dominio que se publica al dar de alta un cliente.
 */
public record ClientRegisteredEvent(String clientId, String clientName, String identification) {
}
