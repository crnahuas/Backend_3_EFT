# Certificados de desarrollo

Los archivos PKCS12 no se versionan. Para crear un certificado autofirmado local:

```bash
./scripts/generar-certificado-dev.sh
```

La contraseña predeterminada es `changeit` y puede cambiarse definiendo `SSL_KEY_STORE_PASSWORD` antes de ejecutar el script. En producción, el certificado debe provenir de AWS Certificate Manager o de la autoridad certificadora definida por el banco; no debe utilizarse este certificado local.
