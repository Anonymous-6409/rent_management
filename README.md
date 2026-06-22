# Rent Management

A simple property-rent management web application built with **Spring Boot MVC**, **Thymeleaf**, and **MySQL**.

It lets you manage **Properties**, **Tenants**, and **Rent Payments** through a clean server-rendered UI, with a dashboard summarising key figures.

## Tech Stack

- Java 17
- Spring Boot 3.2.x (Web MVC, Data JPA, Validation)
- Thymeleaf (server-side views)
- MySQL 8
- Maven

## Features

- **Dashboard** – owner/property/tenant counts, total rent collected, reminder count, recent payments.
- **Owners** – CRUD; an owner can hold many properties.
- **Properties** – CRUD with status (VACANT / OCCUPIED / UNDER_MAINTENANCE), monthly rent, and an owner.
- **Tenants** – CRUD, linked to a property, with lease start/end dates.
- **Payments** – record rent payments per tenant, per period, with status (PAID / PENDING / OVERDUE).
- **Payment reminders** – for both tenants and owners (see below).
- **Authentication** – form login + self-registration, backed by a `users` table (see below).
- Server-side validation with inline error messages.

## Authentication & Login

The whole management UI is protected by **Spring Security** form login.

- **Users** are stored in a `users` table with **BCrypt**-hashed passwords (`CustomUserDetailsService`).
- **Login** at `/login`; **self sign-up** at `/register` (new accounts get the `USER` role).
- **Logout** from the button in the sidebar footer.
- A **default admin** is seeded on first start-up if it doesn't exist:
  - username `admin`, password `admin123` (configurable — see below). **Change it after first login.**
- Public paths: `/login`, `/register`, and static assets. Everything else requires authentication.
- CSRF protection is enabled (Thymeleaf injects the token into all `th:action` forms automatically).

### Roles & owner login

There are three roles: **ADMIN**, **OWNER**, **USER**.

| Role | Access |
|------|--------|
| **ADMIN** | Full access — manages owners, properties, tenants, payments, reminders. |
| **OWNER** | **Manages their own data**: create / edit / delete *their own* properties, tenants, payments, and generate/send *their own* reminders, plus a scoped dashboard. Cannot see or touch other owners' data. |
| **USER** | A self-registered account not yet linked to an owner — sees nothing until an admin links it. |

Enforcement is layered:
- **URL rules** (`SecurityConfig`): `/owners/**` is admin-only; create/edit/delete/generate/send are `hasAnyRole('ADMIN','OWNER')`.
- **Record-level ownership** (controllers): an owner may only edit/delete records belonging to them; new records are auto-tied to the owner, and form dropdowns are scoped to their data (`AccessDeniedException` / 403 otherwise).
- **UI** (`sec:authorize`): Add/Edit/Delete controls render for admins and owners; the Owners menu/card stay admin-only.

**Creating owner logins** — two ways, both supported:
1. **Auto-provisioned**: when an admin adds an `Owner`, a linked `OWNER` login is created (username = the owner's email) with a temporary password that is **emailed to the owner** (and shown once on the Owners page as a fallback when SMTP isn't configured).
2. **Link a self-registered user**: a person signs up at `/register` (role `USER`), then an admin opens that owner's **edit page** and links the registered account, which promotes it to `OWNER`.

### Change password

Every signed-in user can change their own password from the **key icon** in the sidebar footer (`/account/password`) — it verifies the current password before updating.

```properties
app.security.admin.username=admin
app.security.admin.password=admin123
```

## Payment Reminder System

Reminds **tenants** that rent is due and notifies **property owners** of outstanding rent.

- **Generation** – for a billing period (the current month by default), a reminder is created for every
  tenant who has **no `PAID` payment** for that period. A second reminder goes to the property's owner
  (if one is on file). Generation is **idempotent** per `(tenant, period, recipient)` — re-running never
  creates duplicates.
- **Delivery** – reminders are always persisted and shown on the **Reminders** page. Sending marks them
  `SENT`/`FAILED`. Delivery channel is config-driven:
  - `app.reminders.email.enabled=false` (default) → reminders are logged (no SMTP needed).
  - `app.reminders.email.enabled=true` → reminders are emailed via SMTP (configure `spring.mail.*`).
- **Scheduling** – a job (`ReminderScheduler`) generates and sends reminders on a cron schedule
  (`app.reminders.cron`, default daily at 09:00). You can also trigger **Generate** / **Send pending**
  manually from the Reminders page.

### Reminder configuration (`application.properties`)

```properties
app.reminders.due-day=5                 # day of month rent is due (drives the due date)
app.reminders.cron=0 0 9 * * *          # when the scheduled job runs
app.reminders.email.enabled=false       # true => send real emails over SMTP
app.reminders.email.from=no-reply@rentmanager.local
# spring.mail.host / port / username / password  (required only when email is enabled)
```

## Project Structure

```
src/main/java/com/moneytree/rentmanagement/
├── RentManagementApplication.java   # Entry point
├── config/                          # String->entity converters for form binding
├── controller/                      # MVC controllers (Home, Property, Tenant, Payment)
├── model/                           # JPA entities + enums
├── repository/                      # Spring Data JPA repositories
└── service/                         # Business/service layer
src/main/resources/
├── application.properties           # DB + JPA + Thymeleaf config
├── static/css/style.css             # Styling
└── templates/                       # Thymeleaf views
```

## Getting Started

### 1. Prerequisites

- JDK 17+
- Maven 3.9+ (or use the bundled `mvnw` if generated)
- MySQL 8 running locally

### 2. Database

The app is configured to auto-create the schema. With the default credentials you only need MySQL running:

```sql
CREATE DATABASE IF NOT EXISTS rent_management;
```

Update `src/main/resources/application.properties` if your MySQL username/password differ:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

### 3. Run

```bash
mvn spring-boot:run
```

Then open: http://localhost:8080

## Notes

- `spring.jpa.hibernate.ddl-auto=update` lets Hibernate create/update tables automatically. For production, switch to a migration tool (Flyway/Liquibase) and set this to `validate`.
- Default DB credentials are `root/root` — change them for any real deployment.
