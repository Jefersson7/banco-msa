package com.bank.cuentas.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.cuentas.dto.ReporteEstadoCuentaResponse;
import com.bank.cuentas.service.ReporteService;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

	private final ReporteService reporteService;

	public ReporteController(ReporteService reporteService) {
		this.reporteService = reporteService;
	}

	@GetMapping
	public ReporteEstadoCuentaResponse generar(@RequestParam(name = "cliente") String clienteId,
			@RequestParam(name = "fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
			@RequestParam(name = "fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
		return reporteService.generar(clienteId, fechaInicio, fechaFin);
	}

}
