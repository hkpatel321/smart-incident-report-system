# AWS Deployment Guide

## Architecture Overview

```
Internet
  │
  ▼
AWS ALB (Application Load Balancer)
  │
  ├──► ECS Fargate — incident-ingest-service     (port 8081)
  ├──► ECS Fargate — incident-processor-service  (port 8082)
  ├──► ECS Fargate — ai-rag-service              (port 8084)
  ├──► ECS Fargate — notification-service        (port 8083)
  └──► ECS Fargate — frontend (nginx)            (port 80)

Infrastructure:
  ├── Amazon RDS (PostgreSQL) — replaces local Postgres container
  ├── Amazon MSK (Kafka)      — replaces local Kafka container
  └── Amazon ECR              — Docker image registry
```

## Environment Variable Mapping

### Non-secret config → AWS Parameter Store (SSM)

| Local `.env` var | SSM Parameter Path                              |
| ---------------- | ----------------------------------------------- |
| `POSTGRES_DB`    | `/smartincident/prod/db/name`                   |
| `MAIL_HOST`      | `/smartincident/prod/mail/host`                 |
| `MAIL_PORT`      | `/smartincident/prod/mail/port`                 |
| `JWT_EXPIRY_MS`  | `/smartincident/prod/jwt/expiry`                |
| `ONCALL_EMAIL`   | `/smartincident/prod/notification/oncall-email` |

### Secrets → AWS Secrets Manager

| Local `.env` var    | Secrets Manager Secret Name         |
| ------------------- | ----------------------------------- |
| `POSTGRES_PASSWORD` | `smartincident/prod/db-password`    |
| `JWT_SECRET`        | `smartincident/prod/jwt-secret`     |
| `MAIL_PASSWORD`     | `smartincident/prod/mail-password`  |
| `GEMINI_API_KEY`    | `smartincident/prod/gemini-api-key` |

## ECS Task Definition — Environment Injection

In your ECS Task Definition JSON, reference SSM and Secrets Manager like this:

```json
{
  "containerDefinitions": [
    {
      "name": "incident-processor-service",
      "environment": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:postgresql://<RDS_ENDPOINT>:5432/incident_db"
        },
        { "name": "JWT_EXPIRY_MS", "value": "86400000" }
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:REGION:ACCOUNT:secret:smartincident/prod/db-password"
        },
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:REGION:ACCOUNT:secret:smartincident/prod/jwt-secret"
        },
        {
          "name": "GEMINI_API_KEY",
          "valueFrom": "arn:aws:secretsmanager:REGION:ACCOUNT:secret:smartincident/prod/gemini-api-key"
        }
      ]
    }
  ]
}
```

## Step-by-Step Deployment

1. **Push images to ECR:**

   ```bash
   aws ecr get-login-password | docker login --username AWS --password-stdin <ACCOUNT>.dkr.ecr.<REGION>.amazonaws.com
   docker-compose build
   docker tag smartincidencereportsystem-incident-processor-service:latest <ECR_REPO_URI>:latest
   docker push <ECR_REPO_URI>:latest
   ```

2. **Create RDS PostgreSQL** — enable `pgvector` extension after creation:

   ```sql
   CREATE EXTENSION IF NOT EXISTS vector;
   ```

3. **Create MSK Kafka cluster** — use the broker endpoint in place of `kafka:9092`.

4. **Create ECS Fargate cluster** — one service per container, using ALB target groups.

5. **Set up ALB routing rules:**
   - `/api/auth/**` → processor-service
   - `/api/dashboard/**` → processor-service
   - `/api/incidents` → ingest-service
   - `/api/ai/**` → ai-rag-service
   - `/api/notifications/**` → notification-service
   - `/*` → frontend

6. **Store secrets** in Secrets Manager with the paths listed above.

7. **Update Task Definitions** to inject secrets as shown above.

## Local → AWS Config Differences

| Component         | Local                | AWS                 |
| ----------------- | -------------------- | ------------------- |
| Kafka bootstrap   | `kafka:9092`         | MSK broker endpoint |
| DB URL            | `postgres:5432`      | RDS endpoint        |
| Service discovery | Docker network names | ALB path routing    |
| Secrets           | `.env` file          | Secrets Manager     |
| Images            | Local build          | ECR                 |
