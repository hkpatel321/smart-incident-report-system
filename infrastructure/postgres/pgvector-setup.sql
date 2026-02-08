-- ============================================
-- pgvector Setup for AI RAG Service
-- Run once to enable vector extension
-- ============================================

-- Enable pgvector extension (requires superuser)
CREATE EXTENSION IF NOT EXISTS vector;

-- Create ai_rag schema if not exists
CREATE SCHEMA IF NOT EXISTS ai_rag;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA ai_rag TO incident_user;

-- Create knowledge_documents table with vector column
CREATE TABLE IF NOT EXISTS ai_rag.knowledge_documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    doc_type VARCHAR(30) NOT NULL,
    source_id VARCHAR(100),
    tags VARCHAR(500),
    embedding vector(768),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index for vector similarity search
CREATE INDEX IF NOT EXISTS idx_knowledge_embedding 
ON ai_rag.knowledge_documents 
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- Create index for document type
CREATE INDEX IF NOT EXISTS idx_knowledge_doc_type 
ON ai_rag.knowledge_documents(doc_type);

-- ============================================
-- Sample Data (Optional)
-- ============================================

-- Insert sample runbook
INSERT INTO ai_rag.knowledge_documents (title, content, doc_type, tags) VALUES
('Database Connection Pool Exhaustion', 
'## Problem
Database connections exhausted, causing application timeouts.

## Root Cause
- Connection leaks in application code
- Insufficient pool size
- Long-running queries holding connections

## Resolution Steps
1. Check current connection count: SELECT count(*) FROM pg_stat_activity;
2. Identify long-running queries: SELECT * FROM pg_stat_activity WHERE state != ''idle'';
3. Increase pool size in application.properties to 50
4. Set connection timeout to 30 seconds
5. Enable connection leak detection

## Prevention
- Implement connection pool monitoring
- Set up alerts for connection count > 80%
- Use try-with-resources for connections',
'RUNBOOK', 'database,connection,pool,timeout'),

('Kubernetes Pod OOMKilled', 
'## Problem
Pod terminated due to Out Of Memory (OOMKilled).

## Root Cause
- Memory limits too low
- Memory leak in application
- Spike in traffic causing high memory usage

## Resolution Steps
1. Check pod events: kubectl describe pod <name>
2. Review memory usage: kubectl top pod <name>
3. Increase memory limits in deployment.yaml
4. Redeploy: kubectl apply -f deployment.yaml
5. Monitor for recurrence

## Prevention
- Implement proper memory profiling
- Set realistic memory requests and limits
- Configure Horizontal Pod Autoscaler',
'RUNBOOK', 'kubernetes,oom,memory,pod'),

('API Gateway 502 Bad Gateway', 
'## Problem
Users receiving 502 Bad Gateway errors from API Gateway.

## Root Cause
- Backend service unhealthy
- Timeout configuration mismatch
- Network connectivity issues

## Resolution Steps
1. Check backend health: curl -I http://backend-service/health
2. Review gateway logs for upstream errors
3. Verify service discovery registration
4. Increase gateway timeout to 60s
5. Restart unhealthy backend pods

## Prevention
- Implement circuit breaker pattern
- Configure proper health checks
- Set up end-to-end monitoring',
'RUNBOOK', 'gateway,502,nginx,upstream');

-- Note: Embeddings need to be generated via the API after insertion
