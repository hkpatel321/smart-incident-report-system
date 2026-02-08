-- ============================================
-- Database Initialization Script
-- Smart Incident Response System
-- ============================================

-- Create schemas for each service
CREATE SCHEMA IF NOT EXISTS ingest;
CREATE SCHEMA IF NOT EXISTS processor;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS ai_rag;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA ingest TO incident_user;
GRANT ALL PRIVILEGES ON SCHEMA processor TO incident_user;
GRANT ALL PRIVILEGES ON SCHEMA notification TO incident_user;
GRANT ALL PRIVILEGES ON SCHEMA ai_rag TO incident_user;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pgvector for AI/RAG service (if available)
-- CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================
-- Note: Tables will be auto-created by JPA
-- This script sets up schemas and extensions
-- ============================================
