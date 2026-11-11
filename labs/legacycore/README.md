# LegacyCore — Secure SOAP-to-JSON Adapter

This standalone C#/.NET adapter models a common enterprise modernization task:
accept legacy SOAP 1.1 customer responses, reject malformed or unsafe XML,
validate a customer record, and emit a typed JSON representation.

## Run

```bash
dotnet build labs/legacycore/LegacyCore/LegacyCore.csproj
dotnet run --project labs/legacycore/LegacyCore/LegacyCore.csproj -- labs/legacycore/examples/customer.xml
python -m unittest discover -s labs/legacycore/tests -v
```

The original BridgeForge Java REST API remains unchanged. The C# adapter can
serve as a contract translator in a *future* integration; it is not connected
to that service yet. Unit/integration tests exercise the compiled .NET CLI
against real XML fixtures, including an XXE attack payload.

## Safety and scope

Uses XmlReader with DTD parsing prohibited, no external XML resolver, limits
on document size and bounded field lengths. SOAP faults and malformed data
produce a nonzero exit status. Production SOAP APIs would additionally
require HTTPS transport, authentication, timeouts, retry policies, telemetry
and canonical namespace/schema contracts.

No Siebel installation or VB.NET-specific integration is claimed.
