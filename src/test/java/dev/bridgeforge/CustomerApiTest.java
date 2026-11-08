package dev.bridgeforge;

import dev.bridgeforge.legacy.LegacyCustomer;
import dev.bridgeforge.legacy.LegacyGateway;
import dev.bridgeforge.repository.CustomerRepository;
import dev.bridgeforge.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:bridge_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=validate",
    "legacy.enabled=false",
    "outbox.webhook-url="
})
@AutoConfigureMockMvc
class CustomerApiTest {
    @Autowired MockMvc mvc;
    @Autowired CustomerRepository customers;
    @Autowired OutboxRepository outbox;
    @MockitoBean LegacyGateway legacy;

    @Test
    void registerThenReadModernCustomer() throws Exception {
        mvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"id":501,"fullName":"Camila Torres","email":"camila@example.org"}"))
            .andExpect(status().isCreated());
        mvc.perform(get("/api/v1/customers/501"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Data-Origin", "modern"))
            .andExpect(jsonPath("$.fullName", is("Camila Torres")));
        assertEquals(1L, outbox.findAll().stream()
            .filter(x -> x.getEventType().equals("customer.registered") && x.getPayload().contains("501"))
            .count());
    }

    @Test
    void migrateLegacyRecordAndPersistIt() throws Exception {
        when(legacy.fetch(902L)).thenReturn(Optional.of(
            new LegacyCustomer(902L, "Marco Vega", "marco@example.org")));
        mvc.perform(get("/api/v1/customers/902"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Data-Origin", "legacy-imported"));
        mvc.perform(get("/api/v1/customers/902"))
            .andExpect(header().string("X-Data-Origin", "modern"));
        assertEquals("Marco Vega", customers.findById(902L).orElseThrow().getFullName());
    }

    @Test
    void rejectInvalidRegistrationAndMissingCustomer() throws Exception {
        mvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"id":-1,"fullName":"","email":"invalid"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/customers/77777"))
            .andExpect(status().isNotFound());
    }

    @Test
    void duplicateCustomerIdReturnsConflict() throws Exception {
        String payload = "{\"id\":660,\"fullName\":\"Valeria Ruiz\",\"email\":\"valeria@example.org\"}";
        mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content(payload)).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content(payload)).andExpect(status().isConflict());
    }
}
