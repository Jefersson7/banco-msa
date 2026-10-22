package com.bank.cuentas.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.cuentas.dto.UpdateAccountRequest;
import com.bank.cuentas.dto.AccountResponse;
import com.bank.cuentas.dto.AccountsUpdatedResponse;
import com.bank.cuentas.dto.CreateAccountRequest;
import com.bank.cuentas.dto.PatchAccountRequest;
import com.bank.cuentas.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cuentas")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
		AccountResponse created = accountService.create(request);
		return ResponseEntity.created(URI.create("/cuentas/" + created.numeroCuenta())).body(created);
	}

	@PatchMapping
	public AccountsUpdatedResponse updateByClientId(
			@RequestParam(name = "clienteId") String clientId,
			@RequestBody PatchAccountRequest request) {
		return accountService.updateByClientId(clientId, request);
	}

	@GetMapping
	public Page<AccountResponse> findAll(@RequestParam(name = "clienteId", required = false) String clientId,
			@PageableDefault(size = 20, sort = "id") Pageable pageable) {
		return accountService.findAll(clientId, pageable);
	}

	@GetMapping("/{accountNumber}")
	public AccountResponse findByAccountNumber(@PathVariable String accountNumber) {
		return accountService.findByAccountNumber(accountNumber);
	}

	@PatchMapping("/{accountNumber}")
	public AccountResponse updatePartially(@PathVariable String accountNumber,
			@RequestBody PatchAccountRequest request) {
		return accountService.updatePartially(accountNumber, request);
	}

	@PutMapping("/{accountNumber}")
	public AccountResponse update(@PathVariable String accountNumber,
			@Valid @RequestBody UpdateAccountRequest request) {
		return accountService.update(accountNumber, request);
	}

}