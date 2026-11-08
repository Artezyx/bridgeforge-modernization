package dev.bridgeforge.service;
public final class CustomerNotFound extends RuntimeException {
    public CustomerNotFound(Long id) { super("Customer " + id + " not found."); }
}
