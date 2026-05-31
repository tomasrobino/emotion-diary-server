# Emotion Diary Server — Architecture Diagrams

Database model and layered application structure for the Spring Boot backend.

Open this file in Markdown preview (VS Code / Cursor / GitHub) to render the Mermaid diagrams.

---

## Entity Relationship Diagram

Seven JPA entities. All user FKs reference `users.username` (not `users.id`). Associations are unidirectional `@ManyToOne(LAZY)`.

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

    users ||--o{ moodboard : "owns (owner_username)"
    users ||--|{ diary_entry : "owns (owner_username)"
    users ||--o{ moodboard_likes : "likes (liker_username)"
    users ||--o{ moodboard_permissions : "grants (owner_username)"
    users ||--o{ moodboard_permissions : "receives (permitted_username)"
    moodboard ||--o{ moodboard_media : "contains"
    moodboard ||--o{ moodboard_likes : "liked_by"
    moodboard ||--o{ moodboard_permissions : "shared_via"
    moodboard ||--o| diary_entry : "linked_to (optional)"
```

### Constraints and delete behavior

| Rule | Detail |
|------|--------|
| Diary uniqueness | One entry per user per day (`owner_username` + `entry_date`) |
| Like uniqueness | One like per user per moodboard (`moodboard_id` + `liker_username`) |
| Permission uniqueness | One grant per user per moodboard (`moodboard_id` + `permitted_username`) |
| Delete user | RESTRICT while referenced by moodboards, diary entries, likes, or permissions |
| Delete moodboard | CASCADE to media, likes, permissions; SET NULL on `diary_entry.linked_moodboard_id` |
| RevokedToken | Standalone table — no FK to `users` |

---

## Class Diagram

Layered Spring Boot architecture (~27 key classes). Controllers delegate to services; services use repositories to access entities.

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
    }
    class MoodboardRepository {
        <<interface>>
    }
    class MoodboardMediaRepository {
        <<interface>>
    }
    class MoodboardLikeRepository {
        <<interface>>
    }
    class MoodboardPermissionRepository {
        <<interface>>
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

    namespace controller {
        class AuthController
        class DiaryController
        class MoodboardController
        class PublicMoodboardController
        class UserController
        class ProfileController
        class MetricsController
        class GlobalExceptionHandler
    }

    namespace service {
        class AuthService
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

    namespace repository {
        class UserRepository
        class DiaryEntryRepository
        class MoodboardRepository
        class MoodboardMediaRepository
        class MoodboardLikeRepository
        class MoodboardPermissionRepository
        class RevokedTokenRepository
    }

    namespace model {
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

    UserRepository ..> User
    DiaryEntryRepository ..> DiaryEntry
    MoodboardRepository ..> Moodboard
    MoodboardMediaRepository ..> MoodboardMedia
    MoodboardLikeRepository ..> MoodboardLike
    MoodboardPermissionRepository ..> MoodboardPermission
    RevokedTokenRepository ..> RevokedToken

    DiaryEntry --> User : owner
    DiaryEntry --> Moodboard : linkedMoodboard
    Moodboard --> User : owner
    MoodboardMedia --> Moodboard
    MoodboardLike --> Moodboard
    MoodboardLike --> User : liker
    MoodboardPermission --> Moodboard
    MoodboardPermission --> User : owner
    MoodboardPermission --> User : permitted
```

### Architecture notes

- **Layering**: Controllers handle HTTP mapping only; business logic lives in services; persistence goes through Spring Data repositories.
- **EntityReferences**: Shared helper for consistent `requireUser()` / `requireMoodboard()` lookups with validation errors.
- **Security services**: `MoodboardAccessService` (authorization), `MoodboardPermissionService` (grants), and `MoodboardLikeService` (likes) sit alongside domain services.
- **JPA mapping**: Unidirectional `@ManyToOne(LAZY)` — no `@OneToMany` on parent entities.

### Request flow (example)

```mermaid
sequenceDiagram
    participant Client
    participant MoodboardController
    participant MoodboardLikeService
    participant MoodboardAccessService
    participant MoodboardLikeRepository

    Client->>MoodboardController: POST /{user}/moodboards/{id}/likes
    MoodboardController->>MoodboardAccessService: canAccess(id, user, principal)
    MoodboardAccessService-->>MoodboardController: true
    MoodboardController->>MoodboardLikeService: like(moodboard, principal)
    MoodboardLikeService->>MoodboardLikeRepository: save(MoodboardLike)
    MoodboardController-->>Client: 200 OK
```
