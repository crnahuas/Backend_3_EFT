# Guion para el video de presentación final

**Proyecto:** Modernización de la plataforma Banco XYZ

**Estudiante:** Cristian Nahuas

**Duración objetivo:** 6 minutos y 35 segundos

**Formato de entrega:** MP4
**Presentación:** individual, con webcam visible y evidencia del trabajo en pantalla

## 1. Objetivo del video

El video debe demostrar, de manera breve y profesional, que la solución fue desarrollada, probada y documentada. Debe cubrir expresamente estos cuatro puntos:

1. Resumen ejecutivo del proyecto.
2. Resultados obtenidos y comparación con el sistema legacy.
3. Desafíos enfrentados y soluciones implementadas.
4. Propuestas de mejora y próximos pasos.

La grabación propuesta dura aproximadamente 6 minutos y 35 segundos, por lo que queda dentro del límite de 5 a 7 minutos. La webcam debe mantenerse visible en una esquina durante toda la exposición, sin cubrir tablas, diagramas ni resultados.

## 2. Material que debe estar preparado antes de grabar

Abrir los siguientes archivos en el orden indicado, para cambiar rápidamente entre ellos durante la presentación:

| Orden | Archivo o recurso | Uso en el video |
|---:|---|---|
| 1 | `../output/Informe_Tecnico_Banco_XYZ_Final.pdf` | Portada y presentación del proyecto |
| 2 | `imagenes/01-arquitectura-general.png` | Resumen de la arquitectura completa |
| 3 | `imagenes/08-evidencia-pruebas-maven.png` | Pruebas automatizadas y compilación |
| 4 | `imagenes/09-evidencia-eureka-servicios.png` | Descubrimiento y escalamiento horizontal |
| 5 | `imagenes/10-evidencia-docker-kafka.png` | Contenedores activos y tópicos Kafka |
| 6 | `imagenes/11-evidencia-flujo-funcional.png` | Seguridad, transferencia, retiro e idempotencia |
| 7 | `imagenes/07-arquitectura-aws.png` | Arquitectura AWS propuesta como próximo paso |
| 8 | Repositorio `https://github.com/crnahuas/Backend_3_EFT` | Cierre y comprobación de documentación |

Para presentar las evidencias en el navegador sin levantar los microservicios, ejecutar antes de grabar:

```bash
python3 -m http.server 8177 --directory docs/evidencias
```

Luego abrir estas direcciones:

```text
http://localhost:8177/capturas.html?vista=pruebas
http://localhost:8177/imagenes/01-eureka-servicios.png
http://localhost:8177/capturas.html?vista=infraestructura
http://localhost:8177/capturas.html?vista=flujo
```

Estas vistas utilizan resultados guardados de una ejecución real. No es necesario ejecutar la compilación completa durante el video. Si se desea realizar una demostración en vivo, se recomienda ejecutar `./scripts/demo-local.sh` antes de iniciar la grabación y no durante ella, porque la construcción de imágenes puede consumir una parte importante de los siete minutos disponibles.

## 3. Guion cronometrado y narración sugerida

### 0:00–0:25 — Presentación

**Mostrar:** portada del informe técnico. Webcam visible en la esquina superior derecha.

**Narración:**

> Hola, mi nombre es Cristian Nahuas. En esta presentación mostraré la modernización de la plataforma del Banco XYZ, desarrollada para la evaluación final de Backend III. Explicaré el objetivo y alcance de la solución, los resultados obtenidos y su comparación con el sistema legacy, los principales desafíos técnicos y las mejoras propuestas.

**Acción en pantalla:** señalar brevemente el nombre del proyecto y pasar al diagrama de arquitectura.

### 0:25–1:30 — Resumen ejecutivo, objetivos y alcance

**Mostrar:** `imagenes/01-arquitectura-general.png`.

**Narración:**

