# GeoFire DevOps AWS ECS Fargate

Este documento resume la preparacion del proyecto GeoFire para despliegue con AWS ECS Fargate, DockerHub y GitHub Actions, manteniendo el flujo local con `docker-compose.yml`.

## Arquitectura objetivo

Usuario -> Application Load Balancer publico -> `frontend-web` React/Vite servido por Nginx -> `ms-gateway` Spring Cloud Gateway -> `ms-bff` -> microservicios Spring Boot:

- `ms-usuarios`
- `ms-reportes`
- `ms-geografico`
- `ms-notificaciones`

Servicios de soporte:

- `ms-eureka-server` para discovery, si se mantiene en AWS.
- PostgreSQL para persistencia.
- Redis para cache.
- RabbitMQ para mensajeria de notificaciones, porque el proyecto actual lo usa en `docker-compose.yml`.
- CloudWatch Logs para logs de contenedores ECS.

## Imagenes DockerHub

Reemplazar `<dockerhub-user>` por el usuario real de DockerHub:

- `<dockerhub-user>/geofire-frontend:latest`
- `<dockerhub-user>/geofire-gateway:latest`
- `<dockerhub-user>/geofire-bff:latest`
- `<dockerhub-user>/geofire-eureka:latest`
- `<dockerhub-user>/geofire-usuarios:latest`
- `<dockerhub-user>/geofire-reportes:latest`
- `<dockerhub-user>/geofire-geografico:latest`
- `<dockerhub-user>/geofire-notificaciones:latest`

## Variables y secrets

Secrets de GitHub Actions:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `ECS_CLUSTER_NAME`
- `VITE_API_URL`
- `JWT_SECRET`

Variables recomendadas para tareas ECS:

- `FRONTEND_ORIGIN`: origen permitido por CORS. Ejemplo: `https://app.geofire.cl` o URL del ALB.
- `JWT_SECRET`: debe ser igual en `ms-gateway`, `ms-bff` y `ms-usuarios`.
- `EUREKA_URL` o `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`: URL interna del servicio Eureka.
- `MS_GEOGRAFICO_URL`, `MS_REPORTES_URL`, `MS_USUARIOS_URL`, `MS_NOTIFICACIONES_URL`: URLs internas para Feign cuando aplique.
- `REDIS_HOST`, `REDIS_PORT`: endpoint interno de Redis.
- Variables de PostgreSQL segun la estrategia elegida para AWS. Para produccion se recomienda RDS y credenciales en Secrets Manager o variables seguras de ECS.

## Pipeline GitHub Actions

Workflow creado en `.github/workflows/deploy-aws.yml`.

Flujo:

1. Checkout del repositorio.
2. Login en DockerHub.
3. Build de imagenes Docker.
4. Push de imagenes a DockerHub.
5. Configuracion de credenciales AWS.
6. `aws ecs update-service --force-new-deployment`.
7. Espera de estabilidad con `aws ecs wait services-stable`.

El workflow corre al hacer push a `feature/devops-aws-deploy` y tambien puede ejecutarse manualmente con `workflow_dispatch`.

Los nombres de servicios ECS quedaron como valores editables en el job `deploy`:

- `geofire-frontend`
- `geofire-gateway`
- `geofire-bff`
- `geofire-eureka`
- `geofire-usuarios`
- `geofire-reportes`
- `geofire-geografico`
- `geofire-notificaciones`

Si en AWS se crean con otros nombres, actualizar esos valores en el workflow.

## Comandos locales

Levantar ambiente local:

```bash
docker compose up --build
```

Levantar frontend local con Vite:

```bash
cd frontend-web
npm install
npm run dev
```

Build frontend apuntando a otro gateway:

```bash
docker build --build-arg VITE_API_URL=https://api.example.com -t geofire-frontend ./frontend-web
```

Build de un microservicio:

```bash
docker build -t geofire-gateway ./backend/ms-gateway
```

## Configuracion frontend

El cliente Axios usa:

```js
import.meta.env.VITE_API_URL || 'http://localhost:8000'
```

Esto mantiene desarrollo local sin `.env` y permite que AWS use el valor del secret `VITE_API_URL` durante el build Docker.

## Configuracion Gateway

CORS es configurable con `FRONTEND_ORIGIN`.

Valor local por defecto:

```text
http://localhost,http://localhost:80,http://localhost:5173
```

Para AWS, definir el origen publico del frontend:

```text
FRONTEND_ORIGIN=https://frontend.example.com
```

Si hay mas de un origen, separarlos por coma.

## Evidencias sugeridas para evaluacion

- Captura del cluster ECS con servicios en estado `Running`.
- Captura del Application Load Balancer y target groups saludables.
- Captura de DockerHub con las imagenes `geofire-*`.
- Captura de GitHub Actions con workflow exitoso.
- Captura de CloudWatch Logs de `frontend-web`, `ms-gateway`, `ms-bff` y microservicios.
- Prueba funcional: login, registro, listado/creacion de reportes y notificaciones.

## Notas de seguridad

- No guardar credenciales reales en el repositorio.
- Usar GitHub Secrets para DockerHub y AWS.
- Usar variables seguras de ECS o Secrets Manager para credenciales de base de datos y `JWT_SECRET`.
- Mantener `docker-compose.yml` para desarrollo local.
- No eliminar Eureka sin decidir primero si ECS usara discovery de Eureka, DNS interno o Cloud Map.
