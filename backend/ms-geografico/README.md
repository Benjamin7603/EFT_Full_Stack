# ms-geografico

Microservicio responsable de almacenar y consultar la ubicacion geografica asociada a reportes de incendio.

## Responsabilidades

- Guardar coordenadas de reportes.
- Consultar ubicaciones por `idReporte`.
- Persistir datos en PostgreSQL.
- Registrarse en Eureka.
- Exponer documentacion Swagger.

## Puerto

| Entorno | Puerto |
| --- | ---: |
| Local/Docker | `8083` |

URL base directa:

```text
http://localhost:8083
```

URL recomendada via gateway:

```text
http://localhost:8000
```

## Base de datos

Usa PostgreSQL:

- Base: `eft_incendios`
- Esquema: `geografico_db`
- Tabla principal: `ubicaciones`

La conexion actual esta en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://postgres-db:5432/eft_incendios?currentSchema=geografico_db
spring.datasource.username=postgres
spring.datasource.password=root
```

## Variables de entorno

| Variable | Valor por defecto | Descripcion |
| --- | --- | --- |
| `PORT` | `8083` | Puerto HTTP del servicio |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://eureka-server:8761/eureka/` | URL de Eureka |

## Endpoints principales

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `POST` | `/api/geografico/guardar` | Guarda ubicacion de un reporte |
| `GET` | `/api/geografico/reporte/{idReporte}` | Obtiene ubicacion asociada a un reporte |

## Ejemplo de creacion

```bash
curl -X POST http://localhost:8000/api/geografico/guardar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{\"idReporte\":1,\"latitud\":-33.45,\"longitud\":-70.66}"
```

Payload:

```json
{
  "idReporte": 1,
  "latitud": -33.45,
  "longitud": -70.66
}
```

Modelo principal:

- `idReporte`
- `latitud`
- `longitud`
- `zonaRiesgo`

## Swagger

```text
http://localhost:8083/api/geografico/swagger-ui.html
```

## Ejecutar con Docker Compose

Desde la raiz del repositorio:

```bash
docker compose up --build postgres-db eureka-server ms-geografico
```

## Ejecutar localmente

Antes de iniciar, asegurese de tener PostgreSQL y Eureka disponibles.

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

- `ms-reportes` llama este servicio al crear reportes.
- `ms-bff` lo consulta para entregar vistas agregadas de incendio.
- Si se ejecuta fuera de Docker, ajustar la URL JDBC para apuntar al host real de PostgreSQL.
