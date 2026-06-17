# ms-eureka-server

Servidor de descubrimiento de servicios para GeoFire. Permite que los microservicios se registren y se encuentren entre si dentro de la red Docker o del entorno local.

## Responsabilidades

- Levantar Eureka Server.
- Mantener el registro de instancias disponibles.
- Exponer una consola web para revisar el estado de los servicios registrados.
- Servir como punto comun de descubrimiento para gateway, BFF y servicios de dominio.

## Puerto

| Entorno | Puerto |
| --- | ---: |
| Local/Docker | `8761` |

URL principal:

```text
http://localhost:8761
```

## Configuracion principal

Archivo: `src/main/resources/application.yml`

```yaml
server:
  port: ${PORT:8761}

spring:
  application:
    name: ms-eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

## Variables de entorno

| Variable | Valor por defecto | Descripcion |
| --- | --- | --- |
| `PORT` | `8761` | Puerto HTTP del servidor Eureka |

## Ejecutar con Docker Compose

Desde la raiz del repositorio:

```bash
docker compose up --build eureka-server
```

## Ejecutar localmente

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

Ejemplo:

```bash
curl http://localhost:8761/actuator/health
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

- Este servicio debe estar disponible antes de levantar gateway y microservicios que se registran en Eureka.
- En Docker, los servicios usan `http://eureka-server:8761/eureka/`.
- En local, puedes apuntar clientes a `http://localhost:8761/eureka/`.
