# Emotion Diary — Diagramas de arquitectura

Modelo de datos, capas del servidor, casos de uso, despliegue, flujos de actividad y secuencias de petición.

---

## Diagrama entidad-relación

Siete entidades JPA. Todas las FK hacia usuarios referencian `users.username` (no `users.id`). Las asociaciones son unidireccionales `@ManyToOne(LAZY)`.

```mermaid
erDiagram
    users {
        bigint id PK
        varchar username UK
        varchar password
    }

    moodboard {
        bigint id PK
        varchar owner_username FK
        longtext content
        boolean is_public
        varchar name
        blob thumbnail
    }

    diary_entry {
        bigint id PK
        varchar owner_username FK
        date entry_date
        int mood_score
        text text_note
        bigint linked_moodboard_id FK
        datetime reminder_at
        timestamp created_at
        timestamp updated_at
    }

    moodboard_media {
        bigint id PK
        bigint moodboard_id FK
        varchar content_type
        varchar original_filename
        blob data
        bigint size_bytes
        timestamp created_at
    }

    moodboard_likes {
        bigint id PK
        bigint moodboard_id FK
        varchar liker_username FK
    }

    moodboard_permissions {
        bigint id PK
        bigint moodboard_id FK
        varchar owner_username FK
        varchar permitted_username FK
    }

    revoked_tokens {
        varchar jti PK
        timestamp expires_at
    }

    users ||--o{ moodboard : "posee (owner_username)"
    users ||--|{ diary_entry : "posee (owner_username)"
    users ||--o{ moodboard_likes : "da like (liker_username)"
    users ||--o{ moodboard_permissions : "concede (owner_username)"
    users ||--o{ moodboard_permissions : "recibe (permitted_username)"
    moodboard ||--o{ moodboard_media : "contiene"
    moodboard ||--o{ moodboard_likes : "recibe likes"
    moodboard ||--o{ moodboard_permissions : "compartido mediante"
    moodboard ||--o| diary_entry : "vinculado a (opcional)"
```

### Restricciones y comportamiento al eliminar

| Regla | Detalle |
|-------|---------|
| Unicidad del diario | Una entrada por usuario y día (`owner_username` + `entry_date`) |
| Unicidad de likes | Un like por usuario y moodboard (`moodboard_id` + `liker_username`) |
| Unicidad de permisos | Una concesión por usuario y moodboard (`moodboard_id` + `permitted_username`) |
| Eliminar usuario (directo en BD) | RESTRICT mientras existan referencias desde moodboards, entradas del diario, likes o permisos |
| Eliminar cuenta (API) | `UserDeletionService` borra en orden: entradas del diario → moodboards propios → likes del usuario → permisos concedidos al usuario → fila en `users`. Sin cambios de esquema |
| Eliminar moodboard | CASCADE en media, likes y permisos; SET NULL en `diary_entry.linked_moodboard_id` |
| RevokedToken | Tabla independiente — sin FK hacia `users` |

---

## Diagrama de casos de uso

Diez casos de uso agrupados en el sistema E-Diary. Tres actores: invitado (sin sesión), registrado (con JWT) y visitante público (solo lectura de moodboards públicos). Las relaciones `extend` e `include` marcan dependencias entre casos de uso.

```mermaid
flowchart TB
    subgraph actores [Actores]
        Invitado((Usuario invitado))
        Registrado((Usuario registrado))
        Visitante((Visitante publico))
    end

    subgraph sistema [Sistema E-Diary]
        CU01[CU-01 Registrarse]
        CU02[CU-02 Iniciar sesion]
        CU03[CU-03 Crear y editar moodboard]
        CU04[CU-04 Compartir moodboard]
        CU05[CU-05 Explorar y dar like]
        CU06[CU-06 Registrar entrada diario]
        CU07[CU-07 Consultar metricas]
        CU08[CU-08 Gestionar cuenta]
        CU09[CU-09 Exportar imagen PNG]
        CU10[CU-10 Ver moodboard publico]
    end

    Invitado --> CU01
    Invitado --> CU02
    Invitado --> CU10

    Registrado --> CU02
    Registrado --> CU03
    Registrado --> CU04
    Registrado --> CU05
    Registrado --> CU06
    Registrado --> CU07
    Registrado --> CU08
    Registrado --> CU09

    Visitante --> CU10
    Visitante --> CU09

    CU01 -.->|extend| CU02
    CU03 -.->|include| CU04
    CU06 -.->|include| CU07
```

