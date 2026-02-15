@echo off
REM ============================================
REM Seed RAG Knowledge Base with Sample Documents
REM ============================================
REM This script populates the knowledge_documents table
REM with runbooks, past incidents, and KB articles
REM so RAG vector similarity search returns real results.
REM
REM Usage: Run from project root after ai-rag-service is running.
REM        seed-knowledge.bat
REM ============================================

set RAG_URL=http://localhost:8084

echo.
echo ============================================
echo  Seeding RAG Knowledge Base
echo ============================================
echo.

REM --- RUNBOOK 1: CPU Spike Response ---
echo [1/8] Ingesting: CPU Spike Response Runbook
curl -s -X POST %RAG_URL%/api/documents ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"CPU Spike Response Runbook\",\"content\":\"CPU Spike Response Procedure:\n\n1. DETECTION: Alert triggered when CPU usage exceeds 90%% for more than 5 minutes on any service node.\n\n2. IMMEDIATE ACTIONS:\n   a. Check top processes: Run 'top -o %%CPU' or 'htop' to identify resource-hungry processes.\n   b. Check recent deployments: Review deployment history in CI/CD pipeline for recent changes.\n   c. Review application logs for error spikes or infinite loops.\n   d. Check for traffic spikes in load balancer metrics.\n\n3. MITIGATION:\n   a. If caused by traffic spike: Enable auto-scaling and increase instance count.\n   b. If caused by runaway process: Restart the affected service gracefully.\n   c. If caused by code regression: Roll back to previous stable deployment.\n   d. If caused by a memory leak leading to GC overhead: Increase heap size temporarily and schedule fix.\n\n4. RESOLUTION:\n   a. Identify root cause through APM traces and profiling.\n   b. Apply permanent fix (code optimization, caching, scaling rules).\n   c. Update monitoring thresholds if needed.\n   d. Document incident and resolution in knowledge base.\n\n5. PREVENTION:\n   - Set up auto-scaling policies with appropriate thresholds.\n   - Implement circuit breakers for downstream dependencies.\n   - Add CPU usage alerts at 70%%, 80%%, and 90%% thresholds.\n   - Conduct regular load testing.\",\"docType\":\"RUNBOOK\",\"tags\":\"cpu,performance,scaling,runbook\"}"
echo.

REM --- RUNBOOK 2: Database Connection Pool Exhaustion ---
echo [2/8] Ingesting: Database Connection Pool Exhaustion Runbook
curl -s -X POST %RAG_URL%/api/documents ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Database Connection Pool Exhaustion Runbook\",\"content\":\"Database Connection Pool Exhaustion Response:\n\n1. SYMPTOMS:\n   - Application throws 'Cannot acquire connection from pool' errors.\n   - Response times increase significantly.\n   - Database shows many idle connections.\n\n2. IMMEDIATE DIAGNOSIS:\n   a. Check active connections: SELECT count(*) FROM pg_stat_activity;\n   b. Check pool metrics via actuator endpoint /actuator/metrics/hikaricp.connections.active\n   c. Look for long-running queries: SELECT pid, now() - pg_stat_activity.query_start AS duration, query FROM pg_stat_activity WHERE state != 'idle' ORDER BY duration DESC;\n\n3. MITIGATION:\n   a. Kill long-running queries: SELECT pg_terminate_backend(pid);\n   b. Temporarily increase pool size: Set spring.datasource.hikari.maximum-pool-size to a higher value.\n   c. Restart affected service instances if connections are leaked.\n\n4. ROOT CAUSE INVESTIGATION:\n   - Check for missing connection.close() or try-with-resources blocks.\n   - Look for transactions left open without commit/rollback.\n   - Review N+1 query patterns in ORM.\n   - Check for connection timeout settings being too high.\n\n5. PREVENTION:\n   - Configure connection leak detection: spring.datasource.hikari.leak-detection-threshold=30000\n   - Set appropriate pool size (typically 10-20 per service instance).\n   - Implement query timeout limits.\n   - Monitor connection pool utilization metrics.\",\"docType\":\"RUNBOOK\",\"tags\":\"database,connection-pool,postgresql,runbook\"}"
echo.

