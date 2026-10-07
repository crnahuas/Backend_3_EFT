# Instrucciones de ejecución y prueba

## Requisitos

- Java 17 o superior.
- Maven 3.9 o superior.
- Docker Desktop con Compose v2.
- `curl` y `jq` para ejecutar los ejemplos.
- Puertos 8081, 8082, 8083, 8761, 8888, 9000 y 29092 disponibles.

## Compilar y probar

Desde la raíz del proyecto:

```bash
mvn -B clean test
mvn -B -DskipTests package
```

El primer comando ejecuta 35 pruebas. Estas incluyen reglas de negocio, autorización por scopes en los tres BFF y una integración real con PostgreSQL mediante Testcontainers. El segundo comando genera los JAR que copian los Dockerfiles.

## Demostración reproducible

Para compilar, levantar la plataforma con dos instancias de cada microservicio de negocio y ejecutar automáticamente el flujo de autenticación, transferencia idempotente, consumo Kafka, dashboard, retiro y rechazo sin token:

```bash
./scripts/demo-local.sh
```

El script guarda respuestas y comprobaciones en `docs/evidencias/ultima-ejecucion`. Si los artefactos ya fueron compilados, puede omitirse la compilación con `SKIP_BUILD=true ./scripts/demo-local.sh`.

Como alternativa manual, la colección `postman/Banco-XYZ-EFT.postman_collection.json` puede importarse junto con `postman/Local.postman_environment.json`.

## Iniciar toda la plataforma

```bash
docker compose up -d --build
docker compose ps
```

La primera ejecución descarga PostgreSQL, Kafka y la imagen Java. El servicio de inicialización `kafka-init` crea los dos tópicos con tres particiones antes de iniciar los productores y consumidores. Espere hasta que los endpoints respondan:

