# GeoFire - Plataforma de gestion y prevencion de incendios

GeoFire es una plataforma full stack para registrar, visualizar y gestionar reportes de incendio. El sistema centraliza avisos ciudadanos, ubicaciones geograficas, usuarios, notificaciones y paneles operativos para apoyar la respuesta temprana ante emergencias.

El proyecto esta organizado con una arquitectura de microservicios Spring Boot, un frontend web en React/Vite y servicios de infraestructura levantados con Docker Compose.

## Contenido

- [Arquitectura](#arquitectura)
- [Servicios](#servicios)
- [Tecnologias](#tecnologias)
- [Requisitos](#requisitos)
- [Ejecucion con Docker Compose](#ejecucion-con-docker-compose)
- [Ejecucion local para desarrollo](#ejecucion-local-para-desarrollo)
- [Variables de entorno](#variables-de-entorno)
- [Endpoints principales](#endpoints-principales)
- [Pruebas](#pruebas)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Flujo de trabajo Git](#flujo-de-trabajo-git)

## Arquitectura

La solucion usa microservicios independientes comunicados mediante HTTP, Eureka Service Discovery y, para eventos de notificaciones, RabbitMQ.

Flujo principal:

1. El usuario interactua con `frontend-web`.
2. El frontend consume el `ms-gateway` en el puerto `8000`.
3. El gateway valida JWT, aplica CORS y enruta hacia los microservicios.
4. Los servicios de dominio persisten en PostgreSQL usando esquemas separados por servicio.
5. `ms-reportes` registra reportes, guarda ubicaciones mediante `ms-geografico`, usa Redis para cache y publica alertas hacia RabbitMQ.
6. `ms-notificaciones` consume la cola `notificaciones.queue` y mantiene el historial de alertas.
7. `ms-bff` expone endpoints agregados para vistas que necesitan combinar datos de reportes y ubicacion.

## Servicios

| Servicio | Puerto | Responsabilidad | README |
| --- | ---: | --- | --- |
| `ms-eureka-server` | `8761` | Registro y descubrimiento de servicios | [backend/ms-eureka-server/README.md](backend/ms-eureka-server/README.md) |
| `ms-gateway` | `8000` | Entrada publica, CORS, JWT y ruteo | [backend/ms-gateway/README.md](backend/ms-gateway/README.md) |
| `ms-usuarios` | `8082` | Usuarios, autenticacion y JWT | [backend/ms-usuarios/README.md](backend/ms-usuarios/README.md) |
| `ms-reportes` | `8081` | Reportes de incendio, auditoria, cache y eventos | [backend/ms-reportes/README.md](backend/ms-reportes/README.md) |
| `ms-geografico` | `8083` | Ubicaciones asociadas a reportes | [backend/ms-geografico/README.md](backend/ms-geografico/README.md) |
| `ms-notificaciones` | `8084` | Alertas, historial y lectura de notificaciones | [backend/ms-notificaciones/README.md](backend/ms-notificaciones/README.md) |
| `ms-bff` | `8085` | Endpoints agregados para frontend | [backend/ms-bff/README.md](backend/ms-bff/README.md) |
| `frontend-web` | `80` en Docker, `5173` local | Aplicacion React | [frontend-web/README.md](frontend-web/README.md) |

Infraestructura:

| Servicio | Puerto | Uso |
| --- | ---: | --- |
| PostgreSQL | `5432` | Base de datos `eft_incendios` |
| Redis | `6379` | Cache de `ms-reportes` |
| RabbitMQ | `5672` | Mensajeria entre reportes y notificaciones |
| RabbitMQ Management | `15672` | Consola web de RabbitMQ |

## Tecnologias

- Java 17
- Spring Boot 3
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Security con JWT
- Spring Data JPA
- PostgreSQL
- Redis
- RabbitMQ
- OpenFeign
- Springdoc OpenAPI / Swagger UI
- React 19, Vite, Axios, React Router, Leaflet
- Docker y Docker Compose
- JUnit, Mockito, Vitest y Testing Library

## Requisitos

Para levantar todo con contenedores:

- Docker
- Docker Compose

Para desarrollo local:

- Java 17
- Maven o los wrappers `mvnw` incluidos en cada microservicio
- Node.js y npm
- Docker para PostgreSQL, Redis, RabbitMQ y Eureka, o equivalentes locales

## Ejecucion con Docker Compose

Desde la raiz del repositorio:

```bash
docker compose up --build
```

URLs utiles:

- Frontend: `http://localhost`
- Gateway: `http://localhost:8000`
- Eureka: `http://localhost:8761`
- RabbitMQ Management: `http://localhost:15672` con usuario `guest` y password `guest`
- PostgreSQL: `localhost:5432`, base `eft_incendios`, usuario `postgres`, password `root`

Para detener los servicios:

```bash
docker compose down
```

Si necesitas limpiar volumenes/estado local de contenedores, revisa primero que no haya datos que quieras conservar.

## Ejecucion local para desarrollo

La forma mas simple es levantar dependencias con Docker Compose y correr el servicio que estas modificando con Maven.

Ejemplo para `ms-reportes`:

```bash
cd backend/ms-reportes
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
cd backend/ms-reportes
.\mvnw.cmd spring-boot:run
```

Frontend local:

```bash
cd frontend-web
npm install
npm run dev
```

Por defecto el frontend consume `http://localhost:8000`. Puedes cambiarlo con `VITE_API_URL`.

## Variables de entorno

| Variable | Usada por | Valor por defecto | Descripcion |
| --- | --- | --- | --- |
| `PORT` | Servicios Spring Boot | Depende del servicio | Puerto HTTP del microservicio |
| `EUREKA_URL` | Gateway, usuarios, reportes, notificaciones | `http://eureka-server:8761/eureka/` | URL de Eureka |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka clients especificos | `http://eureka-server:8761/eureka/` | URL de Eureka en formato Spring |
| `JWT_SECRET` | Gateway, usuarios, BFF | `geofire-super-secret-key-2024-incendios-eft` | Clave compartida para firmar/validar JWT |
| `FRONTEND_ORIGIN` | Gateway | `http://localhost,http://localhost:80,http://localhost:5173` | Origenes permitidos por CORS |
| `VITE_API_URL` | Frontend | `http://localhost:8000` | URL base del backend |
| `MS_GEOGRAFICO_URL` | Reportes, BFF | `http://ms-geografico:8083` | URL del servicio geografico |
| `MS_REPORTES_URL` | BFF | `http://ms-reportes:8081` | URL del servicio de reportes |
| `MS_USUARIOS_URL` | BFF | `http://ms-usuarios:8082` | URL del servicio de usuarios |
| `MS_NOTIFICACIONES_URL` | Reportes, BFF | `http://ms-notificaciones:8084` | URL del servicio de notificaciones |
| `REDIS_HOST` | Reportes | `redis` | Host de Redis |
| `REDIS_PORT` | Reportes | `6379` | Puerto de Redis |

## Endpoints principales

La API se consume preferentemente a traves del gateway `http://localhost:8000`.

| Metodo | Ruta | Servicio | Uso |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | `ms-usuarios` | Inicio de sesion |
| `POST` | `/api/usuarios` | `ms-usuarios` | Registro publico |
| `GET` | `/api/usuarios/me` | `ms-usuarios` | Perfil autenticado |
| `GET` | `/api/reportes` | `ms-reportes` | Listar reportes |
| `POST` | `/api/reportes` | `ms-reportes` | Crear reporte |
| `GET` | `/api/reportes/activos` | `ms-reportes` | Reportes activos |
| `PATCH` | `/api/reportes/{id}/estado` | `ms-reportes` | Cambiar estado operativo |
| `PATCH` | `/api/reportes/{id}/prioridad` | `ms-reportes` | Cambiar prioridad |
| `GET` | `/api/reportes/auditoria/excel` | `ms-reportes` | Descargar auditoria Excel |
| `GET` | `/api/geografico/reporte/{idReporte}` | `ms-geografico` | Consultar ubicacion de un reporte |
| `GET` | `/api/notificaciones` | `ms-notificaciones` | Historial de notificaciones |
| `GET` | `/bff/incendio/{id}` | `ms-bff` | Vista agregada de reporte y ubicacion |

Los endpoints protegidos reciben el token en:

```http
Authorization: Bearer <token>
```

El gateway propaga informacion del usuario hacia los servicios mediante headers internos como `X-Usuario-Id` y `X-Usuario-Rol`.

## Documentacion Swagger

Cuando los servicios estan levantados, puedes revisar Swagger UI directamente en cada servicio:

- `http://localhost:8082/api/usuarios/swagger-ui.html`
- `http://localhost:8081/api/reportes/swagger-ui.html`
- `http://localhost:8083/api/geografico/swagger-ui.html`
- `http://localhost:8084/api/notificaciones/swagger-ui.html`

## Pruebas

Backend, desde cada microservicio:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Frontend:

```bash
cd frontend-web
npm test
```

## Estructura del repositorio

```text
.
|-- backend/
|   |-- ms-bff/
|   |-- ms-eureka-server/
|   |-- ms-gateway/
|   |-- ms-geografico/
|   |-- ms-notificaciones/
|   |-- ms-reportes/
|   `-- ms-usuarios/
|-- database/
|-- frontend-web/
|-- docker-compose.yml
|-- init.sql
|-- README.md
`-- README_DEVOPS.md
```

`init.sql` crea los esquemas logicos de PostgreSQL:

- `usuarios_db`
- `reportes_db`
- `geografico_db`
- `notificaciones_db`

## Flujo de trabajo Git

El proyecto usa una convencion basada en GitFlow:

1. Actualizar `main`.

```bash
git checkout main
git pull origin main
```

2. Crear una rama de trabajo.

```bash
git checkout -b feature/nombre-feature
```

3. Guardar cambios.

```bash
git add .
git commit -m "feat: descripcion breve del cambio"
```

4. Subir la rama.

```bash
git push -u origin feature/nombre-feature
```

5. Crear Pull Request hacia `main`, revisar y hacer merge.

Tipos sugeridos de ramas:

| Tipo | Uso |
| --- | --- |
| `feature/*` | Nuevas funcionalidades |
| `fix/*` | Correcciones |
| `hotfix/*` | Correcciones urgentes |
| `docs/*` | Cambios de documentacion |