> El objetivo fue transformar procesos financieros legacy en una plataforma backend modular, segura, escalable y reproducible. La solución se divide en diez módulos Maven. Incluye un servicio Spring Batch para procesar información histórica, tres microservicios de negocio para clientes, cuentas y pagos, y tres Backend for Frontend especializados para web, aplicación móvil y cajero automático.
>
> La plataforma incorpora Config Server para centralizar propiedades, Eureka para registrar y descubrir instancias, OAuth2 y JWT para proteger los servicios, Resilience4j para controlar fallas remotas y Kafka para desacoplar las actividades posteriores a una transacción. PostgreSQL mantiene la información operacional separada por dominio y Docker Compose permite reproducir y escalar el entorno local.
>
> El alcance ejecutado es local y académico. Los microservicios quedaron contenerizados y parametrizados para una futura migración a AWS, pero no se aprovisionaron recursos reales en una cuenta cloud. El proceso de despliegue está documentado como una estrategia propuesta.

**Puntos que conviene señalar con el cursor:** los tres BFF, los tres microservicios, Kafka, PostgreSQL, Config Server, Eureka y el servidor de autorización.

### 1:30–2:35 — Resultados y comparación con el sistema legacy

**Mostrar primero:** página 11 o 12 del informe, o la sección `7.6 Resultados consolidados`.

**Mostrar después:** `imagenes/08-evidencia-pruebas-maven.png`.

**Narración:**

> El procesamiento batch utiliza los nueve archivos CSV distribuidos en tres semanas. En total se almacenaron 3.054 resultados: 1.020 movimientos diarios, 1.016 registros de intereses y 1.018 operaciones para estados financieros. De ellos, 946 fueron válidos y 2.108 quedaron registrados como anomalías revisables. También se generaron 339 resúmenes diarios y 20 resúmenes anuales por cuenta.
>
> En el tratamiento legacy, los archivos estaban separados por semana, presentaban fechas y montos inconsistentes, y existía riesgo de descartar información defectuosa o duplicar resultados durante una reejecución. La solución moderna aplica reglas explícitas, conserva las anomalías con su causa, escribe en chunks de 100 registros, procesa hasta cuatro particiones en paralelo y permite reintentos y reinicio automático. Una segunda ejecución mantiene los mismos conteos y no duplica información.
>
> La validación automatizada ejecutó 35 pruebas en 20 suites, sin fallos, errores ni casos omitidos. También se empaquetaron correctamente los diez módulos Maven.

**Destacar en pantalla:** los indicadores `35`, `20`, `0` y `SUCCESS`.

### 2:35–3:35 — Evidencia de arquitectura, escalamiento y mensajería

**Mostrar primero:** `imagenes/09-evidencia-eureka-servicios.png`.

**Mostrar después:** `imagenes/10-evidencia-docker-kafka.png`.

**Narración:**

> La solución completa fue levantada mediante Docker Compose. Eureka registró seis aplicaciones: los tres BFF y los microservicios de clientes, cuentas y pagos. Para comprobar escalabilidad horizontal, cada microservicio de negocio se ejecutó con dos instancias. En total se mantuvieron catorce contenedores activos, además del inicializador Kafka de ejecución única.
>
> Los clientes internos llaman a los servicios por nombre y Spring Cloud LoadBalancer selecciona una instancia disponible. Esto evita depender de direcciones fijas y permite agregar réplicas sin modificar el código de los consumidores.
>
> Kafka utiliza dos tópicos: uno para transacciones completadas y otro para alertas de seguridad. Cada tópico fue creado con tres particiones antes de iniciar productores y consumidores. Una transacción aprobada actualiza de forma asíncrona la última actividad del cliente.

**Destacar en pantalla:** las dos instancias de cada microservicio, los 14 contenedores, los 2 tópicos y las 3 particiones.

### 3:35–4:35 — Seguridad y flujo funcional validado

**Mostrar:** `imagenes/11-evidencia-flujo-funcional.png`.

**Narración:**