| Caso de uso | Descripción breve |
|-------------|-------------------|
| CU-01 / CU-02 | Registro (`POST /auth/register`) e inicio de sesión (`POST /auth/login`) con JWT |
| CU-03 | Editor Fabric.js: crear, actualizar contenido JSON y subir media al moodboard |
| CU-04 | Conceder o revocar permisos por usuario, o marcar el moodboard como público |
| CU-05 | Feed público en Explore y likes sobre moodboards accesibles |
| CU-06 | Entrada diaria con puntuación de ánimo, nota y moodboard vinculado opcional |
| CU-07 | Métricas agregadas (media, racha, tendencia) sobre un periodo configurable |
| CU-08 | Cambio de contraseña y eliminación de cuenta con confirmación |
| CU-09 | Exportación PNG en el cliente (`canvasExport.exportCanvasToBlob`) |
| CU-10 | Lectura de moodboards públicos sin autenticación |

---

## Diagrama de clases

Arquitectura Spring Boot en capas (~28 clases principales). Los controladores delegan en servicios; los servicios usan repositorios para acceder a las entidades.

```mermaid
classDiagram
    direction TB

    class AuthController {
        +register()
        +login()
        +logout()
    }
    class DiaryController {
        +listEntries()
        +getEntry()
        +upsertEntry()
        +deleteEntry()
    }
    class MoodboardController {
        +getMoodboards()
        +createMoodboard()
        +grantAccess()
        +likeMoodboard()
    }
    class PublicMoodboardController {
        +getPublicMoodboards()
    }
    class UserController {
        +searchUsers()
    }
    class ProfileController {
        +changePassword()
        +deleteAccount()
    }
    class MetricsController {
        +getMetrics()
    }
    class GlobalExceptionHandler {
        +handleValidation()
        +handleAuth()
    }

    class AuthService {
        +register()
        +logout()
        +changePassword()
        +deleteAccount()
    }
    class UserDeletionService {
        +deleteAccount()
    }
    class TokenRevocationService {
        +revoke()
        +isRevoked()
    }
    class UserService {
        +loadUserByUsername()
        +searchUsersByPrefix()
        +existsByUsernameIgnoreCase()
    }
    class DiaryEntryService {
        +findInRange()
        +findByDate()
        +upsert()
        +deleteByDate()
    }
    class MoodboardService {
        +save()
        +findById()
        +update()
        +deleteById()
    }
    class MoodboardContentService {
        +serialize()
        +deserialize()
        +validate()
    }
    class MoodboardMediaService {
        +upload()
        +findByIdAndMoodboardId()
        +deleteByIdAndMoodboardId()
    }
    class MoodboardLikeService {
        +like()
        +unlike()
        +countByMoodboardId()
        +getLikedMoodboards()
    }
    class MoodboardPermissionService {
        +grantAccess()
        +revokeAccess()
        +listPermittedUsernames()
    }
    class MoodboardAccessService {
        +canAccess()
    }
    class MetricsService {
        +computeMetrics()
    }
    class EntityReferences {
        +requireUser()
        +requireMoodboard()
    }

    class UserRepository {
        <<interface>>
    }
    class DiaryEntryRepository {
        <<interface>>
        +deleteByOwner_Username()
    }
    class MoodboardRepository {
        <<interface>>
    }
    class MoodboardMediaRepository {
        <<interface>>
    }
    class MoodboardLikeRepository {
        <<interface>>
        +deleteByLiker_Username()
    }
    class MoodboardPermissionRepository {
        <<interface>>
        +deleteByPermitted_Username()
    }
    class RevokedTokenRepository {
        <<interface>>
    }

    class User {
        +id
        +username
        +password
    }
    class Moodboard {
        +id
        +content
        +isPublic
        +name
    }
    class DiaryEntry {
        +id
        +entryDate
        +moodScore
        +textNote
    }
    class MoodboardMedia {
        +id
        +contentType
        +data
    }
    class MoodboardLike {
        +id
    }
    class MoodboardPermission {
        +id
    }
    class RevokedToken {
        +jti
        +expiresAt
    }

    namespace controlador {
        class AuthController
        class DiaryController
        class MoodboardController
        class PublicMoodboardController
        class UserController
        class ProfileController
        class MetricsController
        class GlobalExceptionHandler
    }

    namespace servicio {
        class AuthService
        class UserDeletionService
        class TokenRevocationService
        class UserService
        class DiaryEntryService
        class MoodboardService
        class MoodboardContentService
        class MoodboardMediaService
        class MoodboardLikeService
        class MoodboardPermissionService
        class MoodboardAccessService
        class MetricsService
        class EntityReferences
    }

    namespace repositorio {
        class UserRepository
        class DiaryEntryRepository
        class MoodboardRepository
        class MoodboardMediaRepository
        class MoodboardLikeRepository
        class MoodboardPermissionRepository
        class RevokedTokenRepository
    }

    namespace modelo {
        class User
        class Moodboard
        class DiaryEntry
        class MoodboardMedia
        class MoodboardLike
        class MoodboardPermission
        class RevokedToken
    }

    AuthController --> AuthService
    DiaryController --> DiaryEntryService
    MoodboardController --> MoodboardService
    MoodboardController --> MoodboardContentService
    MoodboardController --> MoodboardMediaService
    MoodboardController --> MoodboardLikeService
    MoodboardController --> MoodboardPermissionService
    MoodboardController --> MoodboardAccessService
    PublicMoodboardController --> MoodboardService
    PublicMoodboardController --> MoodboardLikeService
    UserController --> UserService
    ProfileController --> AuthService
    MetricsController --> MetricsService

    AuthService --> UserRepository
    AuthService --> UserService
    AuthService --> UserDeletionService
    UserDeletionService --> UserRepository
    UserDeletionService --> DiaryEntryRepository
    UserDeletionService --> MoodboardService
    UserDeletionService --> MoodboardLikeRepository
    UserDeletionService --> MoodboardPermissionRepository
    UserDeletionService --> TokenRevocationService
    AuthService --> TokenRevocationService
    UserService --> UserRepository
    DiaryEntryService --> DiaryEntryRepository
    DiaryEntryService --> EntityReferences
    MoodboardService --> MoodboardRepository
    MoodboardContentService --> MoodboardMediaRepository
    MoodboardMediaService --> MoodboardMediaRepository
    MoodboardLikeService --> MoodboardLikeRepository
    MoodboardLikeService --> EntityReferences
    MoodboardPermissionService --> MoodboardPermissionRepository
    MoodboardPermissionService --> UserService
    MoodboardPermissionService --> EntityReferences
    MoodboardAccessService --> MoodboardPermissionRepository
    MoodboardAccessService --> MoodboardRepository
    MetricsService --> DiaryEntryRepository
    EntityReferences --> UserRepository
    EntityReferences --> MoodboardRepository
    TokenRevocationService --> RevokedTokenRepository

    UserRepository ..> User
    DiaryEntryRepository ..> DiaryEntry
    MoodboardRepository ..> Moodboard
    MoodboardMediaRepository ..> MoodboardMedia
    MoodboardLikeRepository ..> MoodboardLike
    MoodboardPermissionRepository ..> MoodboardPermission
    RevokedTokenRepository ..> RevokedToken

    DiaryEntry --> User : propietario
    DiaryEntry --> Moodboard : moodboardVinculado
    Moodboard --> User : propietario
    MoodboardMedia --> Moodboard
    MoodboardLike --> Moodboard
    MoodboardLike --> User : usuarioQueDaLike
    MoodboardPermission --> Moodboard
    MoodboardPermission --> User : propietario
    MoodboardPermission --> User : autorizado
```