REM --- RUNBOOK 3: Memory Leak Investigation ---
echo [3/8] Ingesting: Memory Leak Investigation Runbook
curl -s -X POST %RAG_URL%/api/documents ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Memory Leak Investigation Runbook\",\"content\":\"Memory Leak Investigation and Resolution:\n\n1. DETECTION:\n   - JVM heap usage grows continuously without returning to baseline after GC.\n   - OutOfMemoryError exceptions in application logs.\n   - Container OOMKilled events in Kubernetes.\n\n2. IMMEDIATE ACTIONS:\n   a. Capture heap dump: jmap -dump:live,format=b,file=heapdump.hprof <pid>\n   b. Check GC logs: Look for Full GC frequency and pause times.\n   c. Monitor memory metrics: /actuator/metrics/jvm.memory.used\n   d. If critical: Restart the affected instance and route traffic to healthy nodes.\n\n3. ANALYSIS:\n   a. Analyze heap dump with Eclipse MAT or VisualVM.\n   b. Look for dominator tree — largest retained objects.\n   c. Check for common leak patterns:\n      - Collections that grow unbounded (Maps, Lists used as caches).\n      - Listeners or callbacks not being deregistered.\n      - ThreadLocal variables not cleaned up.\n      - Static references holding large object graphs.\n\n4. RESOLUTION:\n   - Fix the leaking code path.\n   - Add proper resource cleanup (close(), remove(), deregister()).\n   - Implement bounded caches with TTL (e.g., Caffeine, Guava).\n   - Set appropriate JVM heap limits: -Xmx and -Xms.\n\n5. PREVENTION:\n   - Enable GC logging in production.\n   - Set up memory usage alerts at 70%% and 85%% of max heap.\n   - Regular profiling during load tests.\n   - Code review checklist for resource management.\",\"docType\":\"RUNBOOK\",\"tags\":\"memory,leak,jvm,heap,runbook\"}"
echo.

REM --- PAST INCIDENT 1: API Gateway CPU Spike ---
echo [4/8] Ingesting: Past Incident - API Gateway CPU Spike
curl -s -X POST %RAG_URL%/api/documents ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Past Incident: API Gateway CPU Spike During Product Launch\",\"content\":\"Incident Report INC-2025-042:\n\nTitle: API Gateway CPU Spike During Product Launch\nSeverity: CRITICAL\nDuration: 45 minutes\nDate: 2025-06-15\n\nSummary: API Gateway experienced sustained 98%% CPU usage during a marketing campaign launch, causing timeout errors for 30%% of user requests.\n\nRoot Cause: A sudden 5x traffic increase from the marketing campaign overwhelmed the API Gateway. The auto-scaling policy had a 10-minute cooldown period, which was too slow. Additionally, a regex-based request validation rule was computationally expensive under high load.\n\nResolution Steps:\n1. Immediately scaled API Gateway instances from 3 to 8 manually.\n2. Disabled the expensive regex validation temporarily.\n3. Reduced auto-scaling cooldown from 10 minutes to 2 minutes.\n4. Optimized the regex pattern to use non-backtracking approach.\n5. Added caching layer for repeated validation patterns.\n\nPrevention Measures:\n- Pre-scale services before known traffic events.\n- Set auto-scaling cooldown to 2 minutes maximum.\n- Audit regex patterns for ReDoS vulnerabilities.\n- Implement request rate limiting at the gateway level.\n- Add load testing for 10x expected traffic.\",\"docType\":\"PAST_INCIDENT\",\"sourceId\":\"INC-2025-042\",\"tags\":\"cpu,api-gateway,traffic,scaling,past-incident\"}"
echo.

