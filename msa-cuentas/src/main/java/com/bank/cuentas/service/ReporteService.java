package com.bank.cuentas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.cuentas.dto.MovementResponse;
import com.bank.cuentas.dto.ReporteCuentaResponse;
import com.bank.cuentas.dto.ReporteEstadoCuentaResponse;
import com.bank.cuentas.exception.BusinessRuleException;
import com.bank.cuentas.integration.ClientGateway;
import com.bank.cuentas.model.Cuenta;
import com.bank.cuentas.model.Movimiento;
import com.bank.cuentas.repository.MovementRepository;

@Service
public class ReporteService {

	private final MovementRepository movementRepository;

	private final ClientGateway clientGateway;

	public ReporteService(MovementRepository movementRepository, ClientGateway clientGateway) {
		this.movementRepository = movementRepository;
		this.clientGateway = clientGateway;
	}

	/**
	 * Estado de cuenta de un cliente entre dos fechas (inclusive, dia completo).
	 * Solo devuelve las cuentas que tuvieron al menos un movimiento en el rango:
	 * el reporte es de movimientos, no de cuentas.
	 */
	@Transactional(readOnly = true)
	public ReporteEstadoCuentaResponse generar(String clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
		if (fechaInicio.isAfter(fechaFin)) {
			throw new BusinessRuleException("RANGO_INVALIDO",
					"La fecha de inicio no puede ser posterior a la fecha de fin", HttpStatus.BAD_REQUEST);
		}

		// El estado del cliente vive en msa-clientes; se consulta alli y ademas se
		// reutiliza su nombre para el encabezado del reporte.
		ClientGateway.ClienteRemoto cliente = clientGateway.verificarClienteExiste(clienteId);

		LocalDateTime desde = fechaInicio.atStartOfDay();
		LocalDateTime hasta = fechaFin.atTime(LocalTime.MAX);
		List<Movimiento> movimientos = movementRepository.findByClientAndFechaBetween(clienteId, desde, hasta);

		if (movimientos.isEmpty()) {
			throw new BusinessRuleException("MOVIMIENTOS_NO_ENCONTRADOS",
					"El cliente " + clienteId + " no tiene movimientos en el rango indicado",
					HttpStatus.NOT_FOUND);
		}

		return new ReporteEstadoCuentaResponse(clienteId, cliente.nombre(), agruparPorCuenta(movimientos));
	}

	/**
	 * Agrupa los movimientos por cuenta conservando el orden de llegada (fecha
	 * ascendente) y calcula el saldo al inicio del rango.
	 */
	private List<ReporteCuentaResponse> agruparPorCuenta(List<Movimiento> movimientos) {
		Map<String, List<Movimiento>> porCuenta = new LinkedHashMap<>();
		for (Movimiento movimiento : movimientos) {
			porCuenta.computeIfAbsent(movimiento.getCuenta().getNumeroCuenta(), numero -> new ArrayList<>())
					.add(movimiento);
		}

		List<ReporteCuentaResponse> cuentas = new ArrayList<>();
		for (List<Movimiento> movimientosDeCuenta : porCuenta.values()) {
			Cuenta cuenta = movimientosDeCuenta.get(0).getCuenta();
			BigDecimal neto = movimientosDeCuenta.stream()
					.map(Movimiento::getValor)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal saldoAlInicio = cuenta.getSaldoActual().subtract(neto);
			cuentas.add(new ReporteCuentaResponse(cuenta.getNumeroCuenta(), cuenta.getTipoCuenta(),
					cuenta.getSaldoActual(), saldoAlInicio,
					movimientosDeCuenta.stream().map(MovementResponse::from).toList()));
		}
		return cuentas;
	}

}
