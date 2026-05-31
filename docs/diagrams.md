# Emotion Diary Server — Diagramas de arquitectura

Modelo de base de datos y estructura en capas.

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

### Flujo de petición (ejemplo)

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
