# BridgeForge architecture

```
Client → CustomerController → CustomerService ──→ PostgreSQL (customers)
                              │                   └→ PostgreSQL (outbox_events)
                              └→ LegacyGateway → Legacy REST API

OutboxDispatcher → PENDING outbox records → HTTP event consumer
```

**Strangler pattern:** Routes stay stable while source-of-truth changes incrementally. Modernized rows are read from PostgreSQL. Unknown IDs use the legacy adapter only when enabled. Imports do not mutate the remote legacy service.

**Consistency:** A transaction commits customer data and its event atomically. Only successful sends mark an event as published. Events can be delivered more than once if delivery succeeds but the process crashes before committing a status update; downstream consumers must deduplicate using the event ID.

**Failure cases:** `404` when both sources have no record or fallback is disabled; `409` on conflicting IDs/emails; `502` when the legacy service is unavailable. Webhook retry is bounded to five attempts and preserves failed records for investigation.

**Next hardening steps:** distributed outbox claim/lease for multiple replicas, OpenTelemetry traces, circuit breakers, access control, pagination, contract testing against a real legacy endpoint and gradual routing with feature flags.
