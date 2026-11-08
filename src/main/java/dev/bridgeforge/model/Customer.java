package dev.bridgeforge.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    private Long id;
    @Column(name = "full_name", nullable = false, length = 140)
    private String fullName;
    @Column(nullable = false, unique = true, length = 240)
    private String email;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Version
    @Column(nullable = false)
    private Long version;

    protected Customer() { }
    public Customer(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
