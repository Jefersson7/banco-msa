package com.bank.clientes.event;

/**
 * Evento de dominio que se publica al dar de baja un cliente. msa-cuentas lo
 * escucha para desactivar las cuentas que el cliente tenga activas.
 */
public record ClientDeactivatedEvent(String clientId) {
}