```bash
curl http://localhost:8888/actuator/health
curl http://localhost:8761/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

El endpoint de salud del servidor de autorización está protegido por su cadena de seguridad. Su disponibilidad se comprueba solicitando un token.

## Obtener tokens OAuth2

```bash
TOKEN_WEB=$(curl -sS -u bff-web:web-secret \
  -d 'grant_type=client_credentials&scope=web' \
  http://localhost:9000/oauth2/token | jq -r .access_token)

TOKEN_MOBILE=$(curl -sS -u bff-mobile:mobile-secret \
  -d 'grant_type=client_credentials&scope=mobile' \
  http://localhost:9000/oauth2/token | jq -r .access_token)

TOKEN_ATM=$(curl -sS -u bff-atm:atm-secret \
  -d 'grant_type=client_credentials&scope=atm.read%20atm.withdraw' \
  http://localhost:9000/oauth2/token | jq -r .access_token)

TOKEN_INTERNAL=$(curl -sS -u internal-services:internal-secret \
  -d 'grant_type=client_credentials&scope=internal' \
  http://localhost:9000/oauth2/token | jq -r .access_token)
```

Las credenciales son solo valores de desarrollo. En AWS deben reemplazarse mediante Secrets Manager.

## Preparar datos de prueba

Los servicios de negocio no exponen puertos al host para permitir escalamiento sin conflictos. Los siguientes comandos usan una imagen temporal de `curl` dentro de la red de Compose.

Crear un cliente:

```bash
docker run --rm --network bancoxyz-eft_bancoxyz \
  curlimages/curl:8.12.1 -sS -X POST \
  -H "Authorization: Bearer $TOKEN_INTERNAL" \
  -H 'Content-Type: application/json' \
  -d '{"rut":"11111111-1","nombres":"Ana","apellidos":"Prueba","email":"ana.prueba@bancoxyz.cl","telefono":"+56911111111"}' \
  http://customer-service:8091/api/clientes
```

Crear dos cuentas y guardar sus números:

```bash
CUENTA_ORIGEN=$(docker run --rm --network bancoxyz-eft_bancoxyz \
  curlimages/curl:8.12.1 -sS -X POST \
  -H "Authorization: Bearer $TOKEN_INTERNAL" \
  -H 'Content-Type: application/json' \
  -d '{"rutCliente":"11111111-1","tipo":"VISTA","saldoInicial":100000,"moneda":"CLP"}' \
  http://account-service:8092/api/cuentas | jq -r .numeroCuenta)

CUENTA_DESTINO=$(docker run --rm --network bancoxyz-eft_bancoxyz \
  curlimages/curl:8.12.1 -sS -X POST \
  -H "Authorization: Bearer $TOKEN_INTERNAL" \
  -H 'Content-Type: application/json' \
  -d '{"rutCliente":"11111111-1","tipo":"AHORRO","saldoInicial":50000,"moneda":"CLP"}' \
  http://account-service:8092/api/cuentas | jq -r .numeroCuenta)
```

## Probar los BFF

Resumen móvil:

```bash
curl -H "Authorization: Bearer $TOKEN_MOBILE" \
  http://localhost:8082/api/mobile/clientes/11111111-1/resumen
```

Transferencia móvil:

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN_MOBILE" \
  -H 'X-Idempotency-Key: transferencia-001' \
  -H 'Content-Type: application/json' \
  -d "{\"cuentaOrigen\":\"$CUENTA_ORIGEN\",\"cuentaDestino\":\"$CUENTA_DESTINO\",\"monto\":10000,\"descripcion\":\"Prueba de integración\"}" \
  http://localhost:8082/api/mobile/transferencias
```

Repita el mismo comando con la misma clave. Debe devolver el mismo identificador y no debitar nuevamente.

Dashboard web:

```bash
curl -H "Authorization: Bearer $TOKEN_WEB" \
  http://localhost:8081/api/web/clientes/11111111-1/dashboard
```

Saldo y retiro de cajero:

```bash
curl -H "Authorization: Bearer $TOKEN_ATM" \
  "http://localhost:8083/api/cajero/cuentas/$CUENTA_ORIGEN/saldo"

curl -X POST \
  -H "Authorization: Bearer $TOKEN_ATM" \
  -H 'X-Idempotency-Key: retiro-001' \
  -H 'Content-Type: application/json' \
  -d "{\"numeroCuenta\":\"$CUENTA_ORIGEN\",\"monto\":20000}" \
  http://localhost:8083/api/cajero/retiros
```

Una ruta de negocio sin token debe responder `401`:

```bash
curl -i http://localhost:8082/api/mobile/clientes/11111111-1/resumen
```

## Verificar Eureka, Config y Kafka

```bash
curl -H 'Accept: application/json' http://localhost:8761/eureka/apps | jq -r '.applications.application[].name'
curl http://localhost:8888/account-service/default

docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --describe \
  --topic bancoxyz.transacciones.completadas
```

Se esperan seis aplicaciones registradas: los tres BFF y los tres servicios de negocio. Cada tópico Kafka tiene tres particiones.

## Escalar servicios

```bash
docker compose up -d \
  --scale customer-service=2 \
  --scale account-service=2 \
  --scale payment-service=2
```

Verifique las instancias en `http://localhost:8761` o en `/eureka/apps`.

## Logs y métricas

```bash
docker compose logs -f payment-service account-service customer-service
curl http://localhost:8082/actuator/metrics
curl http://localhost:8082/actuator/health
```

## Detener o reiniciar

Conservar la base de datos:

```bash
docker compose down
```

Eliminar también el volumen de desarrollo y comenzar desde cero:

```bash
docker compose down -v
```

El segundo comando elimina los datos de PostgreSQL y debe usarse solo cuando se desea reiniciar el ambiente.

## Ejecutar Batch de forma independiente

```bash
mvn -pl batch-service spring-boot:run
curl -X POST http://localhost:8086/api/batch/jobs/movimientos-diarios
curl -X POST http://localhost:8086/api/batch/jobs/intereses-mensuales
curl -X POST http://localhost:8086/api/batch/jobs/estados-financieros-anuales
curl http://localhost:8086/api/batch/executions/1
```

Si una ejecución termina con estado `FAILED`, el servicio la detecta cada cinco segundos y la reinicia hasta dos veces. Estos valores se pueden ajustar con `app.batch.recovery.poll-ms` y `app.batch.recovery.max-restarts`.
