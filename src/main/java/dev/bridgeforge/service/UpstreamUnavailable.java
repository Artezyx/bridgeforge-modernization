package dev.bridgeforge.service;
public final class UpstreamUnavailable extends RuntimeException {
    public UpstreamUnavailable(String message) { super(message); }
}
