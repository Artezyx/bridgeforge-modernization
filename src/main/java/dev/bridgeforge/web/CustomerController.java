package dev.bridgeforge.web;

import dev.bridgeforge.model.Customer;
import dev.bridgeforge.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }

    public record RegisterCustomer(
        @NotNull @Positive Long id,
        @NotBlank @Size(max = 140) String fullName,
        @NotBlank @Email @Size(max = 240) String email) { }

    public record CustomerView(Long id, String fullName, String email, String status) {
        public static CustomerView from(Customer c) {
            return new CustomerView(c.getId(), c.getFullName(), c.getEmail(), c.getStatus());
        }
    }

    @PostMapping("/customers")
    public ResponseEntity<CustomerView> register(@Valid @RequestBody RegisterCustomer input) {
        Customer customer = service.register(input.id(), input.fullName(), input.email());
        return ResponseEntity.created(URI.create("/api/v1/customers/" + customer.getId()))
            .body(CustomerView.from(customer));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerView> lookup(@PathVariable @Positive Long id) {
        var found = service.lookup(id);
        return ResponseEntity.ok().header("X-Data-Origin", found.origin())
            .body(CustomerView.from(found.customer()));
    }

    @PostMapping("/migrations/customers/{id}")
    public ResponseEntity<CustomerView> migrate(@PathVariable @Positive Long id) {
        var found = service.lookup(id);
        return ResponseEntity.ok().header("X-Data-Origin", found.origin())
            .body(CustomerView.from(found.customer()));
    }
}
