# PickleReserve

PickleReserve is a pickleball court and coaching reservation system for SJSU CMPE 172. Milestone 1 delivered the layered Spring Boot and SQLite skeleton. Milestone 2 added the complete booking workflow, authentication, role-based access, explicit transactions, and concurrency protection. Milestone 3 adds a resilient HTTP notification boundary, post-commit asynchronous delivery, logging, database health, Micrometer metrics, Docker packaging, and continuous integration.

## Milestone 1 scope

- Java 21 and Spring Boot 3.3.13
- Controller -> Service -> Repository -> SQLite request flow
- Exactly five domain tables: `users`, `providers`, `services`, `availability_slots`, and `appointments`
- Startup-safe `schema.sql` and idempotent `seed.sql`
- Database-enforced double-booking guard: `appointments.slot_id` is unique
- `GET /` home summary and `GET /slots` availability endpoint
- DTO-only JSON responses, filters, SQL pagination, validation, and stable errors
- Integration tests for both endpoints, validation, foreign keys, and the unique slot guard

At Milestone 1, booking, login/RBAC, cancellation, provider writes, notifications, observability, deployment, and AI were scheduled for later milestones.

## Milestone 2 scope

Milestone 2 implements the core booking workflow and concurrency controls required by the Milestone 2 Booking and Concurrency specification.

- Thymeleaf web interface with home, login, slot browsing, booking confirmation, appointment history, and provider dashboard pages
- Username/password login with BCrypt-hashed passwords
- Server-side HTTP session authentication
- Player and Admin/Provider role-based access control
- SQL filtering and pagination using `LIMIT` and `OFFSET`
- Player booking, appointment history, and owner-only cancellation
- Provider availability creation and removal
- Provider view of appointments booked with that provider
- `BOOKED`, `CANCELLED`, and computed `COMPLETED` appointment statuses
- Explicit transactional booking with `SERIALIZABLE` isolation
- Optimistic concurrency control using the slot `version` column
- Database-enforced `UNIQUE(slot_id)` double-booking backstop
- Retry handling for transient SQLite contention
- Global exception handling with stable HTTP status codes and no client-visible stack traces
- Unit, integration, authorization, transaction, and two-thread concurrency tests

The course's Customer role is represented by `PLAYER`. The course's Provider role is represented by an `ADMIN` user connected to a row in the `providers` table.

## Milestone 3 scope

- Coarse-grained `NotificationRequest` with recipient, type, and complete message
- Mock external notification controller at `POST /mock-notifications`
- Stable `Idempotency-Key` header and duplicate suppression in the mock service
- HTTP Gateway with connect/read timeouts, three attempts, incremental backoff, and fallback
- Asynchronous `AFTER_COMMIT` notifications for successful bookings and cancellations
- Structured INFO, WARN, and ERROR operational logging
- `GET /health` with a live `SELECT 1` SQLite connectivity check
- Micrometer counters exposed through Spring Boot Actuator
- Java 21 multi-stage Docker image and persistent SQLite Compose volume
- GitHub Actions workflow that runs `mvn clean verify` on every push and pull request
- Integration test proving the booking remains committed while the notification service is down

## Prerequisites

- JDK 21
- Maven 3.9+

Confirm the active versions:

```bash
java -version
mvn -version
```

No separate database server is required. The SQLite JDBC driver creates `picklereserve.db` in the project directory.

## Milestone 2 database upgrade

Milestone 2 adds the `version` column to `availability_slots`. If a Milestone 1 database already exists, stop the application and delete the old development database once before the first Milestone 2 run.

Windows Command Prompt:

```bat
del picklereserve.db
```

macOS or Linux:

```bash
rm picklereserve.db
```

The next application startup recreates the database from `schema.sql` and loads the idempotent seed data from `seed.sql`.

Do not delete a database that contains data that must be preserved. The deletion instruction is only for the local Milestone 1 development database.

## Build and test

Run the complete test suite:

```bash
mvn clean test
```

The Milestone 2 test suite covers:

