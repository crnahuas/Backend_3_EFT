#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
EVIDENCE_DIR="${EVIDENCE_DIR:-$PROJECT_DIR/docs/evidencias/ultima-ejecucion}"
COMPOSE_NETWORK="${COMPOSE_NETWORK:-bancoxyz-eft_bancoxyz}"
CURL_IMAGE="${CURL_IMAGE:-curlimages/curl:8.12.1}"
DEMO_RUT="${DEMO_RUT:-$(date +%s)-9}"
DEMO_EMAIL="${DEMO_EMAIL:-demo.$(date +%s)@bancoxyz.cl}"
CURL_RETRY=(--retry 30 --retry-delay 2 --retry-all-errors)

for command in mvn docker curl jq; do
  if ! command -v "$command" >/dev/null 2>&1; then
    echo "Falta el comando requerido: $command" >&2
    exit 1
  fi
done

mkdir -p "$EVIDENCE_DIR"
cd "$PROJECT_DIR"

if [[ "${SKIP_BUILD:-false}" == "true" ]]; then
  echo "1/8 Pruebas omitidas por SKIP_BUILD=true"
  echo "2/8 Empaquetado omitido por SKIP_BUILD=true"
  BUILD_OPTION=(--no-build)
else
  echo "1/8 Ejecutando pruebas automatizadas"
  mvn -B clean test | tee "$EVIDENCE_DIR/maven-test.log"

  echo "2/8 Empaquetando módulos"
  mvn -B -DskipTests package | tee "$EVIDENCE_DIR/maven-package.log"
  BUILD_OPTION=(--build)
fi

echo "3/8 Iniciando y escalando la plataforma"
docker compose up -d "${BUILD_OPTION[@]}" \
  --scale customer-service=2 \
  --scale account-service=2 \
  --scale payment-service=2

wait_for_url() {
  local url="$1"
  local attempts=60
  until curl -fsS "$url" >/dev/null; do
    attempts=$((attempts - 1))
    if [[ "$attempts" -eq 0 ]]; then
      echo "El endpoint no quedó disponible: $url" >&2
      exit 1
    fi
    sleep 2
  done
}

for url in \
  http://localhost:8888/actuator/health \
  http://localhost:8761/actuator/health \
  http://localhost:8081/actuator/health \
  http://localhost:8082/actuator/health \
  http://localhost:8083/actuator/health; do
  wait_for_url "$url"
done

wait_for_eureka() {
  local attempts=60
  local registry
  until registry="$(curl -fsS -H 'Accept: application/json' http://localhost:8761/eureka/apps)" \
    && [[ "$(jq '[.applications.application[] | select(.name == "BFF-WEB" or .name == "BFF-MOBILE" or .name == "BFF-ATM" or .name == "CUSTOMER-SERVICE" or .name == "ACCOUNT-SERVICE" or .name == "PAYMENT-SERVICE")] | length' <<<"$registry")" -eq 6 ]] \
    && [[ "$(jq '[.applications.application[] | select(.name == "CUSTOMER-SERVICE") | .instance[]] | length' <<<"$registry")" -ge 2 ]] \
    && [[ "$(jq '[.applications.application[] | select(.name == "ACCOUNT-SERVICE") | .instance[]] | length' <<<"$registry")" -ge 2 ]] \
    && [[ "$(jq '[.applications.application[] | select(.name == "PAYMENT-SERVICE") | .instance[]] | length' <<<"$registry")" -ge 2 ]]; do
    attempts=$((attempts - 1))
    if [[ "$attempts" -eq 0 ]]; then
      echo "Eureka no registró las seis aplicaciones y sus réplicas dentro del tiempo esperado" >&2
      exit 1
    fi
    sleep 2
  done
}

wait_for_eureka

