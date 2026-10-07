# Plan de despliegue en AWS

## Objetivo

Este documento describe cómo trasladar los contenedores validados con Docker Compose a AWS con alta disponibilidad, escalamiento horizontal, secretos administrados y observabilidad centralizada. El repositorio contiene la preparación técnica y el procedimiento; la creación de los recursos cloud se realiza al aplicar este plan en una cuenta AWS.

## Estado de esta entrega

La preparación solicitada se materializa en Dockerfiles ejecutables, configuración externalizada, Docker Compose, descubrimiento de servicios, balanceo y una demostración de dos instancias para `customer-service`, `account-service` y `payment-service`. Este documento completa esa preparación con la arquitectura objetivo, el procedimiento, las herramientas y las configuraciones requeridas para AWS.

No se aprovisionaron recursos en una cuenta AWS durante la validación del proyecto, ya que el alcance corresponde a la preparación y explicación del proceso. Por tanto, ECR, ECS Fargate, RDS, MSK, Secrets Manager y CloudWatch se describen como componentes del despliegue propuesto, no como recursos actualmente activos. La evidencia ejecutada pertenece al ambiente local construido con Docker Compose.

## Servicios propuestos

| Componente local | Servicio AWS | Decisión |
|---|---|---|
| Imágenes Docker | Amazon ECR | Repositorio versionado por microservicio |
| Contenedores Java | Amazon ECS con Fargate | Operación administrada y escalamiento por servicio |
| Entrada HTTP | Application Load Balancer | Rutas públicas solo hacia los tres BFF y autorización |
| TLS | AWS Certificate Manager | Certificados válidos y renovación administrada |
| PostgreSQL | Amazon RDS for PostgreSQL Multi AZ | Persistencia administrada, backups y failover |
| Kafka | Amazon MSK | Brokers administrados y cifrado en tránsito |
| Secretos | AWS Secrets Manager | Contraseñas de base de datos y clientes OAuth2 |
| Métricas y logs | Amazon CloudWatch | Logs de contenedor, alarmas y paneles |
| Imágenes y artefactos | Amazon S3 opcional | Informes, respaldos y archivos batch |

## Topología

Los tres BFF y `auth-server` se ubican detrás de un ALB con HTTPS. Los servicios de clientes, cuentas y pagos permanecen en subredes privadas sin dirección pública. RDS y MSK se despliegan en subredes de datos y aceptan tráfico únicamente desde los security groups de las tareas ECS.

Eureka puede mantenerse como servicio ECS interno para conservar la implementación académica. En una evolución productiva puede reemplazarse por AWS Cloud Map sin alterar los límites de dominio. Config Server debe usar un repositorio Git privado o integrar AWS Parameter Store; la configuración nativa incluida en el JAR se utiliza solo para la demostración reproducible.

## Procedimiento

1. Crear repositorios ECR para `bff-web`, `bff-mobile`, `bff-atm`, `auth-server`, `config-server`, `discovery-server`, `customer-service`, `account-service` y `payment-service`.
2. Compilar con `mvn -B clean package` y construir imágenes multi-arquitectura mediante `docker buildx`.
3. Etiquetar cada imagen con el número de versión o SHA del commit y publicarla en ECR.
4. Crear una VPC en al menos dos zonas de disponibilidad, con subredes públicas para ALB y privadas para ECS, RDS y MSK.
5. Crear RDS PostgreSQL Multi AZ. Separar bases o esquemas y usuarios para clientes, cuentas y pagos.
6. Crear el clúster MSK y los tópicos de transacciones y alertas con un factor de replicación apropiado para producción.
7. Guardar secretos en Secrets Manager y exponerlos a las tareas ECS mediante roles IAM, nunca en la definición de imagen.
8. Crear las definiciones de tarea y servicios ECS. Configurar health checks, despliegue gradual y al menos dos tareas por servicio crítico.
9. Configurar el ALB, los listeners HTTPS y el certificado de ACM. No publicar directamente los servicios de negocio.
10. Habilitar logs de aplicación en CloudWatch y alarmas para errores 5xx, latencia, circuit breakers abiertos, CPU, memoria, conexiones RDS y retraso de consumidores Kafka.
11. Ejecutar smoke tests con tokens por canal y una transferencia idempotente antes de promover la versión.

## Variables principales

| Variable | Origen recomendado |
|---|---|
| `CONFIG_SERVER_URL` | Service discovery o nombre DNS privado |
| `EUREKA_URL` | DNS privado del clúster Eureka |
| `JWT_JWK_SET_URI` | URL privada o pública controlada de autorización |
| `DATABASE_URL` | Configuración de tarea ECS |
| `DATABASE_USERNAME` | Secrets Manager |
| `DATABASE_PASSWORD` | Secrets Manager |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers TLS de Amazon MSK |
| Secretos OAuth2 | Secrets Manager |

## Escalamiento y resiliencia

Cada microservicio de negocio se despliega como un servicio ECS independiente. Application Auto Scaling aumenta tareas según CPU, memoria, latencia o métricas personalizadas. Kafka distribuye las particiones entre consumidores del mismo grupo. La base de datos utiliza Multi AZ y backups automáticos.

Los timeouts, circuit breakers y fallbacks existentes evitan que una falla se propague sin control. La idempotencia protege los reintentos del cliente y la compensación revierte el débito cuando una transferencia no puede acreditar el destino. Para producción se debe sumar transactional outbox y reconciliación programada.

## Seguridad operativa

- Aplicar mínimo privilegio en roles IAM por servicio.
- Cifrar tráfico externo con TLS y conexiones a RDS y MSK con TLS.
- Rotar secretos y evitar valores por defecto de Compose.
- Restringir `/actuator` a la red de monitoreo o a endpoints concretos.
- Incorporar análisis de imágenes ECR y dependencias en CI.
- Conservar auditoría de pagos, cambios de cliente y alertas de seguridad.

## Estrategia de entrega continua

Una canalización recomendada ejecuta pruebas Maven, crea imágenes, analiza vulnerabilidades, publica en ECR y actualiza ECS con despliegue blue green o rolling. La promoción a producción requiere comprobar salud, registro de instancias, autenticación, eventos Kafka y una operación financiera idempotente en un entorno previo.
