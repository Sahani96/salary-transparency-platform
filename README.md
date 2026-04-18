# Tech Salary Transparency Platform

A microservice-based platform enabling anonymous salary submissions, community-driven voting for data verification, and aggregated salary statistics for the Sri Lankan tech industry.

## Architecture

```
┌──────────────┐      ┌──────────────┐
│   Frontend   │────▶|   BFF Service │
│  (React/MUI) │      │   (:8080)    │
└──────────────┘      └────┬─────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
   ┌──────▼──────┐ ┌──────▼──────┐ ┌───────▼──────┐
   │  Identity   │ │   Salary    │ │    Vote      │
   │  Service    │ │   Service   │ │   Service    │
   │  (:8081)    │ │   (:8082)   │ │   (:8083)    │
   └─────────────┘ └─────────────┘ └──────────────┘
          │                │                │
   ┌──────▼──────┐ ┌──────▼──────┐          │
   │   Search    │ │   Stats     │          │
   │  Service    │ │   Service   │          │
   │  (:8084)    │ │   (:8085)   │          │
   └──────┬──────┘ └──────┬──────┘          │
          │                │                │
          └────────────────┼────────────────┘
                           │
                  ┌────────▼────────┐
                  │   PostgreSQL    │
                  │    (:5432)      │
                  │  3 schemas:     │
                  │  identity,      │
                  │  salary,        │
                  │  community      │
                  └─────────────────┘
```

## Tech Stack

| Layer | Technology |
| ------- | ----------- |
| Frontend | React 19, Material UI, React Router, Axios |
| BFF | Spring Boot 4.0.3 (Java 17) |
| Backend Services | Spring Boot 4.0.3, Spring Data JPA, Spring Security |
| Database | PostgreSQL 16 |
| Auth | JWT (jjwt 0.12.6), BCrypt passwords |
| Containerization | Docker, multi-stage builds |
| Orchestration | Kubernetes (k3s) |
| E2E Testing | Playwright |

## Services

| Service | Port | Database Schema | Description |
|---------|------|----------------|-------------|
| **BFF** | 8080 | - | API gateway, JWT validation, CORS, proxies to downstream |
| **Identity** | 8081 | `identity` | User signup/login, JWT token generation |
| **Salary** | 8082 | `salary` | CRUD for salary submissions |
| **Vote** | 8083 | `community` | Upvote/downvote submissions, auto-approve on threshold |
| **Search** | 8084 | `salary` (read-only) | Dynamic filtered search with pagination |
| **Stats** | 8085 | `salary` (read-only) | Aggregated statistics with median/percentile |
| **Frontend** | 3000/80 | - | React SPA with Material UI |

## Prerequisites

- **Java 17+** (for backend services)
- **Node.js 18+** (for frontend)
- **PostgreSQL 16** (local or Docker)
- **Docker & Docker Compose** (for containerized deployment)
- **kubectl + k3s** (for Kubernetes deployment)

## Quick Start (Docker Compose)

```bash
# Clone the repository
git clone <repo-url>
cd salary-transparency-platform

# Start all services with Docker Compose
docker-compose up --build

# Access the application
# Frontend: http://localhost
# API: http://localhost:8080
```

### Default Test Users (from seed data)

| Username | Password | Email |
|----------|----------|-------|
| johndoe | password123 | <john@example.com> |
| janesmith | password123 | <jane@example.com> |
| devuser | password123 | <dev@example.com> |

## Local Development

### Database Setup

```bash
# Start PostgreSQL (Docker)
docker run -d --name salary-postgres \
  -e POSTGRES_DB=salarydb \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin123 \
  -p 5432:5432 \
  postgres:16-alpine

# Initialize schemas and seed data
psql -h localhost -U admin -d salarydb -f db/init.sql
psql -h localhost -U admin -d salarydb -f db/seed.sql
```

### Backend Services

Each service can be started independently:

```bash
# Identity Service
cd backend/identity-service
./mvnw spring-boot:run

# Salary Service
cd backend/salary-service
./mvnw spring-boot:run

# Vote Service (requires salary-service running)
cd backend/vote-service
./mvnw spring-boot:run

# Search Service
cd backend/search-service
./mvnw spring-boot:run

# Stats Service
cd backend/stats-service
./mvnw spring-boot:run

# BFF Service (requires all above running)
cd backend/bff-service
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
# Opens at http://localhost:3000
```

### Environment Variables

All services are configured via environment variables with sensible defaults:

| Variable | Default | Used By |
|----------|---------|---------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/salarydb?currentSchema=<schema>` | All backend |
| `SPRING_DATASOURCE_USERNAME` | `admin` | All backend |
| `SPRING_DATASOURCE_PASSWORD` | `admin123` | All backend |
| `JWT_SECRET` | (64-byte base64 key) | Identity, BFF |
| `JWT_EXPIRATION` | `86400000` (24h) | Identity |
| `VOTE_THRESHOLD` | `3` | Vote |
| `SALARY_SERVICE_URL` | `http://localhost:8082` | Vote |
| `IDENTITY_SERVICE_URL` | `http://localhost:8081` | BFF |
| `SEARCH_SERVICE_URL` | `http://localhost:8084` | BFF |
| `STATS_SERVICE_URL` | `http://localhost:8085` | BFF |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | BFF |
| `REACT_APP_API_URL` | `http://localhost:8080` | Frontend |

## API Endpoints

### Auth (via BFF)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/auth/validate` | Validate JWT token |

### Salaries (via BFF)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/salaries` | Submit salary entry |
| GET | `/api/salaries/{id}` | Get submission by ID |

### Votes (via BFF, requires JWT)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/votes` | Cast upvote/downvote |
| GET | `/api/votes/submission/{id}` | Get vote counts |

### Search (via BFF)

| Method | Endpoint | Query Params | Description |
|--------|----------|-------------|-------------|
| GET | `/api/search` | country, company, jobTitle, experienceLevel, employmentType, minSalary, maxSalary, techStack, sortBy, sortDirection, page, size | Search with filters |
| GET | `/api/search/filters` | - | Available filter options |

### Statistics (via BFF)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stats/summary` | Overall statistics |
| GET | `/api/stats/by-role` | Grouped by job title |
| GET | `/api/stats/by-company` | Grouped by company |
| GET | `/api/stats/by-country` | Grouped by country |
| GET | `/api/stats/by-level` | Grouped by experience level |
| GET | `/api/stats/compare` | Compare groups (params: groupBy, values) |

## Kubernetes Deployment (k3s)

### Prerequisites for k3s

```bash
# Install k3s
curl -sfL https://get.k3s.io | sh -

# Start local registry (for images)
docker run -d -p 5000:5000 --restart=always --name registry registry:2
```

### Deploy

```bash
chmod +x deploy.sh
./deploy.sh
```

### Access

- Frontend: `http://<SERVER_IP>:30080`
- BFF API: Port-forward or expose via NodePort

### K8s Manifests

```
k8s/
├── namespaces.yaml      # salary-app, salary-data
├── config.yaml          # ConfigMap + Secrets
├── postgres.yaml        # PostgreSQL + PVC + init scripts
├── identity-service.yaml
├── salary-service.yaml
├── vote-service.yaml
├── search-service.yaml
├── stats-service.yaml
├── bff-service.yaml
└── frontend.yaml        # NodePort on 30080
```

## E2E Testing (Playwright)

```bash
cd e2e
npm install
npx playwright install chromium

# Run tests (requires app running)
npm test

# Run with browser visible
npm run test:headed

# View report
npm run report
```

### Test Suites

- **auth.spec.js** — Signup, login, logout, error handling
- **search.spec.js** — Search page, filters, pagination, voting
- **submit.spec.js** — Salary submission form, validation
- **stats.spec.js** — Statistics dashboard, tabs, comparison
- **workflow.spec.js** — Complete user journey: signup → submit → search → stats → logout

## Project Structure

```
salary-transparency-platform/
├── BRD.md                    # Business Requirements Document
├── README.md                 # This file
├── docker-compose.yml        # Docker Compose for all services
├── deploy.sh                 # K8s deployment script
├── db/
│   ├── init.sql              # Schema creation
│   └── seed.sql              # Sample data
├── backend/
│   ├── bff-service/          # API Gateway (Spring Boot)
│   ├── identity-service/     # Authentication (Spring Boot)
│   ├── salary-service/       # Salary CRUD (Spring Boot)
│   ├── vote-service/         # Voting system (Spring Boot)
│   ├── search-service/       # Search & filter (Spring Boot)
│   └── stats-service/        # Statistics (Spring Boot)
├── frontend/                 # React SPA
│   ├── src/
│   │   ├── components/       # Shared components (Navbar)
│   │   ├── context/          # Auth context
│   │   ├── pages/            # Route pages
│   │   └── services/         # API client
│   ├── nginx.conf            # Production nginx config
│   └── Dockerfile
├── k8s/                      # Kubernetes manifests
│   ├── namespaces.yaml
│   ├── config.yaml
│   ├── postgres.yaml
│   └── *-service.yaml
└── e2e/                      # Playwright E2E tests
    ├── playwright.config.js
    └── tests/
```

## Key Design Decisions

- **Community voting** instead of admin moderation — submissions require configurable vote threshold (default 3) for auto-approval
- **Schema-per-domain** PostgreSQL — identity, salary, community schemas in single database
- **BFF pattern** — single entry point handles CORS, JWT validation, and request routing
- **Anonymization** — submitters can choose to hide company/city; search results respect this
- **Read-only services** — Search and Stats services read the salary schema without write access

## License

MIT
