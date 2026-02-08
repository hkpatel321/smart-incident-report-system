# 🚨 Smart Incident Response System

A cloud-native, event-driven incident management platform with AI-powered resolution suggestions.

**Built with:** Java 21, Spring Boot, Apache Kafka, PostgreSQL + pgvector, React, Google Gemini AI

---

## 🏗️ Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────────┐     ┌─────────────┐
│  Frontend   │     │   Ingest    │     │   Processor     │     │  AI / RAG   │
│  React      │────▶│   :8081     │────▶│   :8082         │────▶│  :8084      │
│  :3000      │     │             │     │                 │     │  Gemini AI  │
└─────────────┘     └──────┬──────┘     └────────┬────────┘     └─────────────┘
                           │                     │                     │
                           ▼                     ▼                     ▼
                    ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
                    │   Kafka     │       │ PostgreSQL  │       │  pgvector   │
                    │   :9092     │       │   :5432     │       │  embeddings │
                    └─────────────┘       └─────────────┘       └─────────────┘
```

---

## 📁 Project Structure

```
SmartIncidenceReportSystem/
├── docker-compose.yml              # Full stack deployment
├── Dockerfile                      # Ingest service
├── infrastructure/
│   └── postgres/
│       ├── init.sql                # Schema setup
│       └── pgvector-setup.sql      # Vector extension
├── services/
│   ├── incident-processor-service/ # Kafka consumer + DB
│   └── ai-rag-service/             # Gemini AI + RAG
├── frontend/                       # React dashboard
│   ├── Dockerfile
│   └── src/
└── k8s/                            # Kubernetes manifests
```

---

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- (Optional) Java 21, Node.js 18+, Maven

### 1. Environment Setup

```bash
cp .env.example .env
# Edit .env and add your GEMINI_API_KEY
```

### 2. Start Everything

```bash
docker-compose up -d
```

### 3. Access Points

| Service           | URL                   |
| ----------------- | --------------------- |
| **Frontend**      | http://localhost:3000 |
| **Ingest API**    | http://localhost:8081 |
| **Dashboard API** | http://localhost:8082 |
| **AI/RAG API**    | http://localhost:8084 |
| **Kafka UI**      | http://localhost:8090 |

---

## 📡 API Endpoints

### Create Incident

```bash
POST http://localhost:8081/api/incidents
{
  "title": "Database Connection Timeout",
  "description": "Production DB timeout errors",
  "serviceName": "database-cluster",
  "category": "INFRASTRUCTURE",
  "severity": "HIGH",
  "source": "MANUAL"
}
```

### Get All Incidents

```bash
GET http://localhost:8082/api/dashboard/incidents
```

### Get AI Resolution

```bash
POST http://localhost:8084/api/ai/resolve
{
  "incidentId": "INC-001",
  "title": "...",
  "description": "...",
  "category": "INFRASTRUCTURE",
  "severity": "HIGH"
}
```

---

## 🛠️ Tech Stack

| Layer         | Technology                      |
| ------------- | ------------------------------- |
| Frontend      | React 18, Vite, Tailwind CSS    |
| API Gateway   | Nginx (reverse proxy)           |
| Backend       | Java 21, Spring Boot 4.0        |
| Messaging     | Apache Kafka                    |
| Database      | PostgreSQL 16 + pgvector        |
| AI            | Google Gemini API               |
| Containers    | Docker, Docker Compose          |
| Orchestration | Kubernetes (manifests included) |

---

## � Kafka Topics

| Topic              | Producer       | Consumer          |
| ------------------ | -------------- | ----------------- |
| `incident-created` | Ingest Service | Processor Service |

---

## 🐳 Docker Commands

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Rebuild after changes
docker-compose build --no-cache <service-name>

# Full reset (removes database)
docker-compose down -v
```

---