### Notas de arquitectura

- **Capas**: Los controladores solo mapean HTTP; la lógica de negocio está en los servicios; la persistencia pasa por repositorios Spring Data.
- **EntityReferences**: Helper compartido para búsquedas consistentes con `requireUser()` / `requireMoodboard()` y errores de validación.
- **Servicios de seguridad**: `MoodboardAccessService` (autorización), `MoodboardPermissionService` (concesiones) y `MoodboardLikeService` (likes) conviven con los servicios de dominio.
- **Mapeo JPA**: `@ManyToOne(LAZY)` unidireccional — sin `@OneToMany` en las entidades padre.
- **Eliminar cuenta**: cascada a nivel de aplicación (`UserDeletionService`), no `ON DELETE CASCADE` en BD.
- **Exportar moodboard**: descarga PNG en el cliente vía `canvasExport.exportCanvasToBlob` (restablece viewport, calcula bounds de todo el contenido y exporta con `toDataURL`); la miniatura al guardar usa el mismo helper con escala JPEG reducida.

---

## Diagrama de despliegue

Tres contenedores Docker en el host: front (nginx sirve la SPA React), API Spring Boot y MariaDB. El navegador carga la UI por HTTP (puerto 80) y llama a la API REST en el puerto 8080 con cabecera `Authorization: Bearer`.

