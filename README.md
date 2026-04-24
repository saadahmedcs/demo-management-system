# Demo Management System

Tech stack: **Spring Boot** (backend) + **JavaFX** (desktop client), built with the **Maven Wrapper**. The backend uses **JPA** with a JDBC database and supports both **MySQL** and **PostgreSQL** via Spring profiles (see `backend/src/main/resources/application.properties`). User passwords are stored as **SHA-256** hashes.

The desktop client talks to the API at **`http://localhost:8080`** (see `LoginView`, `MainView`, `StudentView`).

Recent feature and doc changes are listed in **[CHANGELOG.md](CHANGELOG.md)**.

## Prerequisites

- **Java 21+** on your `PATH`
  ```powershell
  java -version
  javac -version
  ```
- **No separate Maven install** — use `mvnw` / `mvnw.cmd` from the project root.

## Choose a database

The backend reads **`DB_PROFILE`**, **`DB_URL`**, **`DB_USER`**, and **`DB_PASS`**.

- `DB_PROFILE=mysql` (default) loads `application-mysql.properties`
- `DB_PROFILE=postgres` loads `application-postgres.properties`
- `DB_URL` / `DB_USER` / `DB_PASS` can override either profile

| Approach | When to use |
|----------|-------------|
| **MySQL** | Default local profile (`DB_PROFILE=mysql`), easy local setup. |
| **PostgreSQL** | Use when your environment or team setup prefers Postgres. |

---

## Quick start: MySQL (default)

From the **project root** (folder that contains `pom.xml` and `mvnw.cmd`).

**Terminal A — backend**

```powershell
cd "c:\path\to\demo-management-system-main"
.\mvnw.cmd -pl backend spring-boot:run
```

Wait for: `Tomcat started on port 8080` and `Started BackendApplication`.

**Terminal B — client**

```powershell
cd "c:\path\to\demo-management-system-main"
.\mvnw.cmd -pl client javafx:run
```

If your MySQL credentials differ from defaults, set them in the same terminal:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/demomanagement_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER = "root"
$env:DB_PASS = "your_password_here"
.\mvnw.cmd -pl backend spring-boot:run
```

---

## Run with PostgreSQL

1. Install and start **PostgreSQL**, and create a database (e.g. `demomanagement_db`) if it does not exist.
2. Select the PostgreSQL profile and set credentials to match your server (same terminal session as `spring-boot:run`):

```powershell
cd "c:\path\to\demo-management-system-main"

$env:DB_PROFILE = "postgres"
$env:DB_URL = "jdbc:postgresql://localhost:5432/demomanagement_db"
$env:DB_USER = "postgres"
$env:DB_PASS = "your_password_here"

.\mvnw.cmd -pl backend spring-boot:run
```

**Optional — PostgreSQL in Docker**

```powershell
docker run -d --name demo-pg -p 5432:5432 -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=demomanagement_db postgres:16
```

Then set `DB_URL` / `DB_USER` / `DB_PASS` accordingly (e.g. password `secret`).

---

## macOS / Linux

Use `./mvnw` instead of `.\mvnw.cmd`, and `export` env vars:

```bash
cd /path/to/demo-management-system-main
# MySQL (default profile)
./mvnw -pl backend spring-boot:run
```

For PostgreSQL:

```bash
export DB_PROFILE="postgres"
export DB_URL="jdbc:postgresql://localhost:5432/demomanagement_db"
export DB_USER="postgres"
export DB_PASS="your_password_here"
./mvnw -pl backend spring-boot:run
```

Second terminal:

```bash
./mvnw -pl client javafx:run
```

---

## After the app starts

1. **Backend:** [http://localhost:8080](http://localhost:8080) (root may return 404; that is normal.)
2. **Client:** Log in or **Create Account** (email, name, roll number, **section**, password). First login **locks** the account to **Student** or **Teaching Assistant**; the backend enforces the same role later.
3. **TA:** Create a course (**New course tab**), open it, **Generate slots** (assignment name, individual vs group, etc.). Share the **course join code** with students (Google Classroom / GCR-style workflow).
4. **Student:** Join the course with the code, book slots, use **Messages** for **DMs** with the TA.

Stop either process with **Ctrl+C** in its terminal.

---

## Troubleshooting

| Issue | What to do |
|-------|------------|
| **Port 8080 already in use** | Stop the other process, or change `server.port` in `backend/src/main/resources/application.properties`. |
| **Backend: database connection failed** | Verify selected profile (`DB_PROFILE=mysql` or `DB_PROFILE=postgres`), ensure the DB service is up, and check `DB_URL` / `DB_USER` / `DB_PASS`. |
| **Client errors / empty data** | Start the backend **before** the client; confirm `http://localhost:8080` responds. |
| **First run slow** | Maven Wrapper downloads dependencies once; later runs are faster. |

---

## Features (summary)

- **Courses & join codes:** TAs create courses; students join with a teacher-provided code.
- **Assignments:** Multiple assignments per course; TA slot view uses **tabs per assignment**.
- **Slots:** TA generates slots (individual or **group** with member count); students enter **group roll numbers** when booking group slots; TA sees roll, section, and group rolls.
- **Evaluation:** **Evaluate** action per slot (can be used after the demo time).
- **Schedule UI:** **Day separators** in slot tables and in message view for readability.
- **Messaging:** **Direct messages** between student and TA (not a public forum).
- **Timetable:** Students set class periods; conflicting demo slots are disabled.
- **Authentication:** SHA-256 password hashing; role locked per email.
