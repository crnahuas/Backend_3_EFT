#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CERT_DIR="$PROJECT_DIR/infra/certs"
KEYSTORE="$CERT_DIR/bancoxyz-dev.p12"
PASSWORD="${SSL_KEY_STORE_PASSWORD:-changeit}"

mkdir -p "$CERT_DIR"

if [[ -f "$KEYSTORE" ]]; then
  echo "El certificado ya existe en $KEYSTORE"
  exit 0
fi

keytool -genkeypair \
  -alias bancoxyz-dev \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE" \
  -storepass "$PASSWORD" \
  -validity 3650 \
  -dname "CN=localhost, OU=Desarrollo, O=Banco XYZ, L=Santiago, ST=RM, C=CL" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"

echo "Certificado de desarrollo creado en $KEYSTORE"
