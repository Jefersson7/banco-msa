package com.bank.cuentas.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.cuentas.dto.UpdateMovementRequest;
import com.bank.cuentas.dto.CreateMovementRequest;
import com.bank.cuentas.dto.MovementResponse;
import com.bank.cuentas.service.MovementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/movimientos")
public class MovementController {

	private final MovementService movementService;

	public MovementController(MovementService movementService) {
		this.movementService = movementService;
	}

	@PostMapping
	public ResponseEntity<MovementResponse> create(@Valid @RequestBody CreateMovementRequest request) {
		MovementResponse created = movementService.create(request);
		return ResponseEntity.created(URI.create("/movimientos/" + created.id())).body(created);
	}

	@GetMapping
	public Page<MovementResponse> findAll(@RequestParam(name = "cuenta", required = false) String accountNumber,
			@PageableDefault(size = 20, sort = "id") Pageable pageable) {
		return movementService.findAll(accountNumber, pageable);
	}

	@GetMapping("/{id}")
	public MovementResponse findById(@PathVariable Long id) {
		return movementService.findById(id);
	}

	@PutMapping("/{id}")
	public MovementResponse update(@PathVariable Long id,
			@RequestBody UpdateMovementRequest request) {
		return movementService.update(id, request);
	}

}
