# EduSage FX — Presentation Outline
> Paste this into Gamma (gamma.app) → "Generate from outline"

---

## Slide 1 — Title
**EduSage FX**
*A Smart Student Performance Analytics Platform*

- Desktop application for educational institutions
- Real-time analytics, risk detection, and personalized recommendations
- Built with Java 21 · Spring Boot 3.2 · JavaFX 21 · MariaDB

---

## Slide 2 — The Problem
**Why do students fall behind unnoticed?**

- Teachers manage large class sizes with little visibility into individual performance
- Academic struggles often go undetected until it's too late
- No unified platform to track attendance, scores, trends, and risk in one place
- Manual reporting is slow, inconsistent, and error-prone
- Students lack actionable feedback on their own progress

---

## Slide 3 — Our Solution
**EduSage FX: One platform, three roles, complete visibility**

- **Students** — view their analytics, take quizzes, and download performance reports
- **Teachers** — monitor the entire class, identify at-risk students, and export insights
- **Admins** — manage users system-wide and track institution-level statistics
- Role-based access control with JWT authentication
- Fully offline-capable desktop client — no browser required

---

## Slide 4 — System Architecture
**Modern, layered, and secure**

- **Frontend**: JavaFX 21 desktop client with FXML-driven UI
- **Backend**: Spring Boot 3.2 REST API with Spring Security
- **Database**: MariaDB 11 (Dockerized) via Hibernate JPA
- **Auth**: Stateless JWT tokens (24-hour expiry, role-encoded)
- **Communication**: Java 11 HttpClient with JSON (Jackson 2.15)
- Multi-module Maven project — backend and client independently buildable

---

## Slide 5 — Student Dashboard
**Personal performance at a glance**

- **7 live analytics metrics**: overall average, moving average, attendance %, improvement rate, risk level, subject-wise averages, score timeline
- Interactive line chart (score trend over time) and bar chart (subject breakdown)
- Smart risk badge — On Track / Needs Attention / High Risk
- AI-style recommendation cards tailored to each student's data
- Quiz list with double-click to take — instant grading and score display
- One-click PDF report download saved to the user's home folder

---

## Slide 6 — Teacher Dashboard
**Class-wide visibility and intervention tools**

- Full class overview table: name, roll number, average score, attendance, risk level
- At-risk counter and total student count updated live
- Double-click any student row to open their full profile
- Leaderboard popup — top 10 students ranked by overall average
- Student profile view includes: counselling session log, subject breakdown, photo/initials avatar
- Add counselling session records with outcome tracking (RESOLVED, ONGOING, REFERRED)

---

## Slide 7 — Admin Dashboard
**Institution-wide control and oversight**

- **Stats cards**: total students, total teachers, active users, at-risk count, system average score, total quizzes
- **User Management tab**: search, filter, activate/deactivate, delete, create new users
- **Student Analytics tab**: searchable student table with double-click profile access
- Create new accounts for any role (Student, Teacher, Admin) directly from the dashboard
- Roll numbers auto-assigned on student registration (format: STU-0001, STU-0002 …)

---

## Slide 8 — Analytics Engine
**Data-driven insights under the hood**

- **7 computed metrics** per student — calculated fresh on every request
- **Risk classification**: score threshold + attendance rules → HIGH_RISK / NEEDS_ATTENTION / ON_TRACK
- **Recommendation engine**: 4 rule-based triggers generating contextual advice cards
- Score timeline reconstructed from historical quiz submissions
- Subject averages aggregated across all quizzes per subject
- Moving average computed over the last 5 scores for trend detection
- PDF export service generates a formatted multi-section report using iText/OpenPDF

---

## Slide 9 — Technology Stack
**Chosen for reliability, performance, and developer productivity**

| Layer | Technology |
|---|---|
| Desktop UI | JavaFX 21.0.1 + FXML |
| Backend API | Spring Boot 3.2.0 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Database | MariaDB 11.2 (Docker) |
| ORM | Hibernate JPA (Spring Data) |
| JSON | Jackson 2.15.2 |
| Build | Maven (multi-module) |
| Runtime | Java 21 (JDK) |
| PDF | iText / OpenPDF |

---

## Slide 10 — Impact & Future Scope
**What EduSage FX delivers today — and where it's headed**

**Current impact:**
- Early identification of at-risk students before grades deteriorate
- Reduced manual reporting burden on teachers
- Transparent, data-backed feedback loop for students
- Centralized institution management for admins

**Future scope:**
- Email/notification alerts for high-risk students
- Parent portal with read-only analytics access
- Machine learning-based grade prediction
- Multi-institution (multi-tenant) support
- Mobile companion app (Android/iOS)
