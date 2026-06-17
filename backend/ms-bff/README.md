# ms-bff

Backend for Frontend de GeoFire. Expone endpoints orientados a vistas del frontend cuando se necesita combinar datos de varios microservicios.

## Responsabilidades

- Agregar informacion de reportes y ubicaciones.
- Centralizar llamadas a `ms-reportes` y `ms-geografico`.
- Mantener llamadas asincronas para mejorar tiempo de respuesta en vistas compuestas.
- Propagar el JWT recibido hacia clientes Feign.
- Registrarse en Eureka.

## Puerto

| Entorno | Puerto |
| --- | ---: |
| Local/Docker | `8085` |

URL base directa:

```text
http://localhost:8085
```

URL recomendada via gateway:

```text
http://localhost:8000
```

## Dependencias

- Eureka Server
- `ms-reportes`
- `ms-geografico`
- `ms-usuarios`
- `ms-notificaciones`

## Variables de entorno

| Variable | Valor por defecto | Descripcion |
| --- | --- | --- |
| `PORT` | `8085` | Puerto HTTP del servicio |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://eureka-server:8761/eureka/` | URL de Eureka |
| `JWT_SECRET` | `geofire-super-secret-key-2024-incendios-eft` | Clave compartida JWT |
| `MS_GEOGRAFICO_URL` | `http://ms-geografico:8083` | URL de `ms-geografico` |
| `MS_REPORTES_URL` | `http://ms-reportes:8081` | URL de `ms-reportes` |
| `MS_USUARIOS_URL` | `http://ms-usuarios:8082` | URL de `ms-usuarios` |
| `MS_NOTIFICACIONES_URL` | `http://ms-notificaciones:8084` | URL de `ms-notificaciones` |

## Endpoints principales

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `GET` | `/bff/estado` | Verifica que el BFF esta activo |
| `GET` | `/bff/reportes` | Lista reportes desde `ms-reportes` |
| `GET` | `/bff/geografico/reporte/{idReporte}` | Obtiene ubicacion desde `ms-geografico` |
| `POST` | `/bff/reportar-incendio` | Crea un reporte mediante `ms-reportes` |
| `GET` | `/bff/incendio/{id}` | Devuelve reporte y ubicacion en una sola respuesta |

## Ejemplo

```bash
curl http://localhost:8000/bff/incendio/1 \
  -H "Authorization: Bearer <token>"
```

Respuesta conceptual:

```json
{
  "reporte": {
    "id": 1,
    "descripcion": "Humo visible cerca del parque"
  },
  "ubicacion": {
    "idReporte": 1,
    "latitud": -33.45,
    "longitud": -70.66
  }
}
```

Si falla la ubicacion, el endpoint mantiene respuesta parcial con:

```json
{
  "alerta": "Ubicacion temporalmente no disponible"
}
```

## Ejecutar con Docker Compose

Desde la raiz del repositorio:

```bash
docker compose up --build eureka-server ms-reportes ms-geografico ms-bff
```

En uso normal conviene levantar todo el stack:

```bash
docker compose up --build
```

## Ejecutar localmente

Antes de iniciar, asegurese de tener Eureka y los servicios dependientes disponibles.

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Para apuntar a servicios locales:

```powershell
$env:MS_REPORTES_URL="http://localhost:8081"
$env:MS_GEOGRAFICO_URL="http://localhost:8083"
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

- El BFF no reemplaza al gateway; el gateway sigue siendo la entrada publica recomendada.
- Este servicio es util cuando una pantalla necesita datos de mas de un microservicio.
- Si aparecen errores Feign, revisar URLs `MS_*_URL` y que el token se este propagando correctamente.