REM --- PAST INCIDENT 2: Database Connection Timeout ---
echo [5/8] Ingesting: Past Incident - Database Connection Timeout
curl -s -X POST %RAG_URL%/api/documents ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Past Incident: Database Connection Timeout Storm\",\"content\":\"Incident Report INC-2025-078:\n\nTitle: Database Connection Timeout Storm\nSeverity: HIGH\nDuration: 2 hours\nDate: 2025-09-22\n\nSummary: Multiple microservices started failing with database connection timeouts simultaneously. The PostgreSQL connection pool was exhausted across all services.\n\nRoot Cause: A scheduled batch job was holding 50 long-running transactions without committing, consuming all available connections in the shared pool. Other services could not acquire connections, creating a cascading failure.\n\nResolution Steps:\n1. Identified the batch job holding connections via pg_stat_activity.\n2. Terminated long-running queries using pg_terminate_backend().\n3. Restarted the batch service with corrected transaction boundaries.\n4. Implemented separate connection pools for batch vs real-time workloads.\n5. Added connection timeout of 30 seconds to prevent indefinite holds.\n\nPrevention Measures:\n- Separate connection pools for batch and OLTP workloads.\n- Set statement_timeout in PostgreSQL for long-running queries.\n- Monitor connection pool utilization per service.\n- Implement circuit breakers between services and database.\n- Add alerts for connection pool usage above 80%%.\",\"docType\":\"PAST_INCIDENT\",\"sourceId\":\"INC-2025-078\",\"tags\":\"database,connection,timeout,postgresql,past-incident\"}"
echo.

REM --- PAST INCIDENT 3: Service Outage Due to Memory Leak ---
echo [6/9] Ingesting: Past Incident - Microservice Memory Leak
curl -s -X POST %RAG_URL%/api/documents ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Past Incident: Payment Service Memory Leak\",\"content\":\"Incident Report INC-2025-091:\n\nTitle: Payment Service Memory Leak Causing OOM Crashes\nSeverity: CRITICAL\nDuration: 4 hours\nDate: 2025-11-03\n\nSummary: The Payment Service experienced repeated OOMKilled events in Kubernetes. Each pod restart only provided temporary relief for about 30 minutes before crashing again.\n\nRoot Cause: A recently deployed feature was caching payment session objects in a ConcurrentHashMap without any eviction policy. Each session consumed approximately 2MB. Under normal traffic of 100 requests/minute, the cache grew by 200MB every minute.\n\nResolution Steps:\n1. Identified growing heap via Prometheus + Grafana memory metrics.\n2. Captured heap dump and analyzed with Eclipse MAT.\n3. Found ConcurrentHashMap holding 15,000+ PaymentSession objects.\n4. Rolled back to previous version as immediate fix.\n5. Replaced unbounded HashMap with Caffeine cache (max 1000 entries, 5-minute TTL).\n6. Added proper cleanup in PaymentSession.close() method.\n\nPrevention Measures:\n- Never use unbounded in-memory caches in production.\n- Always use bounded caches with TTL (Caffeine, Redis).\n- Set JVM heap limits matching container memory limits.\n- Add memory usage alerts with trend detection.\n- Include memory profiling in integration test suite.\",\"docType\":\"PAST_INCIDENT\",\"sourceId\":\"INC-2025-091\",\"tags\":\"memory,leak,oom,cache,payment,past-incident\"}"
echo.

REM --- RUNBOOK 4: AWS EC2 Integration Troubleshooting ---
echo [7/9] Ingesting: AWS EC2 Integration Troubleshooting Guide
curl -s -X POST %RAG_URL%/api/documents ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"AWS EC2 Integration Troubleshooting Guide\",\"content\":\"Runbook for troubleshooting AWS EC2 integration failures: 1. IAM Permissions: Verify the service IAM role has ec2:DescribeInstances and ec2:RunInstances permissions. Check for explicit Deny policies. 2. Network Connectivity: Ensure Security Groups allow inbound traffic on required ports (e.g., 443 for API, 22 for SSH). Check NACLs and Route Tables for subnet isolation. 3. Instance State: Verify target instances are in RUNNING state. 4. API Throttling: Check CloudWatch for RequestLimitExceeded errors. Implement exponential backoff. 5. Credential Chain: Verify temporary credentials are not expired. Check logs for 403 Forbidden or 401 Unauthorized errors.\",\"docType\":\"RUNBOOK\",\"tags\":\"aws,ec2,cloud,integration,iam,network,security-group\"}"
echo.

