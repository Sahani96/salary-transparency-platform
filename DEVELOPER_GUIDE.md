# Salary Transparency Platform — Developer Documentation

> **Audience**: Software engineers working on the project.
> This guide covers the architecture, every service's responsibilities, API contracts, data models, frontend structure, and key workflows so you can start contributing confidently.

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Architecture](#2-architecture)
3. [Database Schema](#3-database-schema)
4. [Identity Service](#4-identity-service)
5. [Salary Service](#5-salary-service)
6. [Vote Service](#6-vote-service)
7. [Search Service](#7-search-service)
8. [Stats Service](#8-stats-service)
9. [BFF (Backend-for-Frontend) Service](#9-bff-backend-for-frontend-service)
10. [Frontend (React SPA)](#10-frontend-react-spa)
11. [Authentication & Authorization Flow](#11-authentication--authorization-flow)
12. [Core Workflow: Submission → Voting → Approval → Search → Stats](#12-core-workflow)
13. [Configuration & Environment Variables](#13-configuration--environment-variables)
14. [Local Development](#14-local-development)
15. [E2E Testing](#15-e2e-testing)
16. [Common Troubleshooting](#16-common-troubleshooting)

---

## 1. System Overview

The **Salary Transparency Platform** is a community-driven website where Sri Lankan tech professionals can:

- **Submit** their salary information **anonymously** (no login required).
- **Vote** on whether submitted salaries are trustworthy (login required — upvote/downvote).
- **Search** approved salaries by country, company, role, level, and more.
- **View statistics** with averages, medians, percentiles, and cross-group comparisons.

### Key Design Principles

| Principle | Implementation |
|-----------|---------------|
| **Privacy** | User identity data (email, passwords) is stored in a separate database schema from salary submissions. Salary records are never directly linked to user accounts. |
| **Anonymous submission** | No authentication is required to submit a salary. |
| **Community moderation** | Submissions start as `PENDING` and advance to `APPROVED` only after receiving enough upvotes (configurable threshold, default = 3). |
| **Anonymization toggle** | Submitters can set `anonymize: true` to hide their company name and city in public views. |

### Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Frontend | React, Material-UI, React Router, Axios | React 19, MUI 5 |
| Backend services | Java, Spring Boot, Spring Data JPA, Spring Security | Java 17, Spring Boot 4.0.3 |
| Database | PostgreSQL (single instance, 3 schemas) | PostgreSQL 16 |
| Authentication | JWT (jjwt library) + BCrypt password hashing | jjwt 0.12.6 |
| Build tool | Maven (Spring Boot services), npm (frontend) | Maven via `mvnw` wrapper |
| E2E testing | Playwright (Chromium) | 1.48+ |

---

## 2. Architecture

```
┌─────────────┐
│  Frontend   │  React SPA (port 3000 dev / 80 prod)
│  (React)    │  All API calls go to /api/* via BFF
└──────┬──────┘
       │  HTTP (Axios)
       ▼
┌──────────────┐
│  BFF Service │  API Gateway (port 8080)
│  (Spring)    │  JWT validation · CORS · Routing
└──────┬───────┘
       │ RestTemplate
       ├──────────────┬──────────────┬──────────────┬──────────────┐
       ▼              ▼              ▼              ▼              ▼
┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐
│ Identity   │ │  Salary    │ │   Vote     │ │  Search    │ │   Stats    │
│ Service    │ │  Service   │ │  Service   │ │  Service   │ │  Service   │
│ :8081      │ │  :8082     │ │  :8083     │ │  :8084     │ │  :8085     │
└─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └─────┬──────┘
      │              │              │              │              │
      ▼              ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         PostgreSQL :5432                                │
│  ┌──────────────┐  ┌──────────────────┐  ┌──────────────────────────┐   │
│  │ identity     │  │ salary           │  │ community                │   │
│  │  └─ users    │  │  └─ submissions  │  │  └─ votes                │   │
│  └──────────────┘  └──────────────────┘  └──────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

### Service Communication

- **Frontend → BFF**: All frontend requests target `/api/*`. In production nginx proxies these to the BFF container. In development the React dev server uses `REACT_APP_API_URL` to point Axios at the BFF directly.
- **BFF → downstream**: The BFF uses Spring `RestTemplate` to call each downstream service on their internal port.
- **Vote → Salary**: When an upvote threshold is reached, the Vote Service calls the Salary Service's `PATCH /api/salaries/{id}/status` endpoint to mark the submission `APPROVED`.
- **No service-to-service auth**: Internal services trust each other (they run on a private network). Only the BFF validates JWTs for vote-related endpoints.

---

## 3. Database Schema

A single PostgreSQL database (`salarydb`) with three logically separated schemas:

### 3.1 `identity` Schema — User Accounts

```sql
CREATE TABLE identity.users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,    -- BCrypt hash
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

**Indexes**: `idx_users_email`, `idx_users_username`

### 3.2 `salary` Schema — Submissions

```sql
CREATE TABLE salary.submissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_title           VARCHAR(255) NOT NULL,
    company             VARCHAR(255) NOT NULL,
    country             VARCHAR(100) NOT NULL,
    city                VARCHAR(100),
    experience_level    VARCHAR(20) NOT NULL
                        CHECK (experience_level IN ('JUNIOR','MID','SENIOR','LEAD','PRINCIPAL')),
    years_of_experience INTEGER NOT NULL CHECK (years_of_experience >= 0),
    base_salary         DECIMAL(15,2) NOT NULL CHECK (base_salary >= 0),
    currency            VARCHAR(10) NOT NULL DEFAULT 'LKR',
    employment_type     VARCHAR(20) NOT NULL DEFAULT 'FULL_TIME'
                        CHECK (employment_type IN ('FULL_TIME','PART_TIME','CONTRACT','FREELANCE')),
    anonymize           BOOLEAN NOT NULL DEFAULT false,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    tech_stack          VARCHAR(500),
    submitted_at        TIMESTAMP NOT NULL DEFAULT NOW()
);
```

**Indexes**: `status`, `country`, `company`, `experience_level`, `job_title`

> **Important**: No `user_id` or `email` column exists in this table — salary data is fully decoupled from identity data to protect privacy.

### 3.3 `community` Schema — Votes

```sql
CREATE TABLE community.votes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES salary.submissions(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    vote_type       VARCHAR(10) NOT NULL CHECK (vote_type IN ('UPVOTE','DOWNVOTE')),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(submission_id, user_id)   -- one vote per user per submission
);
```

**Indexes**: `submission_id`, `user_id`

---

## 4. Identity Service

**Purpose**: Manages user accounts, signup, login, and JWT token generation/validation.

| Property | Value |
|----------|-------|
| Port | `8081` |
| Schema | `identity` |
| Package | `com.example.identityservice` |
| Database access | Read/Write to `identity.users` |

### 4.1 Entity: `User`

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | `UUID` | Primary key, auto-generated |
| `username` | `String` | Unique, not null |
| `email` | `String` | Unique, not null |
| `passwordHash` | `String` | BCrypt hash, not null |
| `createdAt` | `LocalDateTime` | Auto-set to `NOW()` |

### 4.2 API Endpoints

#### `POST /api/auth/signup` — Register a new user

**Request Body** (`SignupRequest`):

```json
{
  "username": "john_doe",       // required, 3-50 characters
  "email": "john@example.com",  // required, valid email format
  "password": "myP@ssw0rd"      // required, 6-100 characters
}
```

**Response** (`AuthResponse`) — `201 Created`:

```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "username": "john_doe",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "message": "User registered successfully"
}
```

**Error responses**:
- `400` — Validation errors (short username, invalid email, etc.)
- `409` — Email or username already taken

#### `POST /api/auth/login` — Authenticate

**Request Body** (`LoginRequest`):

```json
{
  "email": "john@example.com",   // required, valid email
  "password": "myP@ssw0rd"       // required
}
```

> **Note**: Login uses `email`, not `username`. This is a common point of confusion.

**Response** (`AuthResponse`) — `200 OK`:

```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "username": "john_doe",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "message": "Login successful"
}
```

**Error responses**:
- `401` — Invalid email or password

#### `GET /api/auth/validate` — Validate JWT token

**Headers**: `Authorization: Bearer <token>`

**Response** (`AuthResponse`) — `200 OK`:

```json
{
  "userId": "a1b2c3d4-...",
  "username": "john_doe",
  "token": null,
  "message": "Token is valid"
}
```

**Error responses**:
- `401` — Invalid or expired token

### 4.3 Security Configuration

- CSRF protection is disabled (stateless JWT-based API).
- All `/api/auth/**` endpoints are publicly accessible.
- Sessions are stateless (`SessionCreationPolicy.STATELESS`).
- Passwords are hashed with BCrypt before storage.

### 4.4 JWT Token Details

| Property | Value |
|----------|-------|
| Algorithm | HMAC-SHA512 |
| Expiration | 24 hours (configurable via `JWT_EXPIRATION_MS`) |
| Subject (`sub`) | User UUID |
| Custom claim `username` | Username string |
| Signing key | Shared secret (`JWT_SECRET` env var) |

---

## 5. Salary Service

**Purpose**: Handles salary submission CRUD operations. Stores submissions with a `PENDING` status. Applies anonymization when requested.

| Property | Value |
|----------|-------|
| Port | `8082` |
| Schema | `salary` |
| Package | `com.example.salaryservice` |
| Database access | Read/Write to `salary.submissions` |

### 5.1 Entity: `SalarySubmission`

| Field | Type | Default | Constraints |
|-------|------|---------|-------------|
| `id` | `UUID` | auto-generated | Primary key |
| `jobTitle` | `String` | — | Not null |
| `company` | `String` | — | Not null |
| `country` | `String` | — | Not null |
| `city` | `String` | — | Optional |
| `experienceLevel` | `String` | — | `JUNIOR\|MID\|SENIOR\|LEAD\|PRINCIPAL` |
| `yearsOfExperience` | `Integer` | — | ≥ 0 |
| `baseSalary` | `BigDecimal(15,2)` | — | ≥ 0 |
| `currency` | `String` | `"LKR"` | |
| `employmentType` | `String` | `"FULL_TIME"` | `FULL_TIME\|PART_TIME\|CONTRACT\|FREELANCE` |
| `anonymize` | `Boolean` | `false` | |
| `status` | `String` | `"PENDING"` | `PENDING\|APPROVED\|REJECTED` |
| `techStack` | `String` | — | Optional, max 500 chars |
| `submittedAt` | `LocalDateTime` | `NOW()` | Auto-set on creation |

### 5.2 API Endpoints

#### `POST /api/salaries` — Submit a new salary entry

**No authentication required.** Anyone can submit anonymously.

**Request Body** (`SalarySubmissionRequest`):

```json
{
  "jobTitle": "Software Engineer",          // required
  "company": "WSO2",                        // required
  "country": "Sri Lanka",                   // required
  "city": "Colombo",                        // optional
  "experienceLevel": "MID",                 // required: JUNIOR|MID|SENIOR|LEAD|PRINCIPAL
  "yearsOfExperience": 3,                   // required: integer ≥ 0
  "baseSalary": 150000.00,                  // required: decimal ≥ 0
  "currency": "LKR",                        // optional, defaults to "LKR"
  "employmentType": "FULL_TIME",            // optional, defaults to "FULL_TIME"
  "anonymize": false,                       // optional, defaults to false
  "techStack": "Java, Spring Boot, React"   // optional
}
```

**Response** (`SalarySubmissionResponse`) — `201 Created`:

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "jobTitle": "Software Engineer",
  "company": "WSO2",             // "Anonymous" if anonymize=true
  "country": "Sri Lanka",
  "city": "Colombo",
  "experienceLevel": "MID",
  "yearsOfExperience": 3,
  "baseSalary": 150000.00,
  "currency": "LKR",
  "employmentType": "FULL_TIME",
  "anonymize": false,
  "status": "PENDING",
  "techStack": "Java, Spring Boot, React",
  "submittedAt": "2025-01-15T10:00:00"
}
```

**Error responses**:
- `400` — Validation errors (missing required fields, invalid experience level, negative salary, etc.)

#### `GET /api/salaries/{id}` — Retrieve a submission

**Response**: Same shape as above. `company` returns `"Anonymous"` if `anonymize=true`.

#### `PATCH /api/salaries/{id}/status` — Update submission status

> **Internal API** — called by Vote Service when threshold is reached. Not exposed through the BFF.

**Request Body**:

```json
{ "status": "APPROVED" }
```

**Response**: Updated `SalarySubmissionResponse`.

### 5.3 Anonymization Logic

When `anonymize = true`:
- The `company` field returns `"Anonymous"` in the response.
- The `city` field returns `null` in the response.
- The original values are stored in the database but are hidden from API consumers.

---

## 6. Vote Service

**Purpose**: Allows authenticated users to upvote/downvote salary submissions. Implements the auto-approval threshold.

| Property | Value |
|----------|-------|
| Port | `8083` |
| Schema | `community` |
| Package | `com.example.voteservice` |
| Database access | Read/Write to `community.votes` |
| External calls | Salary Service `PATCH /api/salaries/{id}/status` |

### 6.1 Entity: `Vote`

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | `UUID` | Primary key, auto-generated |
| `submissionId` | `UUID` | FK → `salary.submissions(id)` |
| `userId` | `UUID` | FK → `identity.users(id)` |
| `voteType` | `String` | `UPVOTE` or `DOWNVOTE` |
| `createdAt` | `LocalDateTime` | Auto-set |
| | | `UNIQUE(submission_id, user_id)` |

### 6.2 API Endpoints

#### `POST /api/votes` — Cast a vote

**Requires authentication** — the BFF validates the JWT and passes the user ID via `X-User-Id` header.

**Headers**: `X-User-Id: <uuid>` (set by BFF after JWT validation)

**Request Body** (`VoteRequest`):

```json
{
  "submissionId": "11111111-1111-1111-1111-111111111111",   // required
  "voteType": "UPVOTE"                                      // required: UPVOTE|DOWNVOTE
}
```

**Response** (`VoteResponse`) — `200 OK`:

```json
{
  "id": "vote-uuid-here",
  "submissionId": "11111111-...",
  "userId": "user-uuid-here",
  "voteType": "UPVOTE",
  "message": "Vote recorded"
}
```

**Error responses**:
- `400` — Missing required fields
- `409` — User has already voted on this submission (unique constraint violation)

#### `GET /api/votes/submission/{submissionId}` — Get vote counts

**No authentication required.**

**Response** (`VoteCountResponse`) — `200 OK`:

```json
{
  "submissionId": "11111111-...",
  "upvotes": 5,
  "downvotes": 1,
  "netVotes": 4
}
```

### 6.3 Auto-Approval Logic

After each upvote, the service checks:

```
if (upvotes >= vote.approval.threshold) → mark APPROVED
```

1. Count upvotes for the submission.
2. If `upvotes >= threshold` (default: 3):
   - Call Salary Service: `PATCH http://salary-service:8082/api/salaries/{id}/status` with body `{ "status": "APPROVED" }`.
   - The submission becomes visible in search results and stats.

This threshold is configurable via the `VOTE_THRESHOLD` environment variable.

---

## 7. Search Service

**Purpose**: Provides filtered, paginated search over approved salary entries using dynamic JPA Specification queries.

| Property | Value |
|----------|-------|
| Port | `8084` |
| Schema | `salary` (read-only) |
| Package | `com.example.searchservice` |
| Database access | Read-only from `salary.submissions` |

### 7.1 API Endpoints

#### `GET /api/search` — Search approved salaries

**No authentication required.**

**Query Parameters**:

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `country` | String | Exact match | `Sri Lanka` |
| `company` | String | LIKE `%value%` (case-insensitive) | `WSO2` |
| `jobTitle` | String | LIKE `%value%` | `Software Engineer` |
| `experienceLevel` | String | Exact match | `SENIOR` |
| `employmentType` | String | Exact match | `FULL_TIME` |
| `minSalary` | BigDecimal | `base_salary >= value` | `100000` |
| `maxSalary` | BigDecimal | `base_salary <= value` | `500000` |
| `techStack` | String | LIKE `%value%` | `Java` |
| `sortBy` | String | Sort field (default: `submittedAt`) | `baseSalary`, `company`, `jobTitle`, `experienceLevel`, `country` |
| `sortDirection` | String | `asc` or `desc` (default: `desc`) | `asc` |
| `page` | int | Page number (default: `0`) | `0` |
| `size` | int | Page size (default: `20`, max: `100`) | `10` |

> **Important**: Only `APPROVED` submissions are returned. The service always applies a `status = 'APPROVED'` filter regardless of any user input.

**Response** (`SearchResponse`) — `200 OK`:

```json
{
  "results": [
    {
      "id": "11111111-...",
      "jobTitle": "Software Engineer",
      "company": "WSO2",           // "Anonymous" if anonymized
      "country": "Sri Lanka",
      "city": "Colombo",
      "experienceLevel": "MID",
      "yearsOfExperience": 3,
      "baseSalary": 150000.00,
      "currency": "LKR",
      "employmentType": "FULL_TIME",
      "techStack": "Java, Spring Boot, React",
      "submittedAt": "2025-01-15T10:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 12,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

#### `GET /api/search/filters` — Get available filter options

Returns distinct values from approved submissions for populating dropdowns.

**Response** (`FilterOptionsResponse`) — `200 OK`:

```json
{
  "countries": ["Sri Lanka"],
  "companies": ["WSO2", "IFS", "Sysco LABS", "Virtusa", ...],
  "jobTitles": ["Software Engineer", "Senior Software Engineer", ...],
  "experienceLevels": ["JUNIOR", "MID", "SENIOR", "LEAD", "PRINCIPAL"],
  "employmentTypes": ["FULL_TIME", "PART_TIME", "CONTRACT", "FREELANCE"]
}
```

### 7.2 Dynamic Query Building

The Search Service uses Spring Data JPA's `Specification` pattern to build WHERE clauses dynamically. Only non-empty filter values are added to the query. This avoids a combinatorial explosion of repository methods:

```java
// Pseudocode
Specification<SalarySubmission> spec = Specification.where(statusIsApproved());
if (country != null) spec = spec.and(countryEquals(country));
if (company != null) spec = spec.and(companyContains(company));
// ... and so on for each filter
```

---

## 8. Stats Service

**Purpose**: Calculates aggregated salary statistics (averages, medians, percentiles) over approved entries.

| Property | Value |
|----------|-------|
| Port | `8085` |
| Schema | `salary` (read-only) |
| Package | `com.example.statsservice` |
| Database access | Read-only from `salary.submissions` |

### 8.1 API Endpoints

#### `GET /api/stats/summary` — Overall statistics

**Response** (`StatsSummaryResponse`) — `200 OK`:

```json
{
  "totalSubmissions": 12,
  "averageSalary": 242500.00,
  "medianSalary": 175000.00,
  "minSalary": 80000.00,
  "maxSalary": 600000.00,
  "p25Salary": 130000.00,
  "p75Salary": 350000.00,
  "p90Salary": 500000.00
}
```

The `p25`, `p75`, `p90` fields are the 25th, 75th, and 90th percentiles respectively, calculated using PostgreSQL's `PERCENTILE_CONT()` window function.

#### `GET /api/stats/by-role` — Grouped by job title

**Response** (`GroupedStatsResponse`):

```json
{
  "groupedBy": "role",
  "entries": [
    {
      "groupName": "Software Engineer",
      "count": 3,
      "averageSalary": 143333.33,
      "medianSalary": 150000.00,
      "minSalary": 130000.00,
      "maxSalary": 150000.00,
      "p25Salary": 140000.00,
      "p75Salary": 150000.00,
      "p90Salary": 150000.00
    },
    ...
  ]
}
```

#### `GET /api/stats/by-company` — Grouped by company

Same response shape. **Excludes** entries where `anonymize = true` (can't group by company if the company is hidden).

#### `GET /api/stats/by-country` — Grouped by country

Same response shape.

#### `GET /api/stats/by-level` — Grouped by experience level

Same response shape. Groups: `JUNIOR`, `MID`, `SENIOR`, `LEAD`, `PRINCIPAL`.

#### `GET /api/stats/compare` — Compare two groups

**Query Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `compareBy` | String | `role`, `company`, `country`, or `level` |
| `first` | String | First group value (e.g., `WSO2`) |
| `second` | String | Second group value (e.g., `IFS`) |

**Response** (`ComparisonResponse`) — `200 OK`:

```json
{
  "compareBy": "company",
  "first": {
    "groupName": "WSO2",
    "count": 2,
    "averageSalary": 135000.00,
    "medianSalary": 135000.00,
    "minSalary": 120000.00,
    "maxSalary": 150000.00,
    "p25Salary": 127500.00,
    "p75Salary": 142500.00,
    "p90Salary": 147000.00
  },
  "second": {
    "groupName": "IFS",
    "count": 1,
    "averageSalary": 350000.00,
    ...
  }
}
```

---

## 9. BFF (Backend-for-Frontend) Service

**Purpose**: Single entry point for the frontend. Handles CORS, JWT validation for protected routes, and proxies all requests to downstream microservices.

| Property | Value |
|----------|-------|
| Port | `8080` |
| Database | **None** — DataSource auto-config is disabled |
| Package | `com.example.bffservice` |

### 9.1 Routing Table

| BFF Endpoint | Downstream Target | Auth? |
|-------------|-------------------|-------|
| `POST /api/auth/signup` | Identity `:8081/api/auth/signup` | No |
| `POST /api/auth/login` | Identity `:8081/api/auth/login` | No |
| `GET /api/auth/validate` | Identity `:8081/api/auth/validate` | Token forwarded |
| `POST /api/salaries` | Salary `:8082/api/salaries` | No |
| `GET /api/salaries/{id}` | Salary `:8082/api/salaries/{id}` | No |
| `GET /api/search` | Search `:8084/api/search` | No |
| `GET /api/search/filters` | Search `:8084/api/search/filters` | No |
| `POST /api/votes` | Vote `:8083/api/votes` | **Yes — JWT required** |
| `GET /api/votes/submission/{id}` | Vote `:8083/api/votes/submission/{id}` | No |
| `GET /api/stats/summary` | Stats `:8085/api/stats/summary` | No |
| `GET /api/stats/by-role` | Stats `:8085/api/stats/by-role` | No |
| `GET /api/stats/by-company` | Stats `:8085/api/stats/by-company` | No |
| `GET /api/stats/by-country` | Stats `:8085/api/stats/by-country` | No |
| `GET /api/stats/by-level` | Stats `:8085/api/stats/by-level` | No |
| `GET /api/stats/compare` | Stats `:8085/api/stats/compare` | No |

### 9.2 JWT Validation for Voting

When a user casts a vote (`POST /api/votes`):

1. BFF extracts the `Authorization: Bearer <token>` header.
2. Validates the token using the shared JWT secret.
3. Extracts the `userId` (from the `sub` claim).
4. Passes it to the Vote Service as an `X-User-Id` header.
5. Proxies the request body unchanged.

If the token is invalid/missing, BFF returns `401 Unauthorized` without calling the Vote Service.

### 9.3 Proxy Controller Pattern

Each proxy controller creates a **clean** `ResponseEntity` from the downstream response — only the body and status code are forwarded. This prevents hop-by-hop HTTP headers (like `Transfer-Encoding`) from leaking through, which would cause nginx to reject the response.

```java
// Correct pattern used in all BFF proxy controllers:
ResponseEntity<String> downstream = restTemplate.exchange(...);
return ResponseEntity.status(downstream.getStatusCode())
                     .body(downstream.getBody());
```

### 9.4 CORS Configuration

The BFF allows cross-origin requests from origins specified in the `CORS_ORIGINS` environment variable (default: `http://localhost:3000`). Multiple origins can be comma-separated.

### 9.5 Error Handling

| Error | HTTP Status | Message |
|-------|-------------|---------|
| Downstream service unavailable | `503` | Service temporarily unavailable |
| Generic/unexpected error | `500` | Internal server error |

---

## 10. Frontend (React SPA)

### 10.1 Project Structure

```
frontend/src/
├── App.js                  # Root component, routing, theme
├── index.js                # Entry point
├── components/
│   └── Navbar.js           # Top navigation bar
├── context/
│   └── AuthContext.js       # Auth state management (React Context)
├── pages/
│   ├── LoginPage.js         # Login & Signup forms (tabbed)
│   ├── SearchPage.js        # Salary search with filters, table, pagination, voting
│   ├── SubmitSalaryPage.js  # Anonymous salary submission form
│   └── StatsPage.js         # Statistics dashboard with tabs & comparison
└── services/
    └── api.js               # Axios instance & API function exports
```

### 10.2 Routes

| Route | Component | Description |
|-------|-----------|-------------|
| `/` | `SearchPage` | Homepage — salary search with filters and results table |
| `/submit` | `SubmitSalaryPage` | Anonymous salary submission form |
| `/stats` | `StatsPage` | Statistics dashboard with summary cards, grouped tabs, and comparison |
| `/login` | `LoginPage` | Login (email + password) and signup (username + email + password) |

### 10.3 API Client (`api.js`)

All API calls use a shared Axios instance with:
- `baseURL` set to `REACT_APP_API_URL` (or empty string for same-origin in production).
- A request interceptor that automatically attaches the JWT token from `localStorage` (key: `token`) to every request as `Authorization: Bearer <token>`.

```javascript
// Available API functions:
authApi.signup({ username, email, password })
authApi.login({ email, password })
authApi.validate()

salaryApi.submit({ jobTitle, company, country, ... })
salaryApi.getById(id)

voteApi.castVote({ submissionId, voteType })
voteApi.getVoteCounts(submissionId)

searchApi.search({ country, company, jobTitle, ..., page, size })
searchApi.getFilters()

statsApi.getSummary()
statsApi.getByRole()
statsApi.getByCompany()
statsApi.getByCountry()
statsApi.getByLevel()
statsApi.compare({ groupBy, values })
```

### 10.4 Auth Context (`AuthContext.js`)

Provides global authentication state via React Context:

- **On mount**: Checks `localStorage` for a stored token. If found, validates it via `GET /api/auth/validate`. If valid, restores the user session. If invalid, clears storage.
- **`login(userData)`**: Stores token and user data in `localStorage` and updates context state.
- **`logout()`**: Clears `localStorage` and resets context state.
- **`user` object**: `{ userId, username, token }` when logged in, `null` when not.

### 10.5 Page Details

#### SearchPage (`/`)

- On load: Fetches filter options via `GET /api/search/filters` and performs an initial search.
- Filters: Country (dropdown), Company (text), Job Title (text), Experience Level (dropdown), Employment Type (dropdown), Min/Max Salary (number), Tech Stack (text).
- Results displayed in an MUI `Table` with columns: Job Title, Company, Location, Level, Employment, Base Salary, Tech Stack, and optionally Vote (only if logged in).
- Pagination: MUI `TablePagination` component (5, 10, or 25 rows per page).
- Voting: Logged-in users see upvote/downvote icon buttons next to each entry. Clicking calls `POST /api/votes` then refreshes vote counts for that entry.

#### SubmitSalaryPage (`/submit`)

- An MUI form with fields: Job Title, Company, Country (default: Sri Lanka), City, Experience Level (dropdown), Employment Type (dropdown, default: FULL_TIME), Base Salary, Currency (default: LKR), Years of Experience, Tech Stack.
- On submit: Calls `POST /api/salaries`. Shows a success alert with the submission ID and a note about community approval.
- Form resets after successful submission.
- **No login required** — this is by design for anonymous submissions.

#### StatsPage (`/stats`)

- **Summary Cards**: Total Submissions, Average/Median/Min/Max Base Salary. Loaded from `GET /api/stats/summary`.
- **Grouped Stats Tabs**: By Role, By Company, By Country, By Level. Each tab loads data from the corresponding `/api/stats/by-*` endpoint. Displays a table with columns: Group Name, Count, Avg Salary, Median, Min, Max.
- **Comparison Panel**: Select a group-by dimension and enter comma-separated values to compare. Calls `GET /api/stats/compare`.

#### LoginPage (`/login`)

- Tabbed interface: **Login** tab and **Sign Up** tab.
- Login: Email + Password fields.
- Sign Up: Username + Email + Password fields.
- On successful signup, the returned token is used to auto-login (no separate login call needed).
- Error messages are shown in an MUI `Alert` component.

### 10.6 Nginx Configuration (Production)

In production (Docker), the React app is served by nginx:

```nginx
location / {
    try_files $uri $uri/ /index.html;   # SPA fallback
}

location /api/ {
    proxy_pass http://bff-service:8080;  # Proxy to BFF
}
```

---

## 11. Authentication & Authorization Flow

### Signup Flow

```
Browser                   BFF (:8080)              Identity (:8081)
  │                          │                          │
  ├─POST /api/auth/signup───▶│──POST /api/auth/signup──▶│
  │  {username,email,pass}   │                          │
  │                          │                          ├─ Hash password (BCrypt)
  │                          │                          ├─ Save to identity.users
  │                          │                          ├─ Generate JWT token
  │                          │◀──{userId,username,token}│
  │◀──{userId,username,token}│                          │
  │                          │                          │
  ├─ Store token in localStorage                        │
  ├─ Set user in AuthContext                            │
  └─ Redirect to /                                      │
```

### Login Flow

```
Browser                   BFF (:8080)              Identity (:8081)
  │                          │                          │
  ├─POST /api/auth/login────▶│──POST /api/auth/login───▶│
  │  {email, password}       │                          │
  │                          │                          ├─ Find user by email
  │                          │                          ├─ Verify BCrypt hash
  │                          │                          ├─ Generate JWT token
  │                          │◀──{userId,username,token}│
  │◀──{userId,username,token}│                          │
  │                          │                          │
  ├─ Store token in localStorage                        │
  └─ Redirect to /                                      │
```

### Protected Action (Voting)

```
Browser                   BFF (:8080)              Vote (:8083)        Salary (:8082)
  │                          │                        │                    │
  ├─POST /api/votes─────────▶│                        │                    │
  │  Header: Bearer <jwt>    │                        │                    │
  │  {submissionId,voteType} │                        │                    │
  │                          ├─ Validate JWT token     │                    │
  │                          ├─ Extract userId from sub│                    │
  │                          ├──POST /api/votes───────▶│                    │
  │                          │   X-User-Id: <userId>  │                    │
  │                          │                        ├─ Record vote        │
  │                          │                        ├─ Count upvotes      │
  │                          │                        │  if (upvotes ≥ 3):  │
  │                          │                        ├──PATCH status──────▶│
  │                          │                        │  {"status":"APPROVED"}
  │                          │◀──{voteResponse}───────│                    │
  │◀──{voteResponse}─────────│                        │                    │
```

---

## 12. Core Workflow

The BRD-mandated workflow is: **Submission → Voting → Approval → Search → Stats**.

### Step 1: Salary Submission

A user (anonymous, no login) submits salary data via the form at `/submit`.
- API: `POST /api/salaries`
- The submission is stored with `status = PENDING`.
- Response includes the submission `id`.

### Step 2: Community Voting

Logged-in users visit the search page (`/`). They can see all **approved** entries and can vote on them.
- But how do PENDING entries get votes? In the current implementation, PENDING entries are not visible in search results. The system relies on the seed data's approved entries or direct API calls for testing.
- When a user upvotes: `POST /api/votes` with `{ submissionId, voteType: "UPVOTE" }`.

### Step 3: Auto-Approval

When a submission accumulates `upvotes >= VOTE_THRESHOLD` (default: 3):
- The Vote Service calls `PATCH /api/salaries/{id}/status` with `{ "status": "APPROVED" }`.
- The status in the database changes from `PENDING` to `APPROVED`.

### Step 4: Search

Approved entries become visible in the Search Service.
- API: `GET /api/search` with various filters.
- Only `status = 'APPROVED'` entries are returned.

### Step 5: Statistics

Approved entries are included in statistical calculations.
- API: `GET /api/stats/summary`, `GET /api/stats/by-role`, etc.
- All stats endpoints only operate on `APPROVED` entries.

---

## 13. Configuration & Environment Variables

### Backend Services

All backend services read configuration from `application.properties` with environment variable overrides:

#### Identity Service (`:8081`)

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8081` | HTTP port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/salary_platform` | JDBC URL |
| `DATABASE_USERNAME` | `postgres` | DB username |
| `DATABASE_PASSWORD` | `postgres` | DB password |
| `JWT_SECRET` | `mySecretKeyForJwt...` | HMAC signing key |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Token expiration in ms |

#### Salary Service (`:8082`)

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8082` | HTTP port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/salary_platform` | JDBC URL (salary schema) |

#### Vote Service (`:8083`)

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8083` | HTTP port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/salary_platform` | JDBC URL (community schema) |
| `VOTE_THRESHOLD` | `3` | Upvotes needed for approval |
| `SALARY_SERVICE_URL` | `http://localhost:8082` | Salary service base URL |

#### Search Service (`:8084`)

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8084` | HTTP port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/salary_platform` | JDBC URL (salary schema read-only) |

#### Stats Service (`:8085`)

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8085` | HTTP port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/salary_platform` | JDBC URL (salary schema read-only) |

#### BFF Service (`:8080`)

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | HTTP port |
| `IDENTITY_SERVICE_URL` | `http://localhost:8081` | Identity service URL |
| `SALARY_SERVICE_URL` | `http://localhost:8082` | Salary service URL |
| `VOTE_SERVICE_URL` | `http://localhost:8083` | Vote service URL |
| `SEARCH_SERVICE_URL` | `http://localhost:8084` | Search service URL |
| `STATS_SERVICE_URL` | `http://localhost:8085` | Stats service URL |
| `JWT_SECRET` | same as Identity Service | Must match! |
| `CORS_ORIGINS` | `http://localhost:3000` | Allowed CORS origins (comma-separated) |

### Docker Compose Overrides

When running via `docker-compose.yml`, the following overrides are applied:

| Environment Variable | Docker Compose Value |
|---------------------|---------------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/salarydb?currentSchema=<schema>` |
| `SPRING_DATASOURCE_USERNAME` | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | `admin123` |
| `JWT_SECRET` | `YTJiY2RlZj...` (base64-encoded 64-byte key) |
| `CORS_ORIGINS` | `http://localhost:3000,http://localhost` |

### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| `REACT_APP_API_URL` | `""` (empty — same origin) | BFF base URL. Set to `http://localhost:8088` for local dev |

---

## 14. Local Development

### Quick Start

```bash
# Start everything via Docker Compose
docker compose up --build -d

# Access the app:
#   Frontend: http://localhost:3000
#   BFF API:  http://localhost:8088 (host port mapped from container 8080)
```

### Interactive Dev Script

The project includes an interactive `dev.sh` script:

```bash
./dev.sh
```

It will prompt you to choose:
1. **Backend**: Docker Compose (recommended) vs native Spring Boot (requires Java 17+).
2. **Frontend**: npm dev server with hot reload (recommended for development) vs Docker Compose.

If you choose native Spring Boot, each backend service runs as a separate background process with logs captured in `.dev-logs/`.

### Test Credentials (Seed Data)

| Email | Password | Username |
|-------|----------|----------|
| `testuser1@example.com` | `password123` | `testuser1` |
| `testuser2@example.com` | `password123` | `testuser2` |
| `testuser3@example.com` | `password123` | `testuser3` |

---

## 15. E2E Testing

### Setup

```bash
cd e2e
npm install
npx playwright install chromium
npx playwright install-deps chromium   # system dependencies
```

### Running Tests

Use the interactive runner:

```bash
./run-e2e.sh
```

Or run directly:

```bash
cd e2e
npm test                    # Run all tests
npm run test:headed         # Run with visible browser
npm run test:ui             # Open Playwright interactive UI
npm run report              # View last HTML report
```

### Test Suites

| File | Tests | Coverage |
|------|-------|----------|
| `auth.spec.js` | 7 | Login page, signup form, new user registration, seed user login, invalid credentials, logout, session persistence |
| `search.spec.js` | 11 | Homepage, table columns, seed data, company/job title/salary range filters, pagination, no results, vote columns (logged in vs anonymous), vote casting |
| `submit.spec.js` | 8 | Page load, form fields, default values, successful submission, form reset, experience levels, employment types, freelance submission |
| `stats.spec.js` | 10 | Summary cards, all four grouped tabs, comparison panel, company and role comparisons |
| `workflow.spec.js` | 4 | Full user journey (signup → submit → search → vote → stats → logout), navigation, seed user flow, anonymous access |
| **Total** | **40** | |

### Artifacts

Every test run generates:
- **Screenshots**: Captured at key points in every test (`test-results/screenshots/`).
- **Videos**: `.webm` recording of every test (`test-results/<test-name>/video.webm`).
- **Traces**: Playwright traces for debugging (`test-results/<test-name>/trace.zip`).
- **HTML Report**: `playwright-report/index.html` — interactive report with all results.
- **JSON Report**: `playwright-report/results.json` — machine-readable results.

### Playwright Configuration

Key settings in `playwright.config.js`:

```javascript
{
  timeout: 60000,            // 60s per test
  screenshot: 'on',          // Capture for every test
  video: 'on',               // Record every test
  trace: 'on',               // Full trace for every test
  workers: 1,                // Sequential execution (avoids DB conflicts)
  retries: 1,                // Retry failed tests once
}
```

---

## 16. Common Troubleshooting

### "502 Bad Gateway" from nginx

**Cause**: Duplicate `Transfer-Encoding: chunked` headers. The BFF proxy controllers were forwarding the downstream `ResponseEntity` directly, which included the downstream's `Transfer-Encoding` header. Spring Boot's Tomcat then added its own, resulting in duplicate headers that nginx 1.29+ rejects.

**Fix**: All BFF proxy controllers create a clean `ResponseEntity` with only the body and status code (no downstream headers).

### Login fails with 401/403

**Possible causes**:
1. Sending `username` instead of `email` in the login request. The `LoginRequest` DTO expects an `email` field.
2. Invalid BCrypt password hashes in seed data. Ensure the hash in `seed.sql` actually corresponds to the expected password.

### Search results table is empty

**Possible causes**:
1. Frontend reads `res.data.content` instead of `res.data.results`. The Search Service returns results in a field called `results`.
2. All submissions are still `PENDING`. Only `APPROVED` entries appear in search. Check the seed data or vote to approve some.

### Stats cards show "undefined"

**Possible cause**: Frontend reads `summary.count` instead of `summary.totalSubmissions`, or `row.groupKey` instead of `row.groupName`. Always check the exact field names from the API response.

### Salary submission returns 400

**Possible causes**:
1. Missing `yearsOfExperience` — it's `@NotNull` in the backend. Frontend must send `0` if the user leaves it blank.
2. Invalid `experienceLevel` — must be exactly one of: `JUNIOR`, `MID`, `SENIOR`, `LEAD`, `PRINCIPAL`.
3. Invalid `employmentType` — must be exactly one of: `FULL_TIME`, `PART_TIME`, `CONTRACT`, `FREELANCE`.

### CORS errors in the browser

**Possible cause**: The `CORS_ORIGINS` environment variable in the BFF doesn't include the frontend's origin. For Docker Compose, it should be `http://localhost:3000,http://localhost`. For local dev, at minimum `http://localhost:3000`.

### Port conflicts

The platform uses many ports. If you have other services (Traefik, etc.) on these ports, adjust via environment variables:

| Service | Default Port | Env Var |
|---------|-------------|---------|
| Frontend | `3000` (dev), `80` (prod) | `FRONTEND_PORT` |
| BFF | `8088` (host) → `8080` (container) | `BFF_PORT` |
| Identity | `8081` | `IDENTITY_PORT` |
| Salary | `8082` | `SALARY_PORT` |
| Vote | `8083` | `VOTE_PORT` |
| Search | `8084` | `SEARCH_PORT` |
| Stats | `8085` | `STATS_PORT` |
| PostgreSQL | `5432` | `POSTGRES_PORT` |
