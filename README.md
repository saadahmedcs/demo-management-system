# Demo Management System

Tech stack: **Spring Boot** (backend) + **JavaFX** (desktop client), built with the **Maven Wrapper**.

## Prerequisites
- **Java 21+** installed and on PATH
  - Check:
    ```powershell
    java -version
    javac -version
    ```
- **No separate Maven install needed** (the project uses `mvnw.cmd`)

## Run step-by-step (Windows / PowerShell)

### 1) Open two terminals
- Terminal A: backend
- Terminal B: client

### 2) Start the backend (Terminal A)
1. Go to the project folder:
   ```powershell
   cd "c:\Users\arraf\Demo Management System"
   ```
2. Run Spring Boot:
   ```powershell
   .\mvnw.cmd -pl backend spring-boot:run
   ```
3. Wait until you see a log like:
   - `Tomcat started on port 8080`
   - `Started BackendApplication`

Backend URL: `http://localhost:8080`  
H2 console: `http://localhost:8080/h2`

### 3) Start the JavaFX client (Terminal B)
1. Go to the project folder:
   ```powershell
   cd "c:\Users\arraf\Demo Management System"
   ```
2. Run the JavaFX app:
   ```powershell
   .\mvnw.cmd -pl client javafx:run
   ```
3. The desktop window should open. Use **New course tab** to create a course, then **Generate slots** inside the course.

### 4) Stop the project
- In each terminal, press `Ctrl + C`

## Troubleshooting
- **Client shows no saved courses / slot actions fail**
  - Make sure the backend is running first and you can open `http://localhost:8080`.
- **Port 8080 already in use**
  - Stop the other process using 8080, or change `server.port` in `backend/src/main/resources/application.properties`.
- **First run is slow**
  - Maven Wrapper downloads dependencies the first time. Subsequent runs are faster.

## Sprint 1 features
- **Course tabs**: create course tabs so each course’s demos/slots live together.
- **Automated slot generation**: generate time slots by date range, days of week, working hours, slot length, and breaks.
