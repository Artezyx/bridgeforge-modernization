package dev.bridgeforge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.bridgeforge.legacy.LegacyCustomer;
import dev.bridgeforge.legacy.LegacyGateway;
import dev.bridgeforge.model.Customer;
import dev.bridgeforge.model.OutboxEvent;
import dev.bridgeforge.repository.CustomerRepository;
import dev.bridgeforge.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final OutboxRepository events;
    private final LegacyGateway legacy;
    private final ObjectMapper json;

    public CustomerService(CustomerRepository customers, OutboxRepository events,
                           LegacyGateway legacy, ObjectMapper json) {
        this.customers = customers;
        this.events = events;
        this.legacy = legacy;
        this.json = json;
    }

    @Transactional
    public LookupResult lookup(Long id) {
        var found = customers.findById(id);
        if (found.isPresent()) return new LookupResult(found.get(), "modern");
        LegacyCustomer remote = legacy.fetch(id).orElseThrow(() -> new CustomerNotFound(id));
        if (remote.id() == null || !remote.id().equals(id) || remote.name() == null ||
            remote.email() == null || remote.email().isBlank() || remote.name().length() > 140 ||
            remote.email().length() > 240)
            throw new UpstreamUnavailable("Legacy record is incomplete or inconsistent.");
        Customer imported = customers.save(new Customer(id, remote.name().trim(), remote.email().trim()));
        appendEvent(imported, "customer.imported");
        return new LookupResult(imported, "legacy-imported");
    }

    @Transactional
    public Customer register(Long id, String fullName, String email) {
        if (customers.existsById(id)) throw new CustomerConflict("Customer ID already exists.");
        Customer customer = customers.save(new Customer(id, fullName.trim(), email.trim()));
        appendEvent(customer, "customer.registered");
        return customer;
    }

    private void appendEvent(Customer customer, String type) {
        try {
            String payload = json.writeValueAsString(Map.of(
                "customerId", customer.getId(), "eventType", type));
            events.save(new OutboxEvent(customer.getId(), type, payload));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize event", ex);
        }
    }

    public record LookupResult(Customer customer, String origin) { }
}
