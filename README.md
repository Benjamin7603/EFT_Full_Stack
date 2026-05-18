# Plataforma Inteligente Para la Gestión y Prevención de Incendios

## 📖 Contexto del Proyecto
Esta plataforma tecnológica está diseñada para la Subdirección de Gestión de Emergencias de la Municipalidad Valle del Sol. El sistema busca digitalizar, centralizar y automatizar la gestión de incendios en la comuna, reemplazando procesos manuales y canales de comunicación informales que dificultaban la detección y respuesta temprana ante catástrofes.

## 🏗️ Arquitectura del Sistema
El proyecto utiliza una **arquitectura de microservicios altamente desacoplada**. En lugar de un sistema monolítico tradicional, la solución se divide en servicios independientes desarrollados en Java con Spring Boot, implementando el patrón *Database per Service* para asegurar escalabilidad y tolerancia a fallos.

El cliente principal es una aplicación móvil nativa diseñada bajo el principio de "cliente ligero", delegando toda la complejidad, validaciones de seguridad y lógica de negocio al ecosistema backend.

### 🧩 Ecosistema de Microservicios Actual
El backend está orquestado mediante los siguientes componentes principales:

1. **Eureka Server (Service Discovery):** Servidor de registro que permite a los distintos microservicios registrarse, encontrarse y comunicarse dinámicamente dentro de la red, facilitando la escalabilidad.
2. **API Gateway / BFF:** Construido con Spring Cloud Gateway. Actúa como el punto único de entrada que gestiona y enruta todas las solicitudes desde la app móvil hacia los microservicios correspondientes, simplificando la comunicación y desacoplando el frontend del backend.
3. **Microservicio de Usuarios:** Gestiona la autenticación, perfiles y administración. Soporta 3 tipos de usuario principales ("ciudadanos, brigadas, funcionarios") y una "cuenta de invitado" con funciones limitadas. Expone endpoints RESTful (GET, POST, PUT, DELETE) documentados mediante Swagger.
4. **Microservicio de Reportes de Incendios:** Encargado de procesar las alertas ciudadanas. Permite recibir reportes que incluyen la ubicación geográfica, fotos o videos del propio incendio para ser almacenados y procesados de manera estructurada.

*(Nota: La arquitectura completa proyecta la futura integración de un Microservicio Geográfico para mapeo en tiempo real y un Microservicio de Notificaciones).*

## 🛠️ Tecnologías Utilizadas

**Frontend (Aplicación Móvil):**
* **Lenguaje:** Kotlin.
* **UI:** Jetpack Compose para la creación de interfaces de usuario nativas de forma declarativa.
* **Networking:** Retrofit para consumo de APIs y comunicación asíncrona con el API Gateway.

**Backend & Servicios:**
* **Framework Core:** Java 17 & Spring Boot 3.
* **Enrutamiento:** Spring Cloud Gateway.
* **Base de Datos:** Supabase (PostgreSQL) con soporte para consultas geográficas (PostGIS).

**Calidad y Pruebas (QA):**
* JUnit, Mockito y SonarQube para garantizar un mínimo del 60% de cobertura en pruebas unitarias y asegurar la limpieza del software.

## ⚙️ Patrones de Diseño Implementados
* **Repository Pattern:** Se utiliza en los microservicios para separar la lógica de negocio de las consultas a la base de datos, facilitando el mantenimiento.
* **Factory Method:** Implementado en el Microservicio de Reportes para decidir qué tipo de objeto instanciar dependiendo de quién envía la alerta (ej. un ciudadano o un funcionario).
* **Circuit Breaker:** Protege la comunicación entre servicios para evitar colapsos en cadena. Si un servicio experimenta alta demanda (como en un incendio masivo), el circuito se "abre" para evitar que todo el sistema colapse.

## 👨‍💻 Equipo de Desarrollo
* **Benjamin García (Fullstack Developer):** Responsable del diseño e implementación de la arquitectura de microservicios, configuración de base de datos en la nube (Supabase) y redacción de la documentación técnica y arquitectónica.
* **Carlos Moil (Backend & Software Architect):** Encargado de la creación de la interfaz de usuario móvil mediante Kotlin y Jetpack Compose, integración de APIs (Retrofit), validaciones de seguridad de datos y creación de pruebas unitarias.

---

## Flujo de trabajo GitFlow
Para este proyecto se utiliza una metodología llamada GitFlow, para organizar el desarrollo del proyecto y mantener estabilidad en la rama principal.
### 1. Actualizar la rama principal
Antes de empezar a desarrollar el proyecto.
```bash
git checkout main
git pull origin main
```
### 2. Crear nueva rama feature
Ya desarrollado el proyecto crear la rama feature/cambio.
```bash
git checkout -b feature/nombre-feature
```
Ejemplo:

```bash
git checkout -b feature/cors-fix
```
### 3. Guardar cambios
   Agregar cambios:

```bash
git add .
```

Crear commit:

```bash
git commit -m "fix: configuración CORS para frontend dockerizado"
```
### 4. Subir rama al repositorio remoto
```bash
git push -u origin feature/nombre-feature
```
Ejemplo:

```bash
git push -u origin feature/cors-fix
```
### 5. Realizar Merge hacia main
Ir al repositorio en github y presionar "Compare & pull request"
1. Crear Pull Request desde `feature/nombre-feature` hacia `main`
2. Revisar cambios
3. Realizar merge
### 6. Eliminar rama feature
Eliminar rama local:

```bash
git branch -d feature/nombre-feature
```

Eliminar rama remota:

```bash
git push origin --delete feature/nombre-feature
```
## Estructura de ramas utilizada

| Tipo de rama | Propósito |
|---|---|
| `main` | Rama principal estable |
| `feature/*` | Nuevas funcionalidades o mejoras |
| `fix/*` | Corrección de errores |
| `hotfix/*` | Correcciones urgentes en producción |
---