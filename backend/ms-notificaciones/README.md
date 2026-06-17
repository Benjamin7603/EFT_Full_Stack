# ms-notificaciones

Microservicio encargado de crear, consultar y administrar notificaciones de GeoFire.

## Responsabilidades

- Registrar alertas del sistema.
- Mantener historial de notificaciones.
- Filtrar notificaciones por destinatario.
- Consultar y marcar notificaciones no leidas.
- Consumir eventos desde RabbitMQ en la cola `notificaciones.queue`.
- Persistir notificaciones en PostgreSQL.

## Puerto

| Entorno | Puerto |
| --- | ---: |
| Local/Docker | `8084` |

URL base directa:

```text
http://localhost:8084
```

URL recomendada via gateway:

```text
http://localhost:8000
```

## Dependencias

- PostgreSQL, esquema `notificaciones_db`
- Eureka Server
- RabbitMQ

## Variables de entorno

| Variable | Valor por defecto | Descripcion |
| --- | --- | --- |
| `PORT` | `8084` | Puerto HTTP del servicio |
| `EUREKA_URL` | `http://eureka-server:8761/eureka/` | URL de Eureka |

RabbitMQ esta configurado en `src/main/resources/application.properties`:

```properties
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://postgres-db:5432/eft_incendios?currentSchema=notificaciones_db
spring.datasource.username=postgres
spring.datasource.password=root
```

## Endpoints principales

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `POST` | `/api/notificaciones/enviar` | Crea una notificacion, usado por `ms-reportes` |
| `POST` | `/api/notificaciones` | Crea una notificacion |
| `GET` | `/api/notificaciones` | Lista historial completo |
| `GET` | `/api/notificaciones/{id}` | Obtiene una notificacion por id |
| `GET` | `/api/notificaciones/destinatario/{destinatario}` | Lista por destinatario |
| `GET` | `/api/notificaciones/destinatario/{destinatario}/no-leidas` | Lista no leidas |
| `GET` | `/api/notificaciones/destinatario/{destinatario}/contador` | Cuenta no leidas |
| `PATCH` | `/api/notificaciones/{id}/leer` | Marca una notificacion como leida |
| `PATCH` | `/api/notificaciones/destinatario/{destinatario}/leer-todas` | Marca todas como leidas |
| `DELETE` | `/api/notificaciones/{id}` | Elimina una notificacion |

## Ejemplo de creacion

```bash
curl -X POST http://localhost:8000/api/notificaciones \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{\"titulo\":\"Alerta GeoFire\",\"mensaje\":\"Nuevo reporte de incendio\",\"destinatario\":\"BRIGADAS_ZONA_SUR\",\"tipo\":\"REPORTE\",\"prioridad\":\"ALTA\",\"reporteId\":1}"
```

Payload:

```json
{
  "titulo": "Alerta GeoFire",
  "mensaje": "Nuevo reporte de incendio",
  "destinatario": "BRIGADAS_ZONA_SUR",
  "tipo": "REPORTE",
  "prioridad": "ALTA",
  "reporteId": 1
}
```

Valores por defecto del modelo:

- `titulo`: `Alerta GeoFire`
- `tipo`: `SISTEMA`
- `prioridad`: `MEDIA`
- `leida`: `false`

## Swagger

```text
http://localhost:8084/api/notificaciones/swagger-ui.html
```

## Ejecutar con Docker Compose

Desde la raiz del repositorio:

```bash
docker compose up --build postgres-db rabbitmq eureka-server ms-notificaciones
```

## Ejecutar localmente

Antes de iniciar, asegurese de tener PostgreSQL, RabbitMQ y Eureka disponibles.

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Health check

```text
GET /actuator/health
```

## Pruebas

Linux/macOS:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Notas de desarrollo

- El servicio declara la cola durable `notificaciones.queue`.
- `ms-reportes` publica eventos en esa cola al crear reportes.
- RabbitMQ Management queda disponible en `http://localhost:15672` cuando se levanta con Docker Compose.
