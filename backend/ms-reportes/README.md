# ms-reportes

Microservicio encargado de registrar, consultar y administrar reportes de incendio.

## Responsabilidades

- Crear reportes de incendio.
- Consultar reportes historicos y activos.
- Cambiar estado y prioridad de reportes.
- Generar auditoria de reportes en Excel.
- Persistir reportes en PostgreSQL.
- Guardar ubicaciones asociadas usando `ms-geografico`.
- Emitir alertas hacia `ms-notificaciones` mediante Feign y RabbitMQ.
- Usar Redis como cache.

## Puerto

| Entorno | Puerto |
| --- | ---: |
| Local/Docker | `8081` |

URL base directa:

```text
http://localhost:8081
```

URL recomendada via gateway:

```text
http://localhost:8000
```

## Dependencias

- PostgreSQL, esquema `reportes_db`
- Eureka Server
- Redis
- RabbitMQ
- `ms-geografico`
- `ms-notificaciones`

## Variables de entorno

| Variable | Valor por defecto | Descripcion |
| --- | --- | --- |
| `PORT` | `8081` | Puerto HTTP del servicio |
| `EUREKA_URL` | `http://eureka-server:8761/eureka/` | URL de Eureka |
| `MS_GEOGRAFICO_URL` | Requerida en Docker Compose | URL de `ms-geografico` |
| `MS_NOTIFICACIONES_URL` | Requerida en Docker Compose | URL de `ms-notificaciones` |
| `REDIS_HOST` | `redis` | Host de Redis |
| `REDIS_PORT` | `6379` | Puerto de Redis |

La conexion actual a PostgreSQL esta configurada en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://postgres-db:5432/eft_incendios?currentSchema=reportes_db
spring.datasource.username=postgres
spring.datasource.password=root
```

RabbitMQ:

```properties
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## Endpoints principales

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `POST` | `/api/reportes` | Crea un nuevo reporte |
| `GET` | `/api/reportes` | Lista reportes historicos |
| `GET` | `/api/reportes/activos` | Lista reportes activos |
| `GET` | `/api/reportes/{id}` | Obtiene un reporte por id |
| `PATCH` | `/api/reportes/{id}/estado?nuevoEstado=...` | Actualiza estado |
| `PATCH` | `/api/reportes/{id}/prioridad?nuevaPrioridad=...` | Actualiza prioridad |
| `GET` | `/api/reportes/auditoria/excel` | Descarga auditoria en Excel |

## Ejemplo de creacion

```bash
curl -X POST http://localhost:8000/api/reportes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{\"descripcion\":\"Humo visible cerca del parque\",\"latitud\":-33.45,\"longitud\":-70.66,\"urlMedia\":\"https://example.com/foto.jpg\",\"tipoUsuario\":\"CIUDADANO\",\"usuarioId\":1,\"prioridad\":\"ALTA\"}"
```

Payload:

```json
{
  "descripcion": "Humo visible cerca del parque",
  "latitud": -33.45,
  "longitud": -70.66,
  "urlMedia": "https://example.com/foto.jpg",
  "tipoUsuario": "CIUDADANO",
  "usuarioId": 1,
  "prioridad": "ALTA"
}
```

## Roles operativos

Para modificar estado o prioridad se valida el header interno `X-Usuario-Rol`.

Roles permitidos:

- `ADMIN`
- `BOMBERO`
- `BRIGADISTA`
- `FUNCIONARIO`

Para descargar auditoria Excel:

- `ADMIN`
- `FUNCIONARIO`

## Swagger

```text
http://localhost:8081/api/reportes/swagger-ui.html
```

## Ejecutar con Docker Compose

Desde la raiz del repositorio:

```bash
docker compose up --build postgres-db redis rabbitmq eureka-server ms-geografico ms-notificaciones ms-reportes
```

## Ejecutar localmente

Antes de iniciar, asegurese de tener PostgreSQL, Redis, RabbitMQ, Eureka y los servicios dependientes disponibles.

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

- Al crear un reporte, el servicio intenta registrar la ubicacion en `ms-geografico`.
- Tambien envia una alerta a `notificaciones.queue` para que `ms-notificaciones` la procese.
- Si una integracion externa falla, revisar logs de `ms-reportes`, RabbitMQ y los servicios dependientes.
