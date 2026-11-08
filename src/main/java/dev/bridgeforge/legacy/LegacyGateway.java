package dev.bridgeforge.legacy;

import java.util.Optional;

public interface LegacyGateway {
    Optional<LegacyCustomer> fetch(Long id);
}