```mermaid
flowchart LR
    subgraph cliente [Cliente]
        Browser[Navegador web]
    end

    subgraph host [Servidor / Docker Host]
        subgraph front_container [Contenedor front nginx:80]
            Nginx[nginx]
            Static[React SPA estatica]
            Nginx --> Static
        end

        subgraph app_container [Contenedor spring_app:8080]
            Spring[Spring Boot API]
            JWT[JWT Filter]
            Spring --> JWT
        end

        subgraph db_container [Contenedor mariadb_db:3306]
            MariaDB[(MariaDB 11)]
        end
    end

    Browser -->|HTTP puerto 80| Nginx
    Browser -->|REST JSON puerto 8080 Bearer JWT| Spring
    Spring -->|JDBC| MariaDB
```

En desarrollo, el front puede ejecutarse con Vite (`5173`) apuntando a `VITE_API_URL=http://localhost:8080` mientras el backend y la base de datos corren con `docker compose` del servidor.

---

## Diagramas de actividad

Flujos de alto nivel por caso de uso. Complementan los diagramas de secuencia con la lógica de decisión visible para el usuario.

### CU-01 / CU-02 — Registro e inicio de sesión

Tras validar credenciales, el servidor devuelve un JWT que el cliente guarda en sesión local.

```mermaid
flowchart LR
    A([Inicio]) --> B{Registro o login?}
    B -->|Registro| C[Enviar username y password]
    B -->|Login| C
    C --> D{Datos validos?}
    D -->|No| E[Mostrar error]
    E --> C
    D -->|Si| F[Crear usuario o verificar credenciales]
    F --> G[Generar JWT]
    G --> H[Guardar token y entrar al dashboard]
    H --> I([Fin])
```

### CU-03 — Crear y editar moodboard

El contenido del lienzo se serializa a JSON; las imágenes incrustadas se suben como blobs en `moodboard_media`. Al guardar, el cliente genera y envía una miniatura JPEG.

```mermaid
flowchart LR
    A([Inicio]) --> B[Editar lienzo]
    B --> C{Subir imagen?}
    C -->|Si| D[Subir archivo al servidor]
    D --> B
    C -->|No| E[Guardar moodboard]
    E --> F{Es nuevo?}
    F -->|Si| G[Crear en base de datos]
    F -->|No| H[Actualizar en base de datos]
    G --> I[Guardar miniatura]
    H --> I
    I --> J([Fin])
```

### CU-04 — Compartir moodboard

Solo el propietario puede conceder acceso a otro usuario, revocarlo o cambiar la visibilidad pública.

```mermaid
flowchart LR
    A([Inicio]) --> B[Abrir opciones de compartir]
    B --> C{Que accion?}
    C -->|Conceder| D[Buscar y seleccionar usuario]
    D --> E[Registrar permiso]
    C -->|Revocar| F[Quitar permiso de usuario]
    C -->|Publicar| G[Marcar moodboard como publico]
    E --> H([Fin])
    F --> H
    G --> H
```

### CU-05 — Comprobar acceso a moodboard

`MoodboardAccessService.canAccess` evalúa propietario, flag `is_public` y fila en `moodboard_permissions`. Se invoca antes de servir contenido, dar like o listar moodboards ajenos.

