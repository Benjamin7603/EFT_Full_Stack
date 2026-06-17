# ms-usuarios

Microservicio responsable de usuarios, autenticacion y emision de tokens JWT.

## Responsabilidades

- Registrar usuarios.
- Autenticar credenciales con `POST /api/auth/login`.
- Emitir JWT con username, rol e id de usuario.
- Gestionar perfil, listado, actualizacion y eliminacion de usuarios.
- Restringir acciones administrativas segun rol.

## Puerto

| Entorno | Puerto |
| --- | ---: |
| Local/Docker | `8082` |

URL base directa:

```text
http://localhost:8082
```

URL recomendada via gateway:

```text
http://localhost:8000
```

## Base de datos

Usa PostgreSQL:

- Base: `eft_incendios`
- Esquema: `usuarios_db`
- Tabla principal: `usuarios`

El esquema se crea desde `init.sql` al levantar PostgreSQL con Docker Compose.

## Variables de entorno

| Variable | Valor por defecto | Descripcion |
| --- | --- | --- |
| `PORT` | `8082` | Puerto HTTP del servicio |
| `EUREKA_URL` | `http://eureka-server:8761/eureka/` | URL de Eureka |
| `JWT_SECRET` | `geofire-super-secret-key-2024-incendios-eft` | Clave para firmar JWT |

La conexion actual a PostgreSQL esta configurada en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://postgres-db:5432/eft_incendios?currentSchema=usuarios_db
spring.datasource.username=postgres
spring.datasource.password=root
```

## Endpoints principales

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Inicia sesion y devuelve JWT |
| `GET` | `/api/usuarios/me` | Obtiene perfil del usuario autenticado |
| `GET` | `/api/usuarios` | Lista usuarios, requiere rol `ADMIN` |
| `POST` | `/api/usuarios` | Registro publico de usuario |
| `PUT` | `/api/usuarios/{id}` | Actualiza usuario |
| `DELETE` | `/api/usuarios/{id}` | Elimina usuario |
| `POST` | `/api/usuarios/admin` | Crea usuario desde panel admin |

## Ejemplo de login

```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

Respuesta esperada:

```json
{
  "token": "...",
  "username": "admin",
  "rol": "ADMIN",
  "id": 1,
  "nombre": "Administrador"
}
```

## Ejemplo de registro

```json
{
  "nombre": "Carlos",
  "apellido": "Moil",
  "email": "carlos@mail.com",
  "telefono": "+56912345678",
  "username": "carlos",
  "password": "123456"
}
```

Campos relevantes del modelo:

- `nombre`
- `apellido`
- `email`
- `telefono`
- `fechaNacimiento`
- `rol`
- `username`
- `password`
- `activo`

## Swagger

```text
http://localhost:8082/api/usuarios/swagger-ui.html
```

## Ejecutar con Docker Compose

Desde la raiz del repositorio:

```bash
docker compose up --build postgres-db eureka-server ms-usuarios
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

- `POST /api/usuarios` funciona como registro publico.
- `GET /api/usuarios` y `POST /api/usuarios/admin` requieren rol `ADMIN`.
- El gateway agrega headers internos como `X-Usuario-Id` y `X-Usuario-Rol` despues de validar el JWT.
