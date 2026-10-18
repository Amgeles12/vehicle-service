# vehicle-service

Microservicio A del sistema de trazabilidad y perfil de flota. Administra el catálogo de
vehículos: datos técnicos, estado del motor, última ubicación registrada y fecha de mantenimiento.

- **Stack:** Java 21, Spring Boot 4.1.1 (Spring MVC), Spring Data JPA, Lombok
- **Base de datos:** PostgreSQL, base `vehicles` (una base de datos por microservicio)
- **Puerto:** `8081`

## Arquitectura

Arquitectura por capas de `vehicle-service`. Tiene su propia base de datos PostgreSQL y no depende
de ningún otro microservicio.

```mermaid
flowchart TD
    A["Petición HTTP<br/>GET / POST /vehicles"] --> B["VehicleController<br/>recibe y responde"]
    B --> C["ValidateUtil<br/>valida los datos (400), solo en POST"]
    C --> D["VehicleService<br/>reglas de negocio (404 / 409)"]
    D --> E["VehicleRepository<br/>Spring Data JPA"]
    E --> F[("PostgreSQL<br/>tabla vehicle")]
```

## API

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `GET` | `/vehicles` | Lista todos los vehículos | `200` |
| `GET` | `/vehicles/{id}` | Devuelve un vehículo por id | `200`, `404` |
| `POST` | `/vehicles` | Crea un vehículo. El `id` es opcional: si falta se genera uno con formato `veh-xxxxxxxx` | `201` `{ "id": "..." }`, `400`, `409` |

Los errores de negocio se devuelven con esta forma:

```json
{ "message": "Vehicle not found: veh-999", "httpStatus": "NOT_FOUND" }
```

Ejemplo de vehículo:

```json
{
  "id": "veh-001",
  "plate": "ABC-1234",
  "model": "EcoTruck EV",
  "batteryStatusPercentage": 84.5,
  "engineStatus": "OK",
  "lastLatitude": 28.6353,
  "lastLongitude": -106.0889,
  "lastMaintenanceDate": "2026-09-01"
}
```

Reglas de validación y de negocio:

| Regla | Código |
|---|---|
| `plate` y `model` no pueden venir vacíos | `400` |
| `batteryStatusPercentage` debe estar entre 0 y 100 | `400` |
| Ya existe un vehículo con ese `id` | `409` |
| Ya existe un vehículo con esa `plate` | `409` |
| El vehículo solicitado no existe | `404` |

## Cómo ejecutarlo

1. Crea la base de datos en PostgreSQL:

   ```sql
   CREATE DATABASE vehicles;
   ```

2. Crea un archivo `local.properties` en la raíz del proyecto (junto al `pom.xml`) con tu contraseña de PostgreSQL. Este archivo está en `.gitignore` y no se sube al repositorio:

   ```properties
   spring.datasource.password=TU_CONTRASEÑA
   ```

   Por defecto se usa el usuario `postgres` y la URL `jdbc:postgresql://localhost:5432/vehicles`.
   También se pueden cambiar con las variables de entorno `VEHICLE_DB_URL`, `VEHICLE_DB_USER` y `VEHICLE_DB_PASSWORD`.

3. Arranca el servicio. La tabla `vehicle` se crea sola (`ddl-auto=update`):

   ```powershell
   .\mvnw.cmd spring-boot:run      # Windows
   ./mvnw spring-boot:run          # Linux / macOS
   ```

4. Pruebas unitarias de la validación (no necesitan base de datos):

   ```powershell
   .\mvnw.cmd -Dtest=ValidateUtilTest test
   ```

---

## Diagrama de componentes

Arquitectura por capas del microservicio y su relación con el exterior.

```mermaid
flowchart LR
    client["Cliente HTTP<br/>(Postman / otros servicios)"]

    subgraph vs["vehicle-service  -  Spring MVC  -  puerto 8081"]
        direction TB
        controller["VehicleController<br/>@RestController /vehicles"]
        validate["ValidateUtil<br/>valida los datos de entrada"]
        service["VehicleService<br/>reglas de negocio"]
        repo["VehicleRepository<br/>JpaRepository"]
        model["VehicleModel<br/>@Entity tabla vehicle"]
        dto["VehicleDto / VehicleRsDto / ErrorDto<br/>objetos de transferencia"]
        handler["GlobalExceptionHandler<br/>@ControllerAdvice"]
        exception["BusinessException<br/>mensaje + HttpStatus"]
    end

    db[("PostgreSQL<br/>base vehicles")]

    client -->|"REST / JSON"| controller
    controller --> validate
    controller --> service
    service --> repo
    repo --> model
    repo -->|"JDBC / Hibernate"| db
    controller -.->|"usa"| dto
    service -.->|"toDto / toModel"| dto
    validate -. "lanza" .-> exception
    service -. "lanza" .-> exception
    exception -.-> handler
    handler -->|"ErrorDto + código HTTP"| client
```