echo "4/8 Obteniendo tokens por canal"
TOKEN_WEB="$(curl -fsS -u bff-web:web-secret -d 'grant_type=client_credentials&scope=web' http://localhost:9000/oauth2/token | jq -er .access_token)"
TOKEN_MOBILE="$(curl -fsS -u bff-mobile:mobile-secret -d 'grant_type=client_credentials&scope=mobile' http://localhost:9000/oauth2/token | jq -er .access_token)"
TOKEN_ATM="$(curl -fsS -u bff-atm:atm-secret -d 'grant_type=client_credentials&scope=atm.read%20atm.withdraw' http://localhost:9000/oauth2/token | jq -er .access_token)"
TOKEN_INTERNAL="$(curl -fsS -u internal-services:internal-secret -d 'grant_type=client_credentials&scope=internal' http://localhost:9000/oauth2/token | jq -er .access_token)"

internal_request() {
  docker run --rm --network "$COMPOSE_NETWORK" "$CURL_IMAGE" -fsS "$@"
}

echo "5/8 Creando datos controlados"
internal_request -X POST \
  -H "Authorization: Bearer $TOKEN_INTERNAL" \
  -H 'Content-Type: application/json' \
  -d "{\"rut\":\"$DEMO_RUT\",\"nombres\":\"Cliente\",\"apellidos\":\"Demostracion\",\"email\":\"$DEMO_EMAIL\",\"telefono\":\"+56911111111\"}" \
  http://customer-service:8091/api/clientes | tee "$EVIDENCE_DIR/cliente.json" >/dev/null

internal_request -X POST \
  -H "Authorization: Bearer $TOKEN_INTERNAL" \
  -H 'Content-Type: application/json' \
  -d "{\"rutCliente\":\"$DEMO_RUT\",\"tipo\":\"VISTA\",\"saldoInicial\":100000,\"moneda\":\"CLP\"}" \
  http://account-service:8092/api/cuentas | tee "$EVIDENCE_DIR/cuenta-origen.json" >/dev/null

internal_request -X POST \
  -H "Authorization: Bearer $TOKEN_INTERNAL" \
  -H 'Content-Type: application/json' \
  -d "{\"rutCliente\":\"$DEMO_RUT\",\"tipo\":\"AHORRO\",\"saldoInicial\":50000,\"moneda\":\"CLP\"}" \
  http://account-service:8092/api/cuentas | tee "$EVIDENCE_DIR/cuenta-destino.json" >/dev/null

CUENTA_ORIGEN="$(jq -er .numeroCuenta "$EVIDENCE_DIR/cuenta-origen.json")"
CUENTA_DESTINO="$(jq -er .numeroCuenta "$EVIDENCE_DIR/cuenta-destino.json")"
TRANSFERENCIA_KEY="demo-transferencia-$(date +%s)"
RETIRO_KEY="demo-retiro-$(date +%s)"

echo "6/8 Probando BFF e idempotencia"
curl "${CURL_RETRY[@]}" -fsS -H "Authorization: Bearer $TOKEN_MOBILE" \
  "http://localhost:8082/api/mobile/clientes/$DEMO_RUT/resumen" \
  | tee "$EVIDENCE_DIR/resumen-mobile.json" >/dev/null

TRANSFER_BODY="{\"cuentaOrigen\":\"$CUENTA_ORIGEN\",\"cuentaDestino\":\"$CUENTA_DESTINO\",\"monto\":10000,\"descripcion\":\"Demostracion reproducible\"}"
for intento in 1 2; do
  curl "${CURL_RETRY[@]}" -fsS -X POST \
    -H "Authorization: Bearer $TOKEN_MOBILE" \
    -H "X-Idempotency-Key: $TRANSFERENCIA_KEY" \
    -H 'Content-Type: application/json' \
    -d "$TRANSFER_BODY" \
    http://localhost:8082/api/mobile/transferencias \
    | tee "$EVIDENCE_DIR/transferencia-$intento.json" >/dev/null
done

ID_1="$(jq -er .id "$EVIDENCE_DIR/transferencia-1.json")"
ID_2="$(jq -er .id "$EVIDENCE_DIR/transferencia-2.json")"
[[ "$ID_1" == "$ID_2" ]] || { echo "Falló la idempotencia de transferencia" >&2; exit 1; }

