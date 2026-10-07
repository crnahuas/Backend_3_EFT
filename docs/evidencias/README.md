# Evidencia de validación local

La carpeta `ultima-ejecucion` contiene la salida de la demostración reproducible realizada el 7 de octubre de 2026 mediante `scripts/demo-local.sh`.

## Resultado

- 35 pruebas automatizadas, sin fallos, errores ni casos omitidos.
- Construcción correcta de los diez módulos Maven.
- Seis aplicaciones registradas en Eureka.
- Dos instancias de `customer-service`, `account-service` y `payment-service`.
- Dos tópicos Kafka con tres particiones cada uno.
- Transferencia idempotente y retiro aprobados.
- Evento Kafka consumido y última actividad del cliente actualizada sin reiniciar servicios.
- Respuesta HTTP 401 al consultar una ruta protegida sin token.

## Archivos principales

- `resumen-ejecucion.json`: identificadores y resultado general de la prueba.
- `maven-test.log` y `maven-package.log`: salida completa de pruebas y empaquetado.
- `docker-compose-ps.txt`: contenedores activos durante la validación.
- `eureka-aplicaciones.txt`: aplicaciones registradas.
- `kafka-topicos.txt`: descripción de ambos tópicos.
- `transferencia-1.json` y `transferencia-2.json`: comprobación de idempotencia.
- `cliente-despues-evento.json`: comprobación del consumo Kafka.
- `dashboard-web.json`, `resumen-mobile.json` y `retiro-atm.json`: respuestas de los tres canales.

## Capturas visuales

La carpeta `imagenes` contiene cuatro láminas preparadas a partir de los resultados reales de la ejecución, sin inventar recursos cloud ni representar un despliegue AWS:

- `01-eureka-servicios.png`: seis aplicaciones registradas y dos instancias por microservicio de negocio.
- `02-pruebas-maven.png`: 35 pruebas, 20 suites y resultado Maven exitoso.
- `03-docker-kafka.png`: catorce contenedores activos y dos tópicos Kafka con tres particiones.
- `04-flujo-funcional.png`: transferencia y retiro idempotentes, respuesta 401 y consumo Kafka.

`capturas.html` permite revisar las cuatro evidencias en una sola vista local.

Los tokens OAuth2 no se guardan. Los clientes, cuentas, UUID y credenciales visibles corresponden exclusivamente a datos académicos generados para la demostración local.

Esta evidencia comprueba la ejecución local con Docker Compose. No representa recursos desplegados en AWS; `despliegue.md` describe únicamente la preparación y el procedimiento propuesto para ese entorno.
