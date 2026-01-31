# University Management System (UEMS) — Progress Report

Date: 2025-09-23

---

## 1) Project Overview

The University Management System (UEMS) is currently a desktop application built with Java Swing, backed by a MySQL database via JDBC. The system digitizes admissions, academic records, departmental curricula, and examinations. The design and implementation follow a modular approach with a clear plan to extend into Employee and Library management modules.

Reference design doc: `docs/UEMS_Planning.md`


## 2) Accomplishments to Date (Implemented)

- Authentication and Launch Flow (`Splash` → `Login` → `Project`)
- Student Admissions (`AddStudent`) with registration number generation and initial semester setup
- Faculty Admissions (`AddTeacher`)
- Student and Teacher Directory Views (search/filter, `JTable` rendering via `DbUtils`)
- Department Credits and Courses display (`Dept`)
- Examination Module (`EnterMarks`) with UPSERT to `student_marks`, CGPA computation, and promotion logic
- Optional Password Recovery (`Forget_pass`)

Key technologies used:
- Java Swing UI, Ant build
- MySQL 8.x, Connector/J (`com.mysql.cj.jdbc.Driver`)
- Utility components: `JDateChooser`, `DbUtils`


## 3) Partially Implemented / Remaining Work

- Facility Management (remains)
  - Scope: manage classrooms, labs, equipment, room allocation/scheduling
  - Proposed entities: `facility`, `room`, `booking`, `maintenance`
  - UI: facility inventory, booking calendar, maintenance tickets

- Employee Management (planned; not yet implemented)
  - Entities: `employee`, `attendance`, `payroll`, `role`
  - Features: onboarding, directory, attendance tracking, payroll overview

- Library Management (planned; not yet implemented)
  - Entities: `books`, `copies`, `members` (students/teachers), `loans`, `fines`
  - Features: catalog search, issue/return, fine calculation

- Security and Reliability Enhancements
  - Migrate SQL concatenation to `PreparedStatement`
  - Externalize DB credentials (properties/env)
  - Add FK constraints and validations across modules


## 4) Instructor Feedback and Direction (Action Required)

- The supervising faculty has requested the application be converted from a desktop application to a web application, and deployed on a global server so it is accessible publicly.

Action items:
- Re-architecture plan for web stack
- Select hosting with global availability
- Prepare migration roadmap and timeline


## 5) Proposed Web App Migration Plan

- Target Architecture
  - Backend: Java Spring Boot (or Node.js/Express) with REST APIs
  - Frontend: React/Next.js or plain React SPA
  - Database: MySQL (reuse schema with migrations)
  - Auth: Session/JWT-based authentication; roles for admin, staff, student

- Hosting (Global Access)
  - Options: Render, Railway, Fly.io, AWS/GCP/Azure
  - Frontend on Vercel/Netlify (if SPA/Next.js), Backend on Render/Railway/Fly.io
  - Use a public URL/domain for instructor/student access

- Migration Strategy
  - Phase 1: Expose existing data model via REST APIs
  - Phase 2: Implement web UI for Admissions, Directory, Departments, Examinations
  - Phase 3: Add Employee and Library modules
  - Phase 4: Harden security, logging, backup, and monitoring

- Data & Compatibility
  - Keep identifiers consistent (`registration_no`, `empId`, dept codes)
  - Add input validation and role-based access control (RBAC)


## 6) Current Risks and Mitigation

- Hardcoded DB credentials — plan to move to config and secrets
- SQL injection risk from string concatenation — migrate to `PreparedStatement`
- Schema evolution across new modules — define migrations and constraints
- Deployment readiness — containerize services (Docker) and prepare CI/CD


## 7) Next Milestones

- Milestone A: Finalize web architecture and deployment target
- Milestone B: API layer for Authentication, Admissions, Directory, Departments, Examinations
- Milestone C: React/Next.js UI implementing Phase 2 features
- Milestone D: Implement Employee Management module
- Milestone E: Implement Library Management module
- Milestone F: Public deployment on a global server with test users


## 8) Appendix: Mapping Desktop → Web

- `Project.java` menus → Web navigation (sidebar/topbar)
- `AddStudent.java`/`AddTeacher.java` → Web forms with validation
- `StudentDetails.java`/`TeacherDetails.java` → Data tables with server-side pagination/filtering
- `EnterMarks.java` → Marks entry form with real-time CGPA results
- `Dept.java` → Department catalog and curriculum views

---

Prepared for presentation to supervising faculty. This report acknowledges remaining modules (Facility, Employee, Library) and the directive to convert to a globally accessible web application.
