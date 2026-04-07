# EduSage FX

A smart student performance analytics platform built with **Spring Boot 3.2** and **JavaFX 21**.

Three role-based dashboards — **Student**, **Teacher**, and **Admin** — provide real-time analytics, risk detection, personalised recommendations, quiz management, counselling session logging, and PDF report export.

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 21+ |
| Apache Maven | 3.9+ |
| Docker | 20+ (for MariaDB) |

---

## 1. Database Setup

Start a MariaDB 11 container:

```bash
docker run -d \
  --name mariadb-dev \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=edusage_db \
  -e MYSQL_USER=edusage_user \
  -e MYSQL_PASSWORD=EdusageFX@2025 \
  mariadb:11.2
```

If the container already exists but is stopped:

```bash
docker start mariadb-dev
```

> The backend uses `spring.jpa.hibernate.ddl-auto=update`, so all tables are created automatically on first startup.

---

## 2. Build the Project

From the project root:

```bash
mvn clean compile
```

This compiles both the `edusage-backend` and `edusage-client` modules.

---

## 3. Start the Backend

```bash
mvn spring-boot:run -f edusage-backend/pom.xml
```

The REST API starts on **http://localhost:8080**. Wait for the log line:

```
Started EduSageApplication in X.XX seconds
```

---

## 4. Start the Client

In a **separate terminal**:

```bash
mvn javafx:run -f edusage-client/pom.xml
```

The JavaFX desktop window opens with the login screen.

---

## 5. Default Test Accounts

| Email | Password | Role |
|---|---|---|
| `admin@edusage.com` | `pass123` | Admin |
| `teacher@edusage.com` | `pass123` | Teacher |
| `student@edusage.com` | `pass123` | Student |

> If these accounts don't exist yet, register a new user via the registration screen. The first admin account must be created directly in the database.

---

## Project Structure

```
EduSageFX/
├── pom.xml                          # Parent POM (multi-module)
├── edusage-backend/                 # Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/edusage/
│       ├── config/                  # SecurityConfig, CorsConfig
│       ├── controller/              # Auth, Student, Teacher, Admin, Quiz, Content
│       ├── dto/                     # Request/Response DTOs
│       ├── model/                   # JPA entities + enums
│       ├── repository/              # Spring Data JPA repositories
│       ├── security/                # JWT filter, util, UserDetailsService
│       └── service/                 # Analytics, Auth, Quiz, Recommendation, PDF
└── edusage-client/                  # JavaFX desktop client
    ├── pom.xml
    └── src/main/
        ├── java/com/edusage/client/
        │   ├── controller/          # FXML controllers (Login, Register, dashboards)
        │   ├── model/               # UserSession singleton
        │   ├── service/             # ApiService (HTTP client)
        │   └── util/                # SceneManager
        └── resources/
            ├── css/                 # Stylesheet
            └── fxml/                # UI layouts
```

---

## Key Features

- **Student Dashboard** — 7 analytics metrics, score trend chart, subject breakdown, risk badge, personalised recommendations, quiz taking, PDF report download
- **Teacher Dashboard** — class overview table, at-risk student detection, student profile viewer, counselling session logger, top-10 leaderboard
- **Admin Dashboard** — institution-wide stats, user management (create/activate/deactivate/delete), student analytics table
- **Authentication** — stateless JWT with role-based access control (BCrypt password hashing)
- **Auto Roll Numbers** — student roll numbers auto-assigned as `STU-XXXX` on registration

---

## Tech Stack

| Layer | Technology |
|---|---|
| Desktop UI | JavaFX 21.0.1 + FXML |
| Backend API | Spring Boot 3.2.0 |
| Security | Spring Security 6 + JWT (jjwt 0.11.5) |
| Database | MariaDB 11.2 (Docker) |
| ORM | Hibernate JPA (Spring Data) |
| JSON | Jackson 2.15.2 |
| Build | Maven 3 (multi-module) |
| Runtime | Java 21 |
| PDF Export | OpenPDF / iText |

---

## Running Tests

```bash
mvn test -f edusage-backend/pom.xml
```

Runs 21 unit tests covering `AnalyticsService` and `RecommendationService`.

---

## Configuration

All backend configuration is in `edusage-backend/src/main/resources/application.properties`:

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | Backend API port |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/edusage_db` | Database connection URL |
| `spring.datasource.username` | `edusage_user` | DB username |
| `spring.datasource.password` | `EdusageFX@2025` | DB password |
| `jwt.secret` | *(32+ char string)* | JWT signing key |
| `jwt.expiration` | `86400000` | Token expiry (24 hours in ms) |
| `app.upload.dir` | `./uploads` | File upload directory |

The client connects to `http://localhost:8080/api` (configured in `ApiService.java`).