- REST endpoint behavior and request validation
- Session authentication and role enforcement
- Rejection of unauthenticated requests
- Rejection of wrong-role requests with HTTP `403`
- Booking service rules
- Owner-only appointment cancellation
- Transactional booking behavior
- Optimistic version conflicts
- SQLite's unique appointment-slot constraint
- Two simultaneous booking attempts for the same slot, with exactly one successful booking

Milestone 3 adds tests for:

- Database-aware health responses
- Actuator metric exposure
- Notification-stub validation and idempotent retry handling
- Booking and cancellation event publication
- Commit-then-notify behavior when the remote notification endpoint is unreachable

## Run

Start the application from the project directory:

```bash
mvn spring-boot:run
```

Open the web application at:

```text
http://localhost:8080/
```

The embedded Tomcat server listens on port `8080` by default.

The application calls its mock external controller through HTTP at `http://localhost:8080` by default. Override `NOTIFICATION_BASE_URL` when the mock controller is hosted separately.

## Configuration

The default datasource URL is:

```text
jdbc:sqlite:picklereserve.db
```

To place the SQLite database elsewhere, set `PICKLERESERVE_DB_URL`.

Windows Command Prompt:

```bat
set PICKLERESERVE_DB_URL=jdbc:sqlite:D:/coding/CMPE172/picklereserve/picklereserve.db
mvn spring-boot:run
```

Windows PowerShell:

```powershell
$env:PICKLERESERVE_DB_URL = "jdbc:sqlite:D:/coding/CMPE172/picklereserve/picklereserve.db"
mvn spring-boot:run
```

macOS or Linux:

```bash
PICKLERESERVE_DB_URL=jdbc:sqlite:/absolute/path/picklereserve.db mvn spring-boot:run
```

SQLite foreign-key enforcement, the busy timeout, and WAL journal mode are configured for every application connection.

Notification gateway settings can be overridden with these properties or environment variables:

| Setting | Default | Purpose |
|---|---|---|
| `NOTIFICATION_ENABLED` | `true` | Enables remote notification delivery |
| `NOTIFICATION_BASE_URL` | `http://localhost:8080` | Base URL for the mock external controller |
| `notification.gateway.connect-timeout` | `500ms` | Maximum TCP connection time |
| `notification.gateway.read-timeout` | `1s` | Maximum response wait |
| `notification.gateway.max-attempts` | `3` | Initial attempt plus bounded retries |
| `notification.gateway.retry-backoff` | `200ms` | Base incremental retry delay |

## Demo accounts

All seeded demo accounts use the password `password`.

| Role | Username | Purpose |
|---|---|---|
| Player | `player.demo` | Browse, book, view, and cancel appointments |
| Player | `player.two` | Second player and concurrency-test account |
| Provider/Admin | `coach.bob` | Manage Bob Kim's availability and appointments |
| Provider/Admin | `coach.tanner` | Manage Tanner Lee's availability and appointments |

Only BCrypt password hashes are stored in `seed.sql`. Plaintext passwords are never stored in the database or written to application logs.

## Web interface

| Route | Access | Purpose |
|---|---|---|
| `GET /` | Public | Home page and current availability summary |
| `GET /login` | Public | Login form |
| `POST /login` | Public | Authenticate and establish the server-side session |
| `POST /logout` | Authenticated | Invalidate the current session |
| `GET /slots` | Public | Browse, filter, and paginate open slots |
| `POST /book` | Player | Book an available slot |
| `GET /confirmation` | Player | Display booking confirmation |
| `GET /appointments` | Player | Display upcoming and historical appointments |
| `POST /appointments/{id}/cancel` | Appointment owner | Cancel an owned booked appointment |
| `GET /provider` | Provider/Admin | Display provider availability and booked appointments |
| `POST /provider/slots` | Provider/Admin | Create an availability slot |
| `POST /provider/slots/{id}/remove` | Owning provider | Remove an eligible open slot |

The Thymeleaf pages are rendered on the server. A page reflects current database state when it is requested or refreshed; the interface does not use live WebSocket updates.

Cancellation changes an appointment from `BOOKED` to `CANCELLED`. The appointment remains visible in history rather than being deleted. Past booked appointments are displayed as `COMPLETED`.

## Frontend-to-backend request flow

A browser request follows the required layered architecture:

```text
Thymeleaf form or page request
        |
        v
Spring MVC Controller
        |
        v
Business Service
        |
        v
JDBC Repository
        |
        v
SQLite database
```

The result returns through the repository and service to the controller. The controller either renders a Thymeleaf template or redirects the browser to the appropriate confirmation, appointment, or provider page.

Spring MVC's `DispatcherServlet` acts as the Front Controller. It receives incoming requests and dispatches them to the appropriate controller method. Controllers handle HTTP mapping, services enforce business rules and transaction boundaries, and repositories contain parameterized hand-written SQL and row mapping.

## REST API

Milestone 2 moves the original JSON endpoints under `/api` because `/` and `/slots` now render Thymeleaf pages.

### Home and availability

- `GET /api/home`
- `GET /api/slots`

`GET /api/slots` accepts these optional query parameters:

| Parameter | Meaning | Default |
|---|---|---|
| `providerId` | Positive provider ID | all providers |
| `serviceId` | Positive service ID | all services |
| `date` | ISO date (`YYYY-MM-DD`) | all dates |
| `page` | Zero-based page number | `0` |
| `size` | Results per page, from 1 through 100 | `20` |

Example requests:

```bash
curl -s http://localhost:8080/api/home
curl -s "http://localhost:8080/api/slots?page=0&size=5"
curl -s "http://localhost:8080/api/slots?providerId=1&serviceId=1&date=2026-10-15"
```

### Authentication

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`

Example login request:

```bash
curl -i -c cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"username":"player.demo","password":"password"}' \
  http://localhost:8080/api/auth/login
```

The returned session cookie must be included in later authenticated API requests.

### Player appointments

- `POST /api/appointments`
- `GET /api/appointments/mine`
- `POST /api/appointments/{id}/cancel`

Example booking request:

```bash
curl -i -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"slotId":1,"serviceId":1}' \
  http://localhost:8080/api/appointments
```

Example appointment-history request:

```bash
curl -s -b cookies.txt http://localhost:8080/api/appointments/mine
```

### Provider operations

- `POST /api/provider/slots`
- `DELETE /api/provider/slots/{id}`
- `GET /api/provider/appointments`

Provider endpoints require an authenticated Provider/Admin session. A Player request is rejected with HTTP `403 Forbidden`.

## Booking transaction and concurrency control

The booking race condition occurs when two players read the same open slot and attempt to reserve it at approximately the same time.

PickleReserve handles this race using three coordinated protections:

1. The booking operation executes inside an explicit `@Transactional` service method using `SERIALIZABLE` isolation.
2. The repository atomically claims the slot with an optimistic version check using the expected `availability_slots.version` value.
3. `appointments.slot_id` has a database-level unique constraint that remains the authoritative double-booking backstop.

The optimistic claim is equivalent to:

```sql
UPDATE availability_slots
   SET version = version + 1
 WHERE id = ?
   AND status = 'OPEN'
   AND version = ?
   AND NOT EXISTS (
       SELECT 1
         FROM appointments
        WHERE slot_id = ?
          AND status = 'BOOKED'
   );
