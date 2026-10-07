# Checklist de cierre del proyecto

Estado revisado el 7 de octubre de 2026. El desarrollo funcional está implementado y probado. El informe final y las evidencias visuales ya fueron generados y revisados. Los puntos restantes corresponden a completar los datos administrativos de portada, preparar el video y reunir los archivos de entrega.

## Pendientes principales

### Publicación del código fuente

- [x] Crear el repositorio remoto en GitHub.
- [x] Configurar el remoto `origin` en este proyecto.
- [x] Revisar los archivos publicados: `.env` y las carpetas `target` están excluidos; las credenciales, el certificado y la base local incluidos corresponden exclusivamente al ambiente académico de demostración y no contienen datos reales.
- [x] Crear el primer commit con el código fuente completo.
- [x] Subir la rama principal a GitHub.
- [x] Agregar al `README.md` el enlace definitivo del repositorio.
- [x] Confirmar que GitHub muestre correctamente el archivo `README.md` en la página principal.

### Informe técnico en PDF

- [ ] Obtener la plantilla institucional `PBY2203_EFT_S9_plantilla_PDF` y trasladar el contenido si su uso literal es exigido; la plantilla no fue suministrada junto con las instrucciones y la pauta.
- [x] Crear el informe técnico final en formatos DOCX y PDF.
- [x] Incorporar portada, identificación del estudiante, asignatura y fecha.
- [ ] Completar en la portada el nombre del docente y la sección.
- [x] Presentar el resumen ejecutivo, objetivos y alcance en el borrador Markdown.
- [x] Explicar los cinco ejes de modernización: batch, microservicios, BFF, seguridad distribuida y Kafka.
- [x] Explicar al menos tres requerimientos clave del negocio y justificar las decisiones arquitectónicas.
- [x] Incluir un diagrama general de arquitectura.
- [x] Incluir un diagrama de componentes o despliegue.
- [x] Incluir casos de uso o secuencias para batch, transferencia y retiro.
- [x] Documentar los tres procesos Spring Batch y sus resultados sobre los nueve CSV.
- [x] Comparar resultados del sistema nuevo con el sistema legacy.
- [x] Documentar los tres BFF y sus diferencias de payload, seguridad y comportamiento.
- [x] Documentar clientes, cuentas y pagos, además de Config Server, Eureka y balanceo.
- [x] Explicar OAuth2, scopes, propagación del token y HTTPS.
- [x] Explicar circuit breakers, fallbacks, idempotencia y compensación distribuida.
- [x] Explicar los productores, consumidores y tópicos Kafka.
- [x] Incorporar evidencia textual del despliegue Docker Compose y de la escala a dos instancias.
- [x] Incorporar la preparación y estrategia de despliegue AWS descrita en `despliegue.md`.
- [x] Agregar resultados de pruebas, Eureka, Kafka y endpoints principales.
- [x] Cerrar con desafíos, soluciones, limitaciones y próximos pasos.
- [x] Revisar redacción, numeración, referencias y legibilidad de las 31 páginas del PDF final.
- [x] Incorporar cuatro evidencias visuales basadas en la ejecución real: pruebas Maven, Eureka, Docker/Kafka y flujo funcional.

### Preparación para el despliegue en AWS

El alcance del proyecto es local. Para AWS se solicita preparar los microservicios y explicar el proceso de despliegue, las herramientas y las configuraciones necesarias. No se requiere aprovisionar recursos, entregar una URL pública ni presentar capturas de una cuenta AWS.

- [x] Contenerizar los microservicios de clientes, cuentas y pagos mediante Docker.
- [x] Mantener configuración, URLs y credenciales parametrizables para cambiar de ambiente.
- [x] Comprobar escalamiento horizontal de los tres microservicios con dos instancias en Docker Compose.
- [x] Definir la arquitectura objetivo en AWS mediante ECR, ECS Fargate, ALB, RDS, MSK, Secrets Manager y CloudWatch.
- [x] Explicar en `despliegue.md` el procedimiento, las herramientas, las variables y las configuraciones necesarias.
- [x] Documentar red privada, HTTPS, administración de secretos, observabilidad, resiliencia y escalamiento.
- [x] Incorporar al informe un diagrama de la arquitectura AWS propuesta.
- [x] Explicar en el informe el proceso propuesto desde la construcción de imágenes hasta las pruebas posteriores al despliegue.
- [x] Diferenciar claramente la evidencia local ejecutada de la arquitectura cloud propuesta.

### Video de presentación

- [ ] Preparar un guion para una duración de 5 a 7 minutos.
- [ ] Preparar diapositivas o apoyo visual.
- [ ] Incluir un resumen ejecutivo del proyecto.
- [ ] Mostrar resultados y comparación con el sistema legacy.
- [ ] Explicar desafíos enfrentados y soluciones implementadas.
- [ ] Presentar propuestas de mejora y próximos pasos.
- [ ] Mostrar evidencia real del sistema funcionando.
- [ ] Grabar la presentación con la webcam visible.
- [ ] Exportar o descargar el video en formato MP4.
- [ ] Comprobar audio, imagen, duración y reproducción antes de entregar.

### Verificación final

- [ ] Ejecutar `mvn -B clean test` desde una copia limpia del repositorio. (El comando ya pasó localmente; falta repetirlo después de clonar el commit final.)
- [x] Ejecutar `mvn -B -DskipTests package`.
- [x] Ejecutar `docker compose up -d --build` desde cero.
- [x] Comprobar emisión de tokens para web, móvil y cajero.
- [x] Comprobar respuesta `401` sin token y acceso correcto con cada scope.
- [x] Ejecutar una transferencia y repetirla con la misma clave de idempotencia.
- [x] Verificar registro de los seis servicios en Eureka.
- [x] Verificar los dos tópicos Kafka y el consumo de una transacción.
- [x] Confirmar que las instrucciones del `README.md` y `instrucciones.md` sean reproducibles mediante `scripts/demo-local.sh`.
- [x] Detener el ambiente con `docker compose down` sin eliminar el volumen persistente.
- [ ] Reunir el enlace de GitHub, el informe PDF y el video MP4 en una misma carpeta de publicación.

## Mejoras recomendadas antes de presentar

- [x] Agregar pruebas de autorización por scopes, validación Kafka de extremo a extremo e integración PostgreSQL con Testcontainers.
- [x] Incorporar una colección Postman y un ambiente local para demostrar las API.
- [x] Añadir una canalización de integración continua en GitHub Actions.
- [x] Documentar que las credenciales académicas deben reemplazarse antes de cualquier uso real o despliegue público.
- [x] Evaluar transactional outbox para eliminar la ventana entre base de datos y Kafka.
- [x] Preparar datos de demostración controlados para que el video sea breve y repetible.
- [x] Guardar resultados de la ejecución validada en `docs/evidencias/ultima-ejecucion` como respaldo verificable.

## Elementos ya completados

- [x] Tres procesos Spring Batch sobre los archivos legacy.
- [x] Tres BFF independientes para web, móvil y cajero.
- [x] Microservicios de clientes, cuentas y pagos.
- [x] Spring Cloud Config, Eureka y balanceo de carga.
- [x] OAuth2/JWT y autorización por scopes.
- [x] Resilience4j, fallbacks e idempotencia.
- [x] Productores y consumidores Kafka.
- [x] Docker Compose y escalabilidad horizontal comprobada.
- [x] Plan documentado para AWS.
- [x] Treinta y cinco pruebas automatizadas exitosas.
- [x] Prueba de extremo a extremo con transferencia, reintento y evento Kafka.
