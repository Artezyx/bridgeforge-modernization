# BridgeForge security and performance verification

The API now denies unauthenticated requests. A read-only account can GET only already-migrated customers without side effects;
only ADMIN may POST registrations or legacy migrations. Health remains
public to allow container probes. The example uses stateless HTTP Basic over
TLS for local CLI demonstration; do **not** deploy it without HTTPS.

Set BRIDGEFORGE_ADMIN_PASSWORD (at least 16 characters) to enable startup.
Optionally set BRIDGEFORGE_READER_PASSWORD (also at least 16 characters).
The reader is not provisioned when its password is omitted.
Never commit these passwords. Use an enterprise OIDC/OAuth 2 identity
provider, tenant-specific authorization and an external secret manager
for production.

Run `mvn verify`: MockMvc tests check 401 for missing credentials, 403
for prohibited writes and successful reader routing. Existing migration
and outbox tests remain enabled.

A k6 smoke profile is in `load/bridgeforge-read.js`:

```sh
READER_USER=portfolio-reader READER_PASSWORD="<local test password>" \
CUSTOMER_ID=501 BASE_URL=http://localhost:8082 k6 run load/bridgeforge-read.js
```

Test only a disposable local stack with an existing customer. 8 VUs,
30 seconds and p95 under 700 ms are illustrative acceptance thresholds,
not verified throughput or latency measurements. Store actual results
separately and avoid hitting third-party legacy or webhook services.