curl "${CURL_RETRY[@]}" -fsS -H "Authorization: Bearer $TOKEN_WEB" \
  "http://localhost:8081/api/web/clientes/$DEMO_RUT/dashboard" \
  | tee "$EVIDENCE_DIR/dashboard-web.json" >/dev/null

curl "${CURL_RETRY[@]}" -fsS -X POST \
  -H "Authorization: Bearer $TOKEN_ATM" \
  -H "X-Idempotency-Key: $RETIRO_KEY" \
  -H 'Content-Type: application/json' \
  -d "{\"numeroCuenta\":\"$CUENTA_ORIGEN\",\"monto\":20000}" \
  http://localhost:8083/api/cajero/retiros \
  | tee "$EVIDENCE_DIR/retiro-atm.json" >/dev/null

echo "7/8 Verificando seguridad, Eureka y Kafka"
HTTP_SIN_TOKEN="$(curl -sS -o /dev/null -w '%{http_code}' "http://localhost:8082/api/mobile/clientes/$DEMO_RUT/resumen")"
[[ "$HTTP_SIN_TOKEN" == "401" ]] || { echo "Se esperaba HTTP 401 sin token y se obtuvo $HTTP_SIN_TOKEN" >&2; exit 1; }

wait_for_kafka_consumption() {
  local attempts=30
  local cliente
  until cliente="$(internal_request -H "Authorization: Bearer $TOKEN_INTERNAL" "http://customer-service:8091/api/clientes/$DEMO_RUT")" \
    && [[ "$(jq -r '.ultimaActividad // empty' <<<"$cliente")" != "" ]]; do
    attempts=$((attempts - 1))
    if [[ "$attempts" -eq 0 ]]; then
      echo "El evento Kafka no actualizó la actividad del cliente dentro del tiempo esperado" >&2
      exit 1
    fi
    sleep 1
  done
  jq . <<<"$cliente" > "$EVIDENCE_DIR/cliente-despues-evento.json"
}

wait_for_kafka_consumption
ULTIMA_ACTIVIDAD="$(jq -er .ultimaActividad "$EVIDENCE_DIR/cliente-despues-evento.json")"

curl -fsS -H 'Accept: application/json' http://localhost:8761/eureka/apps \
  | jq -r '.applications.application[].name' | sort -u \
  | tee "$EVIDENCE_DIR/eureka-aplicaciones.txt" >/dev/null

for topic in bancoxyz.transacciones.completadas bancoxyz.alertas.seguridad; do
  docker compose exec -T kafka /opt/kafka/bin/kafka-topics.sh \
    --bootstrap-server localhost:9092 --describe --topic "$topic"
done | tee "$EVIDENCE_DIR/kafka-topicos.txt" >/dev/null

docker compose ps | tee "$EVIDENCE_DIR/docker-compose-ps.txt" >/dev/null

echo "8/8 Guardando resumen"
jq -n \
  --arg fecha "$(date -Iseconds)" \
  --arg rut "$DEMO_RUT" \
  --arg cuentaOrigen "$CUENTA_ORIGEN" \
  --arg cuentaDestino "$CUENTA_DESTINO" \
  --arg transferenciaId "$ID_1" \
  --arg ultimaActividad "$ULTIMA_ACTIVIDAD" \
  --arg httpSinToken "$HTTP_SIN_TOKEN" \
  '{fecha:$fecha,rut:$rut,cuentaOrigen:$cuentaOrigen,cuentaDestino:$cuentaDestino,transferenciaId:$transferenciaId,ultimaActividad:$ultimaActividad,httpSinToken:$httpSinToken,resultado:"APROBADO"}' \
  > "$EVIDENCE_DIR/resumen-ejecucion.json"

echo "Demostración completada. Evidencia: $EVIDENCE_DIR"
echo "Postman: use rut_demo=$DEMO_RUT, cuenta_origen=$CUENTA_ORIGEN y cuenta_destino=$CUENTA_DESTINO"
