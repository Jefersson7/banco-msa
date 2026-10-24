package com.bank.clientes.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.clientes.dto.PatchClientRequest;
import com.bank.clientes.dto.UpdateClientRequest;
import com.bank.clientes.dto.ClientResponse;
import com.bank.clientes.dto.CreateClientRequest;
import com.bank.clientes.service.ClientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClientController {

	private final ClientService clientService;

	public ClientController(ClientService clientService) {
		this.clientService = clientService;
	}

	@PostMapping
	public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
		ClientResponse created = clientService.create(request);
		return ResponseEntity.created(URI.create("/clientes/" + created.clientId())).body(created);
	}

	@GetMapping
	public Page<ClientResponse> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
		return clientService.findAll(pageable);
	}

	@GetMapping("/{clientId}")
	public ClientResponse findByClientId(@PathVariable String clientId) {
		return clientService.findByClientId(clientId);
	}

	@PutMapping("/{clientId}")
	public ClientResponse update(@PathVariable String clientId,
			@Valid @RequestBody UpdateClientRequest request) {
		return clientService.update(clientId, request);
	}

	@PatchMapping("/{clientId}")
	public ClientResponse updatePartially(@PathVariable String clientId,
			@Valid @RequestBody PatchClientRequest request) {
		return clientService.updatePartially(clientId, request);
	}

	@DeleteMapping("/{clientId}")
	public ResponseEntity<Void> deactivate(@PathVariable String clientId) {
		clientService.deactivate(clientId);
		return ResponseEntity.noContent().build();
	}

}
