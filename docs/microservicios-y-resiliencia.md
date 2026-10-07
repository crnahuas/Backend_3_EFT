# Microservicios y resiliencia

## Visión general

Los dominios de clientes, cuentas y pagos funcionan como aplicaciones independientes. Cada servicio conserva su propia base lógica, valida el token recibido y se registra en Eureka para que los consumidores puedan localizarlo sin direcciones fijas.

## Componentes de negocio

| Servicio | Responsabilidad | Controles principales |
|---|---|---|
| `customer-service` | Perfiles, contacto, estado y actividad del cliente | Validación, control de versión y consumo idempotente de eventos |
| `account-service` | Apertura, saldo, débito, crédito y cierre de cuentas | Bloqueo pesimista, control de versión y validación de fondos |
| `payment-service` | Pagos, depósitos, transferencias y retiros | Idempotencia, circuit breaker, compensación y eventos Kafka |

## Infraestructura compartida

| Componente | Uso |
|---|---|
| `config-server` | Entrega propiedades comunes y específicas por aplicación |
| `discovery-server` | Registra instancias mediante Eureka |
| `auth-server` | Emite JWT para web, móvil, cajero y comunicación interna |
| Spring Cloud LoadBalancer | Distribuye llamadas entre instancias disponibles |
| Resilience4j | Limita la propagación de fallos y activa respuestas alternativas |
| PostgreSQL | Mantiene una base lógica por dominio |
| Kafka | Transporta transacciones completadas y alertas de seguridad |

## Consistencia distribuida

Las operaciones de saldo usan bloqueo pesimista y una columna de versión. Cada solicitud financiera exige una clave de idempotencia única. Una transferencia debita la cuenta de origen y, si falla el crédito de destino, intenta una operación compensatoria. Los pagos conservan los estados `PENDIENTE`, `APROBADO` o `RECHAZADO` para facilitar el seguimiento.

El flujo utiliza una saga orquestada simple. Una evolución posterior puede incorporar transactional outbox, CDC y conciliación automática para eliminar la ventana entre la confirmación de la base de datos y la publicación del evento.

## Eventos Kafka

- `bancoxyz.transacciones.completadas`: se publica al aprobar una operación; `customer-service` actualiza la última actividad del cliente.
- `bancoxyz.alertas.seguridad`: se publica ante montos elevados, rechazos o fallos de compensación; `customer-service` persiste la alerta sin duplicarla.

Los dos tópicos tienen tres particiones. Los consumidores usan grupos separados para que Kafka distribuya el trabajo cuando se agregan instancias.

## Seguridad

El servidor de autorización usa `client_credentials`. Cada BFF exige su scope y propaga el token al backend. Los servicios internos vuelven a validar la firma JWT y permiten solamente los scopes asociados a sus operaciones. Las credenciales presentes en Compose se utilizan únicamente en desarrollo y pueden reemplazarse mediante variables de entorno.

## Observabilidad

Los servicios exponen salud, información, métricas y estado de circuit breakers mediante Actuator. Los logs se escriben en salida estándar, lo que permite recolectarlos desde Docker o una plataforma administrada como CloudWatch.

## Validación en contenedores

La ejecución integral confirmó:

1. Construcción de las nueve imágenes Java en arquitectura ARM64.
2. Inicio de PostgreSQL, Kafka y todos los servicios de soporte.
3. Registro de los tres BFF y los tres servicios de negocio en Eureka.
4. Emisión de tokens OAuth2 y rechazo de solicitudes sin credenciales.
5. Creación de un cliente y dos cuentas dentro de la red privada.
6. Transferencia aprobada a través de `bff-mobile`.
7. Reintento idempotente sin un segundo débito.
8. Publicación y consumo del evento de la transferencia.
9. Creación de los dos tópicos Kafka con tres particiones.
10. Funcionamiento simultáneo de dos instancias de clientes, cuentas y pagos, seguido de una nueva transferencia aprobada.
