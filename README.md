# Gestor de Cámaras Multimedia (GestorCamaras)

[![Java Version](https://img.shields.io/badge/Java-17-orange.svg?style=flat-square)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg?style=flat-square)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/MySQL-8.0-blue.svg?style=flat-square)](https://www.mysql.com/)
[![Cache](https://img.shields.io/badge/Redis-5.0+-red.svg?style=flat-square)](https://redis.io/)
[![Build](https://img.shields.io/badge/Build-Maven-blueviolet.svg?style=flat-square)](https://maven.apache.org/)

Plataforma empresarial integrada para la administración de dispositivos de videovigilancia, captura multimedia, procesamiento digital de imágenes y monitoreo en tiempo real. El sistema combina una arquitectura cliente-servidor distribuida compuesta por un **Backend Spring Boot** (API REST + Servidor Web) y un **Cliente de Escritorio Swing** (procesamiento local con OpenCV y sincronización remota).

---
##  Proyecto con objetivos académicos
##  Arquitectura del Sistema

El sistema implementa una arquitectura desacoplada donde el cliente y el servidor comparten un contrato de datos unificado para evitar duplicaciones y dependencias cruzadas.

```
                  ╔══════════════════════════════════════╗
                  ║           Cliente Swing              ║
                  ║        (Módulo Escritorio)           ║
                  ║  - Captura y filtros con OpenCV      ║
                  ║  - Conexión por WebSocket/STOMP      ║
                  ╚══════════════════════════════════════╝
                                    │▲
                    HTTP REST       ││  Notificaciones STOMP
                    JSON (DTOs)     ▼│  (Tiempo Real)
                  ╔══════════════════════════════════════╗
                  ║         Servidor Spring Boot         ║
                  ║  - API REST & Gestión de Seguridad   ║
                  ║  - Capa de Negocio (Service/JPA)     ║
                  ║  - Consola Web Thymeleaf (:8080)     ║
                  ╚══════════════════════════════════════╝
                                    │▲
                         Consultas  ││  Caché de
                         SQL        ▼│  Estados (Jedis)
                  ╔══════════════════╗    ╔══════════════════╗
                  ║ Base de Datos    ║    ║ Servidor Redis   ║
                  ║ MySQL            ║    ║ (Estado Equipos) ║
                  ╚══════════════════╝    ╚══════════════════╝
```

---

##  Funcionalidades Clave

* **Monitoreo Distribuido**: Sincronización en tiempo real del estado de los equipos y cámaras utilizando WebSockets (STOMP).
* **Procesamiento de Imágenes**: Aplicación de filtros digitales sobre capturas de cámaras de forma local y remota usando la biblioteca nativa OpenCV.
* **Optimización de Memoria (Object Pool)**: Reutilización de filtros pesados de imagen a través del patrón Object Pool para reducir el consumo y la recolección de basura.
* **Consolidación de Contratos**: Definición única de `ArchivoMultimediaDTO` compartida entre el Backend y el Cliente Swing (evitando la inversión de dependencias).
* **Seguridad y Auditoría**: Autenticación centralizada por JWT (JSON Web Tokens) y control de accesos basados en roles.
* **Generación de Informes**: Empleo del patrón Builder para estructurar reportes multimedia complejos de forma dinámica.

---

##  Stack Tecnológico

| Componente | Tecnología | Versión / Descripción |
|---|---|---|
| **Lenguaje** | Java | 17 LTS |
| **Framework Base** | Spring Boot | 3.2.5 (Starter Web, Data JPA, Security) |
| **BBDD Relacional** | MySQL | Conector J y Dialectos Hibernate |
| **BBDD en Memoria** | H2 Database | Empleado de forma aislada para la fase de tests |
| **Caché y Mensajería** | Redis / Jedis | Almacenamiento rápido de estados y colas |
| **Procesamiento Visual** | OpenCV | Enlace Java por medio de Bytedeco (4.5.5-1.5.7) |
| **Comunicación Síncrona** | Spring REST | Controladores REST con mapeo seguro |
| **Comunicación Asíncrona** | Spring WebSocket | Conexión STOMP + SockJS |
| **Frontend Web Admin** | Thymeleaf | Motor de plantillas HTML5 + Bootstrap |
| **Cliente de Escritorio** | Java Swing | Aplicación interactiva nativa con hilos dedicados |

---

##  Requisitos Previos

* **Java Development Kit (JDK)** versión 17 o superior instalado.
* **Base de Datos MySQL** corriendo en `localhost:3306` (esquema: `gestor_camaras`).
* **Servidor Redis** activo en `localhost:6379`.
* *(Opcional)* PowerShell con permisos de administrador para ejecutar scripts automatizados.

> [!TIP]
> Si está en entorno Windows, puede utilizar el script `.\start-redis.ps1` en la raíz del proyecto para descargar, instalar como servicio y levantar Redis automáticamente.

---

##  Instalación y Configuración

### 1. Configuración de Base de Datos
Crea una base de datos en tu servidor MySQL:
```sql
CREATE DATABASE gestor_camaras CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configuración de Propiedades
Verifica y edita las credenciales en `src/main/resources/application.properties` si es necesario:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestor_camaras?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contrasena
```

---

##  Ejecución del Proyecto

### Iniciar el Servidor Backend
Compila el proyecto y pon en marcha la aplicación Spring Boot usando el Maven Wrapper provisto:
```bash
# En Windows (PowerShell/CMD)
.\mvnw.cmd spring-boot:run

# En Linux/macOS
./mvnw spring-boot:run
```

La consola de administración estará disponible en: **`http://localhost:8080`**

### Iniciar el Cliente de Escritorio Swing
Con el servidor backend ejecutándose, arranca el cliente de escritorio iniciando la clase principal del entorno gráfico:
```bash
# Ejecutar desde tu IDE preferido o mediante línea de comandos apuntando a:
com.example.gestorcamaras.Escritorio.CamaraFrame
```

### Cuentas de Acceso por Defecto (DataInitializer)
Al iniciarse por primera vez, el sistema autogenera las siguientes credenciales para pruebas:

| Rol de Usuario | Correo Electrónico | Contraseña |
|---|---|---|
| **Administrador** | `admin@gestor.com` | `admin123` |
| **Operador** | `oper@gestor.com` | `oper123` |

---

##  Pruebas Unitarias e Integración

Para validar la correcta implementación y consistencia de los modelos y mappers sin depender de infraestructura externa (MySQL/Redis), la suite de pruebas del proyecto utiliza una base de datos **H2 en memoria** con un dialecto compatible con MySQL y deshabilita la persistencia en caché.

Ejecuta las pruebas en cualquier entorno ejecutando:
```bash
.\mvnw.cmd clean test
```

---

##  Estructura del Código

```text
src/main/java/com/example/gestorcamaras/
├── Escritorio/       # Cliente Swing (UI, Servicios de red, Clientes WebSocket)
├── builder/          # Patrón Builder para exportación y estructuración de informes
├── config/           # Configuraciones de WebSocket, Jackson, MVC y Seguridad
├── controller/       # Controladores de la API REST
├── dto/              # DTOs compartidos (contrato unificado cliente/servidor) y Mappers
├── filtros/          # Algoritmos y lógica de procesamiento de filtros OpenCV
├── model/            # Entidades JPA (Usuario, Cámara, Equipo, Rol, Archivo)
├── pool/             # Patrón Object Pool para el reciclaje de filtros de imagen
├── redis/            # Capa de integración y caché con Redis (Jedis)
├── repository/       # Repositorios Spring Data JPA
└── service/          # Servicios y lógica de negocio (Interfaces e Impl)
```

---

##  Patrones de Diseño Implementados

* **Object Pool (Filtros)**: El procesamiento de imágenes con OpenCV requiere instanciar objetos pesados en memoria. Implementamos un Pool que almacena filtros pre-inicializados para evitar sobrecargar el recolector de basura (GC) y optimizar la CPU durante la captura de frames.
* **Builder (Informes)**: La exportación de reportes multimedia puede variar en campos y estructura. El patrón Builder permite configurar paso a paso el formato del informe sin sobrecargar constructores.
* **Mapper**: Centralización de la lógica de transformación entidad-DTO en `ArchivoMultimediaMapper`. Garantiza un mapeo manual explícito sin dependencias de terceros y con control de nulos en las relaciones (`CamaraId`, `EquipoId`).