```mermaid
flowchart LR
    A([Solicitud de acceso]) --> B{Es el propietario?}
    B -->|Si| OK([Acceso permitido])
    B -->|No| C{Es publico?}
    C -->|Si| OK
    C -->|No| D{Tiene permiso?}
    D -->|Si| OK
    D -->|No| DENY([Acceso denegado])
```

### CU-06 — Registrar entrada del diario

Una entrada por usuario y día. El moodboard vinculado es opcional y debe pertenecer al mismo usuario.

```mermaid
flowchart LR
    A([Inicio]) --> B[Seleccionar fecha]
    B --> C[Introducir mood y nota]
    C --> D{Vincular moodboard?}
    D -->|Si| E[Elegir moodboard]
    D -->|No| F[Guardar]
    E --> F
    F --> G{Datos validos?}
    G -->|No| H[Mostrar error]
    H --> C
    G -->|Si| I[Persistir entrada]
    I --> J[Actualizar calendario]
    J --> K([Fin])
```

### CU-07 — Consultar métricas

El periodo (`7d`, `30d`, `90d`) filtra entradas del diario; el servicio calcula media de ánimo, racha de días consecutivos y tendencia reciente.

```mermaid
flowchart LR
    A([Inicio]) --> B[Elegir periodo 7d / 30d / 90d]
    B --> C[Consultar entradas del diario]
    C --> D[Calcular media, racha y tendencia]
    D --> E[Mostrar grafico y resumen]
    E --> F{Cambiar periodo?}
    F -->|Si| B
    F -->|No| G([Fin])
```

### CU-08 — Eliminar cuenta

Requiere la contraseña actual. `UserDeletionService` borra datos dependientes en orden antes de eliminar la fila de `users` y revocar el JWT activo.

```mermaid
flowchart LR
    A([Inicio]) --> B[Confirmar con password]
    B --> C{Password correcta?}
    C -->|No| D[Mostrar error]
    D --> B
    C -->|Si| E[Borrar diario y moodboards]
    E --> F[Borrar likes y permisos]
    F --> G[Revocar sesion y eliminar usuario]
    G --> H[Redirigir a login]
    H --> I([Fin])
```

---

## Diagramas de secuencia

Interacciones HTTP entre cliente y capas del servidor (y flujo cliente-only para exportación PNG).

### Flujo de petición — autenticación

```mermaid
sequenceDiagram
    participant Cliente
    participant AuthController
    participant AuthService
    participant AuthenticationManager
    participant JwtService
    participant UserRepository

    alt Registro
        Cliente->>AuthController: POST /auth/register
        AuthController->>AuthService: register(username, password)
        AuthService->>UserRepository: save(User)
        AuthService->>JwtService: generateToken(username)
        AuthController-->>Cliente: 201 + JWT
    else Login
        Cliente->>AuthController: POST /auth/login
        AuthController->>AuthenticationManager: authenticate(...)
        AuthController->>JwtService: generateToken(username)
        AuthController-->>Cliente: 200 + JWT
    end
```

### Flujo de petición — guardar moodboard

```mermaid
sequenceDiagram
    participant Cliente
    participant MoodboardController
    participant MoodboardContentService
    participant MoodboardService
    participant MoodboardMediaService

    Cliente->>MoodboardController: POST /{user}/moodboards (crear)
    MoodboardController->>MoodboardContentService: validateForCreate + serialize
    MoodboardController->>MoodboardService: save(Moodboard)
    MoodboardController-->>Cliente: 201 Created

    Note over Cliente,MoodboardMediaService: Edición posterior
    Cliente->>MoodboardController: PUT /{user}/moodboards/{id}
    MoodboardController->>MoodboardContentService: validate + serialize
    MoodboardController->>MoodboardService: update(Moodboard)
    Cliente->>MoodboardController: PUT .../thumbnail (JPEG)
    Cliente->>MoodboardController: POST .../media (multipart)
    MoodboardController->>MoodboardMediaService: upload(...)
    MoodboardController-->>Cliente: 200 OK
```

### Flujo de petición — compartir moodboard

