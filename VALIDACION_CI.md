# Validación automatizada — BridgeForge

Este documento describe únicamente verificaciones reproducibles del repositorio.

## Ejecución local

Requisitos: Java 21 y Maven.

```bash
mvn -B --no-transfer-progress verify
```

## Pruebas incluidas

El archivo `src/test/java/dev/bridgeforge/CustomerApiTest.java` usa JUnit 5, MockMvc, Mockito y H2 en modo de compatibilidad PostgreSQL para probar:

- Registro y consulta de clientes vía REST y el evento de outbox correspondiente.
- Importación desde el adaptador de un sistema legado simulado.
- Respuestas de error ante registros inválidos o faltantes.
- Conflictos cuando se repite un identificador de cliente.

## Automatización

`.github/workflows/ci.yml` ejecuta `mvn -B --no-transfer-progress verify` con Java 21 en cada push y pull request.

Historial de ejecuciones: https://github.com/Artezyx/bridgeforge-modernization/actions

La ejecución verde acredita las comprobaciones del flujo de CI sobre el commit señalado en GitHub, **no** certifica despliegue en producción, infraestructura externa ni pruebas de carga.

## Limitaciones

Estas pruebas usan H2 y un adaptador legado simulado. No sustituyen una verificación end-to-end con PostgreSQL real y el sistema legado operativo, ni prueban el comportamiento de producción bajo concurrencia.
