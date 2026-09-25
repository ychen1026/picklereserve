# PickleReserve

PickleReserve is a pickleball court and coaching reservation system for SJSU CMPE 172. Milestone 1 delivers the requirements/design package and a running layered Spring Boot skeleton backed by SQLite and hand-written JDBC SQL.

## Milestone 1 scope

- Java 21 and Spring Boot 3.3.13
- Controller -> Service -> Repository -> SQLite request flow
- Exactly five domain tables: `users`, `providers`, `services`, `availability_slots`, and `appointments`
- Startup-safe `schema.sql` and idempotent `seed.sql`
- Database-enforced double-booking guard: `appointments.slot_id` is unique
- `GET /` home summary and `GET /slots` availability endpoint
- DTO-only JSON responses, filters, SQL pagination, validation, and stable errors
- Integration tests for both endpoints, validation, foreign keys, and the unique slot guard

Booking, login/RBAC, cancellation, provider writes, notifications, observability, deployment, and AI are intentionally scheduled for Milestones 2 through 4.

## Prerequisites

- JDK 21
- Maven 3.9+

No separate database server is required. The SQLite JDBC driver creates `picklereserve.db` in the project directory.

## Build and test

```bash
mvn clean test
```

## Run

```bash
mvn spring-boot:run
```

In another terminal:

```bash
curl -s http://localhost:8080/
curl -s "http://localhost:8080/slots?page=0&size=2"
curl -s "http://localhost:8080/slots?providerId=1&serviceId=1&date=2026-09-26"
```

To place the database elsewhere, provide a JDBC URL through the environment:

```bash
PICKLERESERVE_DB_URL=jdbc:sqlite:/absolute/path/picklereserve.db mvn spring-boot:run
```

## API

### `GET /`

Returns product metadata, counts read from SQLite, and the next three open slots.

### `GET /slots`

Returns available slots as a paginated DTO. Optional query parameters:

| Parameter | Meaning | Default |
|---|---|---|
| `providerId` | Positive provider ID | all providers |
| `serviceId` | Positive service ID | all services |
| `date` | ISO date (`YYYY-MM-DD`) | all dates |
| `page` | Zero-based page | `0` |
| `size` | Results per page, 1-100 | `20` |

## Database behavior

`schema.sql` runs on every startup using `CREATE TABLE IF NOT EXISTS`; `seed.sql` uses `INSERT OR IGNORE`, so application restarts preserve existing data. `DatabaseConfig` enables SQLite foreign keys on every connection, a busy timeout, and WAL journal mode. All SQL is hand-written; the project contains no ORM, JPA, Hibernate, or Spring Data repository.

## Code Walkthrough
[Milestone 1 Code Walkthrough] (https://drive.google.com/file/d/1Y2-yKbf_UhXS4FvupyvaxS4RJLb5fqzu/view?usp=sharing)