# Canales Backend for Frontend

## Propósito

Los tres BFF separan las necesidades de web, móvil y cajero automático. Cada aplicación mantiene su propio contrato, configuración y política de autorización, lo que permite ajustar un canal sin cambiar los otros dos.

## Componentes

| Componente | Función | Adaptación del canal |
|---|---|---|
| `bff-web` | Dashboard de cliente y creación de pagos | Combina datos detallados y conserva resultados parciales en consultas |
| `bff-mobile` | Resumen de cliente y transferencias | Reduce el payload y limita los movimientos recientes |
| `bff-atm` | Consulta de saldo y retiros | Usa contratos mínimos, límite de retiro e idempotencia |

No existe dependencia de código entre los BFF. Cada módulo tiene su propio ejecutable, controlador, DTOs, cliente HTTP y configuración de resiliencia.

## Seguridad

- Las aplicaciones no mantienen sesión y validan JWT como OAuth2 Resource Servers.
- Los scopes `web`, `mobile`, `atm.read` y `atm.withdraw` separan los permisos por canal y operación.
- El bearer token entrante se propaga a los servicios de negocio para mantener la autorización extremo a extremo.
- Las rutas de negocio están cerradas por defecto; `health` e `info` permanecen disponibles para supervisión.
- HTTPS se activa mediante variables de entorno. El certificado autofirmado incluido se limita al desarrollo local.
- Retiros, transferencias y pagos exigen una clave de idempotencia.

## Resiliencia

Las llamadas a clientes, cuentas y pagos están protegidas por circuit breakers de Resilience4j. Web y móvil pueden entregar información parcial cuando falla una consulta. Las operaciones que modifican dinero responden `503 Service Unavailable` si el servicio requerido no está disponible.

Spring Cloud LoadBalancer resuelve los destinos mediante los nombres registrados en Eureka. Un cliente HTTP normal separado se reserva para la comunicación del propio cliente Eureka, evitando ciclos durante el arranque.

## Integración comprobada

- Los tres canales arrancaron de forma independiente.
- Las rutas de salud respondieron correctamente.
- Una ruta de negocio sin token devolvió `401 Unauthorized`.
- El BFF móvil ejecutó una transferencia con un token real.
- El reintento con la misma clave devolvió el mismo pago sin repetir el débito.
- El resumen móvil reunió cliente, cuentas y movimientos sin datos parciales.
- Los tres BFF se registraron correctamente en Eureka.