```mermaid
sequenceDiagram
    participant Cliente
    participant MoodboardController
    participant MoodboardPermissionService
    participant UserService

    Cliente->>MoodboardController: POST .../permissions?grantTo={user}
    MoodboardController->>MoodboardPermissionService: grantAccess(moodboard, owner, grantTo)
    MoodboardPermissionService->>UserService: existsByUsernameIgnoreCase(grantTo)
    MoodboardPermissionService->>MoodboardPermissionService: save(MoodboardPermission)
    MoodboardController-->>Cliente: 200 OK
```

### Flujo de petición — comprobar acceso

```mermaid
sequenceDiagram
    participant Cliente
    participant MoodboardController
    participant MoodboardAccessService
    participant MoodboardRepository
    participant MoodboardPermissionRepository

    Cliente->>MoodboardController: GET /{user}/moodboards/{id}
    MoodboardController->>MoodboardAccessService: canAccess(id, owner, principal)
    alt Es propietario
        MoodboardAccessService-->>MoodboardController: true
    else Moodboard publico
        MoodboardAccessService->>MoodboardRepository: findById
        MoodboardAccessService-->>MoodboardController: true
    else Permiso explicito
        MoodboardAccessService->>MoodboardPermissionRepository: existsBy...
        MoodboardAccessService-->>MoodboardController: true / false
    end
    MoodboardController-->>Cliente: 200 OK o 404
```

### Flujo de petición — métricas

```mermaid
sequenceDiagram
    participant Cliente
    participant MetricsController
    participant MetricsService
    participant DiaryEntryRepository

    Cliente->>MetricsController: GET /{user}/metrics?period=30d
    MetricsController->>MetricsService: computeMetrics(user, period)
    MetricsService->>DiaryEntryRepository: findInRange(owner, from, to)
    MetricsService->>MetricsService: calcular media, racha, tendencia
    MetricsController-->>Cliente: 200 MetricsResponseDto
```

### Flujo de petición — eliminar cuenta

```mermaid
sequenceDiagram
    participant Cliente
    participant ProfileController
    participant AuthService
    participant UserDeletionService
    participant Repositorios

    Cliente->>ProfileController: DELETE /{user}/profile
    ProfileController->>AuthService: deleteAccount(user, password, token)
    AuthService->>UserDeletionService: deleteAccount(...)
    UserDeletionService->>Repositorios: borrar diario, moodboards, likes, permisos
    UserDeletionService->>Repositorios: revocar JWT y borrar usuario
    ProfileController-->>Cliente: 204 No Content
```

### Exportar moodboard (cliente)

Sin endpoint de backend. El editor y la vista llaman a `FabricMoodboardEditor.exportImage` → `canvasExport.exportCanvasToBlob` → `downloadBlob`.

Antes de exportar, el helper restablece el viewport (zoom/pan), descarta la selección activa y calcula el rectángulo que engloba el lienzo completo y todos los objetos visibles — incluido contenido fuera del área visible en pantalla.

```mermaid
sequenceDiagram
    participant Usuario
    participant MoodboardPage
    participant FabricMoodboardEditor
    participant CanvasExport
    participant Navegador

    Usuario->>MoodboardPage: Descargar imagen
    MoodboardPage->>FabricMoodboardEditor: exportImageRef
    FabricMoodboardEditor->>CanvasExport: exportCanvasToBlob PNG
    CanvasExport->>CanvasExport: reset viewport + content bounds
    CanvasExport->>CanvasExport: toDataURL
    FabricMoodboardEditor-->>MoodboardPage: Blob
    MoodboardPage->>Navegador: downloadBlob
```

### Flujo de petición — dar like a moodboard

```mermaid
sequenceDiagram
    participant Cliente
    participant MoodboardController
    participant MoodboardLikeService
    participant MoodboardAccessService
    participant MoodboardLikeRepository

    Cliente->>MoodboardController: POST /{user}/moodboards/{id}/likes
    MoodboardController->>MoodboardAccessService: canAccess(id, user, principal)
    MoodboardAccessService-->>MoodboardController: true
    MoodboardController->>MoodboardLikeService: like(moodboard, principal)
    MoodboardLikeService->>MoodboardLikeRepository: save(MoodboardLike)
    MoodboardController-->>Cliente: 200 OK
```