```

Only one concurrent request can update the expected version. The winning transaction inserts the appointment. The losing request receives a deterministic conflict response. Transient SQLite contention is retried a limited number of times.

The automated concurrency test starts two booking attempts against the same slot and asserts that exactly one succeeds.

## ACID properties

- **Atomicity:** Slot validation, optimistic claiming, and appointment insertion succeed or roll back together.
- **Consistency:** Foreign keys, check constraints, service ownership rules, appointment status rules, and the unique slot constraint preserve valid database state.
- **Isolation:** `SERIALIZABLE` transaction isolation and the optimistic version comparison prevent two requests from successfully claiming the same slot version.
- **Durability:** After the transaction commits, SQLite persists the appointment in the database.

## Authentication and authorization

Login looks up the user through JDBC, verifies the submitted password against the stored BCrypt hash, and stores an authenticated-user record in the server-side HTTP session.

The role always comes from the database. The application does not trust a client-supplied role or ownership value.

Authorization rules include:

- Only Players can book appointments.
- Only Players can access their appointment history.
- A Player can cancel only an appointment belonging to that Player.
- Only Provider/Admin users can create and remove availability.
- A Provider can manage only that Provider's services and slots.
- A Provider can view appointments booked with that Provider.
- Unauthenticated protected requests receive HTTP `401`.
- Authenticated wrong-role requests receive HTTP `403`.

## Validation and error handling

Request DTOs and service methods validate identifiers, dates, ownership, roles, slot state, service relationships, and booking eligibility.

Global exception handling returns stable HTTP responses without exposing stack traces:

| Status | Meaning |
|---|---|
| `400 Bad Request` | Invalid input or malformed request |
| `401 Unauthorized` | Login is required or credentials are invalid |
| `403 Forbidden` | Wrong role or ownership violation |
| `404 Not Found` | Requested resource does not exist |
| `409 Conflict` | Slot conflict, duplicate booking, or SQLite contention |

## Database behavior

`schema.sql` runs on startup using `CREATE TABLE IF NOT EXISTS`. `seed.sql` uses `INSERT OR IGNORE`, so application restarts preserve existing records.

The database contains exactly these five domain tables:

1. `users`
2. `providers`
3. `services`
4. `availability_slots`
5. `appointments`

All persistence uses Spring JDBC and hand-written, parameterized SQL. The project does not use JPA, Hibernate, Spring Data repositories, ORM entities, JPQL, or another database.

## Notification distribution boundary

Booking and cancellation services publish an appointment event while their SQLite transaction is active. `NotificationDispatchService` listens with `TransactionPhase.AFTER_COMMIT` and runs on a dedicated asynchronous executor. A rollback therefore produces no remote call, while a committed booking is never undone by a timeout or notification failure.

The listener loads the committed appointment details through hand-written JDBC SQL and sends one coarse-grained request:

```json
{
  "recipient": "player.demo",
  "type": "BOOKING_CONFIRMED",
  "message": "Confirmed: Court Rental with Bob Kim on Oct 15, 2026 9:00 AM -07:00."
}
```

The Gateway sends a stable key such as `appointment-12-booking_confirmed` with every retry. The mock service stores only the first request for that key, so a lost response and retry do not create duplicate notification work. After the final failure, the fallback increments a metric and writes an ERROR log; the appointment remains committed.

## Health, metrics, and logs

`GET /health` runs `SELECT 1` through `JdbcTemplate`. A successful response is HTTP `200`:

```json
{"status":"UP","database":"UP","checkedAt":"2026-10-27T22:30:00Z"}
```

A database failure returns HTTP `503` with both status fields set to `DOWN`.

Actuator exposes metric names at `GET /actuator/metrics`. Important counters include:

- `picklereserve.bookings.committed`
- `picklereserve.cancellations.committed`
- `picklereserve.notifications.delivered`
- `picklereserve.notifications.retry`
- `picklereserve.notifications.fallback`

Example metric request:

```bash
curl -s http://localhost:8080/actuator/metrics/picklereserve.bookings.committed
```

The logging plan avoids passwords and request bodies. INFO records committed appointment events and accepted/delivered notifications. WARN records a transient remote failure before a retry. ERROR records exhausted notification delivery or a failed SQLite health check.

## Docker

Build and run the application with its persistent SQLite volume:

```bash
docker compose up --build
```

Verify the container:

```bash
curl -s http://localhost:8080/health
curl -s http://localhost:8080/api/home
```

Stop it without deleting the named database volume:

```bash
docker compose down
```

The Compose file intentionally runs one application container because SQLite is an embedded file. The database is stored in the `picklereserve-data` named volume at `/app/data/picklereserve.db`.

## Continuous integration

`.github/workflows/build.yml` checks out the repository, installs Temurin Java 21, enables the Maven dependency cache, and runs `mvn -B -ntp clean verify` on every push and pull request. Surefire reports are uploaded even if a test fails.

## Code walkthrough

- [Milestone 1 Code Walkthrough](https://drive.google.com/file/d/1Y2-yKbf_UhXS4FvupyvaxS4RJLb5fqzu/view?usp=sharing)
- [Milestone 2 Code Walkthrough](https://drive.google.com/file/d/1jkBewytTw-SWNfrBJBJ4uk2ip1qgbB_o/view?usp=sharing)
