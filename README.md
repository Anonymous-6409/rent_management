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
- Server-side validation with inline error messages.

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