### Contrato para quien consuma este servicio

Otros servicios consultan `GET /vehicles/{id}` y usan los campos `id`, `plate`, `model` y
`batteryStatusPercentage`. El `id` del vehículo (por ejemplo `veh-001`) es el identificador que
otros servicios guardan para relacionarse con un vehículo.

---

## Diagramas de secuencia

### `GET /vehicles/{id}`: consultar un vehículo

```mermaid
sequenceDiagram
    actor C as Cliente
    participant Ctl as VehicleController
    participant Svc as VehicleService
    participant Repo as VehicleRepository
    participant DB as PostgreSQL
    participant H as GlobalExceptionHandler

    C->>Ctl: GET /vehicles/veh-001
    Ctl->>Svc: findById("veh-001")
    Svc->>Repo: findById("veh-001")
    Repo->>DB: SELECT * FROM vehicle WHERE id = ?
    DB-->>Repo: fila (o ninguna)
    alt el vehículo existe
        Repo-->>Svc: Optional con VehicleModel
        Svc->>Svc: toDto()
        Svc-->>Ctl: VehicleDto
        Ctl-->>C: 200 OK + VehicleDto
    else el vehículo no existe
        Repo-->>Svc: Optional vacío
        Svc-->>H: BusinessException (404)
        H-->>C: 404 Not Found + ErrorDto
    end
```

### `GET /vehicles`: listar vehículos

```mermaid
sequenceDiagram
    actor C as Cliente
    participant Ctl as VehicleController
    participant Svc as VehicleService
    participant Repo as VehicleRepository
    participant DB as PostgreSQL

    C->>Ctl: GET /vehicles
    Ctl->>Svc: findAll()
    Svc->>Repo: findAll()
    Repo->>DB: SELECT * FROM vehicle
    DB-->>Repo: filas
    Repo-->>Svc: lista de VehicleModel
    Svc->>Svc: map(toDto)
    Svc-->>Ctl: lista de VehicleDto
    Ctl-->>C: 200 OK + lista JSON
```

### `POST /vehicles`: crear un vehículo

```mermaid
sequenceDiagram
    actor C as Cliente
    participant Ctl as VehicleController
    participant V as ValidateUtil
    participant Svc as VehicleService
    participant Repo as VehicleRepository
    participant DB as PostgreSQL
    participant H as GlobalExceptionHandler

    C->>Ctl: POST /vehicles (VehicleDto en JSON)
    Ctl->>V: validateRequest(dto)
    alt datos inválidos (placa o modelo vacíos, batería fuera de 0-100)
        V-->>H: BusinessException (400)
        H-->>C: 400 Bad Request + ErrorDto
    else datos válidos
        Ctl->>Svc: create(dto)
        opt el dto no trae id
            Svc->>Svc: genera id con prefijo veh-
        end
        opt el dto trae id
            Svc->>Repo: existsById(id)
            Repo->>DB: SELECT
            DB-->>Repo: resultado
        end
        Svc->>Repo: existsByPlate(plate)
        Repo->>DB: SELECT
        DB-->>Repo: resultado
        alt id o placa repetidos
            Svc-->>H: BusinessException (409)
            H-->>C: 409 Conflict + ErrorDto
        else disponible
            Svc->>Repo: save(dto.toModel())
            Repo->>DB: INSERT INTO vehicle
            DB-->>Repo: fila guardada
            Repo-->>Svc: VehicleModel
            Svc-->>Ctl: VehicleRsDto con el id
            Ctl-->>C: 201 Created + id
        end
    end
```

---

## Estructura del proyecto

```text
src/main/java/com/vehicle/vehicle/
├── VehicleApplication.java
├── constants/Constants.java
├── controller/VehicleController.java
├── dto/
│   ├── VehicleDto.java
│   ├── VehicleRsDto.java
│   └── ErrorDto.java
├── exception/
│   ├── BusinessException.java
│   └── handler/GlobalExceptionHandler.java
├── model/VehicleModel.java
├── repository/VehicleRepository.java
├── service/VehicleService.java
└── util/ValidateUtil.java

src/test/java/com/vehicle/vehicle/
├── VehicleApplicationTests.java
└── util/ValidateUtilTest.java
```

| Capa | Responsabilidad |
|---|---|
| `controller` | Recibe la petición HTTP y delega; no contiene lógica de negocio |
| `util` | Valida que los datos de entrada sean correctos (400) |
| `service` | Reglas de negocio: genera ids, evita duplicados (409) y convierte entre entidad y DTO |
| `repository` | Acceso a PostgreSQL mediante Spring Data JPA |
| `model` | Entidad JPA que representa la tabla `vehicle` |
| `dto` | Objetos que viajan por HTTP, separados de la entidad |
| `exception` | Errores de negocio y su conversión a respuesta JSON |