> Cada canal obtiene un token OAuth2 mediante el flujo client credentials y recibe scopes específicos. Web utiliza el scope `web`, móvil utiliza `mobile`, y el cajero separa la consulta y el retiro mediante `atm.read` y `atm.withdraw`. Una solicitud sin token respondió HTTP 401, confirmando que las rutas de negocio están protegidas.
>
> La prueba funcional creó un cliente y dos cuentas. Después ejecutó una transferencia de 10.000 pesos por el BFF móvil. Al repetir la solicitud con la misma clave de idempotencia, el servicio devolvió el mismo identificador y no realizó un segundo débito. También se aprobó un retiro de 20.000 pesos, cuyo reintento mantuvo el mismo identificador y saldo final. Finalmente, el evento Kafka fue consumido y actualizó la actividad del cliente sin reiniciar servicios.

**Destacar en pantalla:** `APROBADO`, `401`, `MISMO ID` y `CONSUMIDO`.

### 4:35–5:35 — Desafíos y soluciones implementadas

**Mostrar:** mantener la evidencia funcional o abrir las secciones 18 y 19 del informe.

**Narración:**

> Durante el desarrollo aparecieron cuatro desafíos principales. Primero, los datos legacy tenían formatos inconsistentes. Esto se resolvió con parsers tolerantes, validaciones por tipo de registro y una clasificación que conserva las anomalías en lugar de eliminarlas.
>
> Segundo, los reintentos de red podían duplicar débitos. La solución fue persistir claves de idempotencia únicas y devolver el resultado existente cuando llega la misma solicitud.
>
> Tercero, una transferencia distribuida podía fallar después de debitar la cuenta de origen. Se aplicó bloqueo pesimista para proteger los saldos y una compensación que devuelve el monto al origen si el crédito de destino no puede completarse.
>
> Finalmente, en una ejecución inicial los consumidores Kafka podían comenzar antes de que los tópicos tuvieran sus tres particiones. Se incorporó un contenedor `kafka-init`, y los servicios de clientes y pagos esperan su finalización. La ejecución posterior confirmó el consumo correcto sin reinicios manuales.

**Consejo:** en esta sección hablar con ritmo pausado; es el punto donde se demuestra la toma de decisiones técnicas.

### 5:35–6:15 — Mejoras, próximos pasos y preparación para AWS

**Mostrar:** `imagenes/07-arquitectura-aws.png`.

**Narración:**

> Como próximos pasos, la prioridad sería incorporar transactional outbox para garantizar la entrega de eventos entre PostgreSQL y Kafka, utilizar Flyway o Liquibase para versionar los esquemas, ampliar las pruebas de contrato, concurrencia y rendimiento, y añadir trazas distribuidas mediante OpenTelemetry.
>
> Para AWS, la arquitectura propuesta publica las imágenes en Amazon ECR y ejecuta los servicios en ECS Fargate. Un Application Load Balancer expondría solamente los BFF y autorización mediante HTTPS. RDS PostgreSQL aportaría persistencia administrada, Amazon MSK reemplazaría al broker local, Secrets Manager administraría credenciales y CloudWatch centralizaría logs y métricas. Esta imagen representa el diseño objetivo y el procedimiento documentado; no corresponde a un despliegue AWS ejecutado en esta entrega.

**Puntos que conviene señalar:** ECR, ECS Fargate, ALB, RDS, MSK, Secrets Manager y CloudWatch.

### 6:15–6:35 — Cierre

**Mostrar:** página principal del repositorio o portada del informe.

**Narración:**

> En conclusión, el proyecto demuestra una modernización funcional y reproducible: procesa los datos legacy con trazabilidad, separa responsabilidades en microservicios y BFF, protege las operaciones, evita duplicaciones, tolera fallas controladas y utiliza mensajería asíncrona. El código, las instrucciones de ejecución, el informe técnico, la evidencia y el plan de despliegue se encuentran documentados en el repositorio. Muchas gracias.

## 4. Versión abreviada por si el video supera los siete minutos

Si la primera grabación dura más de siete minutos, aplicar estos recortes sin eliminar ninguno de los cuatro puntos obligatorios:

1. En el resumen ejecutivo, mencionar los componentes sin describir individualmente Config Server y Eureka.
2. En los resultados batch, omitir el desglose de válidos y anomalías, pero conservar el total de 3.054 resultados.
3. En la evidencia funcional, explicar la transferencia idempotente y resumir el retiro en una sola frase.
4. En AWS, enumerar los servicios en una sola oración y conservar la aclaración de que no hubo despliegue real.

No eliminar la comparación con legacy, los desafíos, las soluciones ni los próximos pasos, porque corresponden a contenidos expresamente solicitados.

## 5. Plan de respaldo si falla la demostración en vivo

No conviene depender exclusivamente de servicios ejecutándose durante la grabación. Si Docker, Eureka o alguna API no responde:

- continuar con las cuatro imágenes de evidencia, porque fueron generadas a partir de la ejecución validada;
- mostrar `docs/evidencias/ultima-ejecucion/resumen-ejecucion.json` para respaldar identificadores y resultado;
- mostrar `docs/evidencias/ultima-ejecucion/maven-test.log` para respaldar las 35 pruebas;
- evitar reiniciar o recompilar durante el video;
- no sustituir la evidencia local por una captura ficticia de AWS.

## 6. Lista de comprobación antes de grabar

- [ ] Cerrar correo, mensajería y ventanas con información personal.
- [ ] Desactivar notificaciones del sistema.
- [ ] Conectar el equipo a corriente y comprobar espacio disponible.
- [ ] Usar audífonos o micrófono y realizar una prueba de audio de 20 segundos.
- [ ] Iluminar el rostro desde el frente y colocar la cámara a la altura de los ojos.
- [ ] Configurar grabación en 1080p cuando el equipo lo permita.
- [ ] Mantener la webcam visible sin cubrir el contenido técnico.
- [ ] Abrir previamente los siete recursos visuales en el orden del guion.
- [ ] Aumentar el zoom hasta que tablas y textos sean legibles.
- [ ] Ocultar la barra de favoritos y pestañas que no pertenezcan al proyecto.
- [ ] Tener un cronómetro visible fuera del área grabada.
- [ ] Ensayar una vez buscando una duración entre 6:00 y 6:40.
- [ ] Pronunciar pausadamente los conceptos, sin leer rutas o UUID completos.

## 7. Lista de comprobación después de grabar

- [ ] Confirmar duración entre 5:00 y 7:00 minutos.
- [ ] Confirmar que el archivo final sea MP4.
- [ ] Verificar que la webcam sea visible y que el rostro no quede cortado.
- [ ] Escuchar el video completo con audífonos y confirmar volumen estable.
- [ ] Comprobar que las evidencias se lean y no estén borrosas.
- [ ] Confirmar que se expliquen los cuatro contenidos obligatorios.
- [ ] Confirmar que no se afirme que AWS fue desplegado realmente.
- [ ] Revisar que no existan silencios largos, notificaciones o errores visibles.
- [ ] Usar un nombre claro, por ejemplo `PBY2203_EFT_S9_Cristian_Nahuas.mp4`.
- [ ] Reproducir el MP4 desde el inicio hasta el final antes de incorporarlo a la carpeta de entrega.

## 8. Frases que deben evitarse

Para que la presentación sea consistente con la implementación y el informe, evitar estas afirmaciones:

- **No decir:** “La solución está desplegada en AWS”.

  **Decir:** “La solución está preparada para AWS y cuenta con una estrategia documentada de despliegue”.
- **No decir:** “Es una plataforma bancaria lista para producción”.

  **Decir:** “Es una demostración académica funcional y reproducible”.
- **No decir:** “Kafka garantiza por sí solo la consistencia con la base de datos”.

  **Decir:** “Kafka desacopla el procesamiento posterior y transactional outbox queda propuesto como mejora”.
- **No decir:** “Las anomalías fueron descartadas”.

  **Decir:** “Las anomalías de negocio fueron conservadas con una observación para mantener trazabilidad”.
