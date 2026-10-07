# Checklist de cierre del proyecto

Estado revisado el 6 de octubre de 2026. El desarrollo funcional está implementado y probado. Los puntos restantes corresponden a la publicación, el informe y la presentación del sistema.

## Pendientes principales

### Publicación del código fuente

- [ ] Crear el repositorio remoto en GitHub.
- [ ] Configurar el remoto `origin` en este proyecto.
- [ ] Revisar que no se publiquen `.env`, certificados, contraseñas, bases locales ni carpetas `target`.
- [ ] Crear el primer commit con el código fuente completo.
- [ ] Subir la rama principal a GitHub.
- [ ] Agregar al `README.md` el enlace definitivo del repositorio.
- [ ] Confirmar que GitHub muestre correctamente el archivo `README.md` en la página principal.

### Informe técnico en PDF

- [ ] Obtener la plantilla definida para el informe técnico y usarla como documento base.
- [ ] Crear el informe técnico final en formato PDF.
- [ ] Incorporar portada, identificación del estudiante, asignatura y fecha.
- [ ] Presentar el resumen ejecutivo, objetivos y alcance.
- [ ] Explicar los cinco ejes de modernización: batch, microservicios, BFF, seguridad distribuida y Kafka.
- [ ] Explicar al menos tres requerimientos clave del negocio y justificar las decisiones arquitectónicas.
- [ ] Incluir un diagrama general de arquitectura.
- [ ] Incluir un diagrama de componentes o despliegue.
- [ ] Incluir casos de uso o secuencias para batch, transferencia y retiro.
- [ ] Documentar los tres procesos Spring Batch y sus resultados sobre los nueve CSV.
- [ ] Comparar resultados del sistema nuevo con el sistema legacy.
- [ ] Documentar los tres BFF y sus diferencias de payload, seguridad y comportamiento.
- [ ] Documentar clientes, cuentas y pagos, además de Config Server, Eureka y balanceo.
- [ ] Explicar OAuth2, scopes, propagación del token y HTTPS.
- [ ] Explicar circuit breakers, fallbacks, idempotencia y compensación distribuida.
- [ ] Explicar los productores, consumidores y tópicos Kafka.
- [ ] Incorporar evidencia del despliegue Docker Compose y de la escala a dos instancias.
- [ ] Incorporar la estrategia de despliegue AWS descrita en `despliegue.md`.
- [ ] Agregar capturas o resultados de pruebas, Eureka, Kafka y endpoints principales.
- [ ] Cerrar con desafíos, soluciones, limitaciones y próximos pasos.
- [ ] Revisar redacción, numeración, referencias y legibilidad del PDF final.

### Despliegue en AWS

- [ ] Crear o seleccionar la cuenta y región AWS donde se realizará la demostración.
- [ ] Publicar las imágenes de al menos clientes, cuentas y pagos en Amazon ECR.
- [ ] Crear los servicios de contenedores y conectarlos a una base PostgreSQL administrada.
- [ ] Configurar Kafka administrado o una alternativa compatible para los dos tópicos.
- [ ] Configurar HTTPS, secretos y acceso privado entre servicios.
- [ ] Activar logs y métricas centralizadas.
- [ ] Demostrar escalamiento horizontal con más de una instancia por servicio de negocio.
- [ ] Guardar capturas de la arquitectura desplegada y de una operación funcional.
- [ ] Documentar en `despliegue.md` cualquier diferencia entre el ambiente creado y el diseño propuesto.

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

- [ ] Ejecutar `mvn -B clean test` desde una copia limpia del repositorio.
- [ ] Ejecutar `mvn -B -DskipTests package`.
- [ ] Ejecutar `docker compose up -d --build` desde cero.
- [ ] Comprobar emisión de tokens para web, móvil y cajero.
- [ ] Comprobar respuesta `401` sin token y acceso correcto con cada scope.
- [ ] Ejecutar una transferencia y repetirla con la misma clave de idempotencia.
- [ ] Verificar registro de los seis servicios en Eureka.
- [ ] Verificar los dos tópicos Kafka y el consumo de una transacción.
- [ ] Confirmar que las instrucciones del `README.md` y `instrucciones.md` sean reproducibles.
- [ ] Detener el ambiente con `docker compose down`.
- [ ] Reunir el enlace de GitHub, el informe PDF y el video MP4 en una misma carpeta de publicación.

## Mejoras recomendadas antes de presentar

- [ ] Agregar más pruebas de seguridad, Kafka e integración con Testcontainers.
- [ ] Incorporar OpenAPI o una colección Postman para demostrar las API.
- [ ] Añadir una canalización de integración continua en GitHub Actions.
- [ ] Reemplazar las credenciales de desarrollo antes de cualquier despliegue público.
- [ ] Evaluar transactional outbox para eliminar la ventana entre base de datos y Kafka.
- [ ] Preparar datos de demostración controlados para que el video sea breve y repetible.
- [ ] Guardar capturas de la ejecución validada como respaldo para el informe y la presentación.

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
- [x] Veintitrés pruebas automatizadas exitosas.
- [x] Prueba de extremo a extremo con transferencia, reintento y evento Kafka.
