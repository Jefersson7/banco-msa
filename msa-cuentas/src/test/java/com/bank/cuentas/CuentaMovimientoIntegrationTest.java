package com.bank.cuentas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bank.cuentas.integration.ClientGateway;
import com.bank.cuentas.model.Cuenta;
import com.bank.cuentas.repository.AccountRepository;
import com.bank.cuentas.repository.MovementRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(TestcontainersConfiguration.class)
class CuentaMovimientoIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private MovementRepository movementRepository;

	@MockitoBean
	private ClientGateway clientGateway;

	@BeforeEach
	void setup() {
		movementRepository.deleteAllInBatch();
		accountRepository.deleteAllInBatch();
		willDoNothing().given(clientGateway).verificarClienteActivo(anyString());
	}

	@Test
	void registraMovimientoYActualizaElSaldoPersistido() throws Exception {
		String crearCuentaJson = """
				{
				  "tipoCuenta": "CORRIENTE",
				  "saldoInicial": 500.00,
				  "clienteId": "CLI-TEST-01",
				  "nombreCliente": "Ana Test",
				  "identificacion": "12345678"
				}
				""";

		MvcResult cuentaResult = mockMvc.perform(post("/cuentas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(crearCuentaJson))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("/cuentas/")))
			.andExpect(jsonPath("$.saldoActual", is(500.00)))
			.andReturn();

		String responseJson = cuentaResult.getResponse().getContentAsString();
		String numeroCuenta = JsonPath.read(responseJson, "$.numeroCuenta");
		assertThat(numeroCuenta).isNotBlank();

		String crearMovimientoJson = """
				{
				  "cuenta": "%s",
				  "tipoMovimiento": "INGRESO",
				  "valor": 100.00
				}
				""".formatted(numeroCuenta);

		mockMvc.perform(post("/movimientos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(crearMovimientoJson))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.saldo", is(600.00)))
			.andExpect(jsonPath("$.tipoMovimiento", is("INGRESO")))
			.andExpect(jsonPath("$.valor", is(100.00)));

		mockMvc.perform(get("/cuentas/" + numeroCuenta))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.saldoActual", is(600.00)));

		Cuenta cuentaPersistida = accountRepository.findByAccountNumber(numeroCuenta).orElseThrow();
		assertThat(cuentaPersistida.getSaldoActual()).isEqualByComparingTo(new BigDecimal("600.00"));
		assertThat(movementRepository.count()).isEqualTo(1);
	}

	@Test
	void rechazaEgresoSinSaldoYSaldoSinCambios() throws Exception {
		String crearCuentaJson = """
				{
				  "tipoCuenta": "CORRIENTE",
				  "saldoInicial": 100.00,
				  "clienteId": "CLI-TEST-02",
				  "nombreCliente": "Luis Test",
				  "identificacion": "87654321"
				}
				""";

		MvcResult cuentaResult = mockMvc.perform(post("/cuentas")
				.contentType(MediaType.APPLICATION_JSON)
				.content(crearCuentaJson))
			.andExpect(status().isCreated())
			.andReturn();

		String responseJson = cuentaResult.getResponse().getContentAsString();
		String numeroCuenta = JsonPath.read(responseJson, "$.numeroCuenta");

		String crearMovimientoJson = """
				{
				  "cuenta": "%s",
				  "tipoMovimiento": "EGRESO",
				  "valor": -150.00
				}
				""".formatted(numeroCuenta);

		mockMvc.perform(post("/movimientos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(crearMovimientoJson))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code", is("SALDO_NO_DISPONIBLE")))
			.andExpect(jsonPath("$.title", is("Saldo insuficiente")))
			.andExpect(jsonPath("$.detail", is("Saldo no disponible")));

		mockMvc.perform(get("/cuentas/" + numeroCuenta))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.saldoActual", is(100.00)));

		Cuenta cuentaPersistida = accountRepository.findByAccountNumber(numeroCuenta).orElseThrow();
		assertThat(cuentaPersistida.getSaldoActual()).isEqualByComparingTo(new BigDecimal("100.00"));
		assertThat(movementRepository.count()).isEqualTo(0);
	}

}
