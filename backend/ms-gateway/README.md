# ms-gateway

API Gateway de GeoFire. Es el punto de entrada recomendado para el frontend y clientes externos.

## Responsabilidades

- Enrutar solicitudes hacia los microservicios registrados en Eureka.
- Aplicar configuracion CORS.
- Validar tokens JWT en rutas protegidas.
- Propagar datos del usuario autenticado hacia servicios internos.
- Exponer health check para monitoreo.

## Puerto

| Entorno | Puerto |
| --- | ---: |
| Local/Docker | `8000` |

URL base:

```text
http://localhost:8000
```

## Rutas configuradas

| Ruta publica en gateway | Destino |
| --- | --- |
| `/api/auth/**` | `MS-USUARIOS` |
| `/api/usuarios/**` | `MS-USUARIOS` |
| `/api/reportes/**` | `MS-REPORTES` |
| `/api/geografico/**` | `MS-GEOGRAFICO` |
| `/api/notificaciones/**` | `MS-NOTIFICACIONES` |
| `/bff/**` | `MS-BFF` |

## Seguridad

Rutas publicas principales:

- `POST /api/auth/login`
- `POST /api/usuarios`
- Swagger/OpenAPI y `GET /actuator/health`

El resto de rutas espera:

```http
Authorization: Bearer <token>
```

La clave JWT debe coincidir con la usada por `ms-usuarios` y `ms-bff`.

## Variables de entorno

| Variable | Valor por defecto | Descripcion |
| --- | --- | --- |
| `PORT` | `8000` | Puerto HTTP del gateway |
| `EUREKA_URL` | `http://eureka-server:8761/eureka/` | URL de registro Eureka |
| `JWT_SECRET` | `geofire-super-secret-key-2024-incendios-eft` | Clave para validar JWT |
| `FRONTEND_ORIGIN` | `http://localhost,http://localhost:80,http://localhost:5173` | Origenes permitidos por CORS |

## Ejecutar con Docker Compose

Desde la raiz del repositorio:

```bash
docker compose up --build ms-gateway
```

Normalmente se levanta junto a Eureka:

```bash
docker compose up --build eureka-server ms-gateway
```

## Ejecutar localmente

Antes de iniciar, asegurese de tener Eureka disponible.

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Para apuntar a Eureka local:

```powershell
$env:EUREKA_URL="http://localhost:8761/eureka/"
.\mvnw.cmd spring-boot:run
```

## Health check

```text
GET /actuator/health
```

Ejemplo:

```bash
curl http://localhost:8000/actuator/health
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

- Si el frontend recibe errores CORS, revisar `FRONTEND_ORIGIN`.
- Si las rutas protegidas responden `401`, validar que el token venga con `Bearer` y que `JWT_SECRET` coincida.
- Si una ruta no resuelve, revisar en Eureka que el servicio destino este registrado.