REM --- KB ARTICLE 1: Monitoring Best Practices ---
echo [8/9] Ingesting: KB Article - Monitoring and Alerting Best Practices
curl -s -X POST %RAG_URL%/api/documents ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Monitoring and Alerting Best Practices\",\"content\":\"Comprehensive Monitoring and Alerting Guide:\n\n1. THE FOUR GOLDEN SIGNALS (Google SRE):\n   - Latency: Time to serve a request (p50, p95, p99).\n   - Traffic: Demand on the system (requests per second).\n   - Errors: Rate of failed requests (5xx errors, timeouts).\n   - Saturation: How full the system is (CPU, memory, disk, connections).\n\n2. ALERT THRESHOLDS:\n   - CPU: Warning at 70%%, Critical at 90%% (sustained 5+ minutes).\n   - Memory: Warning at 75%%, Critical at 85%%.\n   - Disk: Warning at 70%%, Critical at 85%%.\n   - Error Rate: Warning at 1%%, Critical at 5%%.\n   - Latency p99: Warning at 2x baseline, Critical at 5x baseline.\n\n3. DASHBOARD DESIGN:\n   - Overview dashboard: System-wide health.\n   - Service dashboard: Per-service metrics.\n   - Infrastructure dashboard: Nodes, containers, network.\n   - Business dashboard: Key business metrics.\n\n4. TOOLS:\n   - Metrics: Prometheus + Grafana.\n   - Logging: ELK Stack or Loki.\n   - Tracing: Jaeger or Zipkin.\n   - Alerting: PagerDuty, Alertmanager.\n\n5. BEST PRACTICES:\n   - Alert on symptoms, not causes.\n   - Every alert should be actionable.\n   - Maintain runbooks for every alert.\n   - Regularly review and prune alert rules.\n   - Use correlation IDs across services for tracing.\",\"docType\":\"KB_ARTICLE\",\"tags\":\"monitoring,alerting,sre,best-practices,observability\"}"
echo.

REM --- KB ARTICLE 2: Auto-Scaling Configuration ---
  -d "{\"title\":\"Auto-Scaling Configuration Guide for Microservices\",\"content\":\"Auto-Scaling Configuration Guide:\n\n1. HORIZONTAL POD AUTOSCALER (HPA):\n   - Target CPU utilization: 60-70%% (leave headroom for spikes).\n   - Min replicas: 2 (for high availability).\n   - Max replicas: Based on capacity planning (typically 10-20).\n   - Scale-up cooldown: 1-2 minutes.\n   - Scale-down cooldown: 5-10 minutes (prevent flapping).\n\n2. VERTICAL POD AUTOSCALER (VPA):\n   - Use in 'recommend' mode first to observe.\n   - Set resource requests based on VPA recommendations.\n   - Avoid using VPA and HPA together on CPU metrics.\n\n3. CLUSTER AUTOSCALER:\n   - Enable for production clusters.\n   - Set appropriate node pool boundaries.\n   - Use preemptible/spot instances for non-critical workloads.\n\n4. APPLICATION-LEVEL SCALING:\n   - Connection pool sizes: Scale with instance count.\n   - Queue consumer concurrency: Match to available CPU.\n   - Cache sizes: Consider memory limits when scaling.\n\n5. LOAD TESTING:\n   - Test auto-scaling with realistic traffic patterns.\n   - Validate scale-up time meets SLA requirements.\n   - Test scale-down behavior to avoid premature termination.\n   - Use tools like k6, Locust, or Gatling.\n\n6. COMMON PITFALLS:\n   - Too-aggressive scale-down causing flapping.\n   - Not account for JVM warm-up time.\n   - Connection pool exhaustion during rapid scale-out.\n   - Not setting PodDisruptionBudget for graceful scaling.\",\"docType\":\"KB_ARTICLE\",\"tags\":\"scaling,autoscaling,kubernetes,hpa,configuration\"}"
echo.

echo ============================================
echo  Verifying ingested documents...
echo ============================================
curl -s %RAG_URL%/api/documents | python -c "import sys,json; docs=json.load(sys.stdin); print(f'Total documents in knowledge base: {len(docs)}'); [print(f'  - [{d[\"docType\"]}] {d[\"title\"]}') for d in docs]" 2>nul || curl -s %RAG_URL%/api/documents
echo.
echo ============================================
echo  RAG Knowledge Base seeded successfully!
echo ============================================
