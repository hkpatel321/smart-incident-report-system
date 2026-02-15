/**
 * Mock incident data for development/demo purposes.
 * Replace with actual API calls in production.
 */
export const mockIncidents = [
  {
    id: 'INC-2026-001',
    title: 'Database Connection Pool Exhausted',
    description: 'Production PostgreSQL database showing connection timeout errors. Multiple services affected including user-service and order-service. Connection pool appears to be exhausted with 50/50 connections in use.',
    serviceName: 'database-cluster',
    category: 'INFRASTRUCTURE',
    severity: 'CRITICAL',
    status: 'NEW',
    reporterEmail: 'ops-team@company.com',
    source: 'PROMETHEUS',
    createdAt: '2026-02-07T14:30:00Z',
    processedAt: null,
    resolvedAt: null,
    logs: `[2026-02-07 14:30:01] ERROR: Connection pool exhausted
[2026-02-07 14:30:02] WARN: Waiting for available connection...
[2026-02-07 14:30:05] ERROR: Connection timeout after 3000ms
[2026-02-07 14:30:06] ERROR: Failed to execute query: SELECT * FROM users`,
    aiSuggestion: null,
  },
  {
    id: 'INC-2026-002',
    title: 'API Gateway 502 Bad Gateway',
    description: 'Users experiencing 502 errors when accessing the main application. Error rate spiked to 15% in the last 10 minutes.',
    serviceName: 'api-gateway',
    category: 'NETWORK',
    severity: 'HIGH',
    status: 'PROCESSING',
    reporterEmail: 'monitoring@company.com',
    source: 'DATADOG',
    createdAt: '2026-02-07T13:45:00Z',
    processedAt: '2026-02-07T13:50:00Z',
    resolvedAt: null,
    logs: `[2026-02-07 13:45:00] ERROR: upstream connect error
[2026-02-07 13:45:01] WARN: Circuit breaker OPEN for user-service
[2026-02-07 13:45:02] ERROR: 502 Bad Gateway returned to client`,
    aiSuggestion: {
      rootCause: 'Backend service health check failures causing upstream connection errors.',
      steps: [
        'Check backend service health endpoints',
        'Verify network connectivity between gateway and services',
        'Increase gateway timeout to 60 seconds',
        'Restart unhealthy backend pods',
      ],
      confidence: 0.85,
    },
  },
  {
    id: 'INC-2026-003',
    title: 'Kubernetes Pod OOMKilled',
    description: 'Payment service pod terminated due to OutOfMemory. Auto-restart occurring but service degraded.',
    serviceName: 'payment-service',
    category: 'INFRASTRUCTURE',
    severity: 'HIGH',
    status: 'ASSIGNED',
    reporterEmail: 'k8s-alerts@company.com',
    source: 'KUBERNETES',
    createdAt: '2026-02-07T12:00:00Z',
    processedAt: '2026-02-07T12:05:00Z',
    resolvedAt: null,
    logs: `[2026-02-07 12:00:00] OOMKilled: Container exceeded memory limit
[2026-02-07 12:00:01] Pod payment-service-7d8f9 terminated
[2026-02-07 12:00:05] Pod payment-service-8e9g0 starting...`,
    aiSuggestion: {
      rootCause: 'Memory leak in payment processing causing gradual memory increase until OOM.',
      steps: [
        'Increase memory limit from 512Mi to 1Gi',
        'Enable heap dump on OOM for debugging',
        'Review recent code changes for memory leaks',
        'Consider implementing memory profiling',
      ],
      confidence: 0.78,
    },
  },
  {
    id: 'INC-2026-004',
    title: 'SSL Certificate Expiring Soon',
    description: 'SSL certificate for api.company.com expires in 7 days. Automated renewal failed.',
    serviceName: 'cert-manager',
    category: 'SECURITY',
    severity: 'MEDIUM',
    status: 'RESOLVED',
    reporterEmail: 'security@company.com',
    source: 'CERTBOT',
    createdAt: '2026-02-01T09:00:00Z',
    processedAt: '2026-02-01T09:10:00Z',
    resolvedAt: '2026-02-02T11:30:00Z',
    logs: `[2026-02-01 09:00:00] WARN: Certificate expires in 7 days
[2026-02-01 09:05:00] ERROR: Auto-renewal failed: DNS challenge timeout`,
    aiSuggestion: null,
  },
  {
    id: 'INC-2026-005',
    title: 'Disk Space Warning on Log Server',
    description: 'Log aggregation server disk usage at 85%. Predicted to fill within 48 hours.',
    serviceName: 'log-server',
    category: 'INFRASTRUCTURE',
    severity: 'LOW',
    status: 'CLOSED',
    reporterEmail: 'infra@company.com',
    source: 'NAGIOS',
    createdAt: '2026-01-28T16:00:00Z',
    processedAt: '2026-01-28T16:30:00Z',
    resolvedAt: '2026-01-29T10:00:00Z',
    logs: `[2026-01-28 16:00:00] WARN: Disk /var/log at 85% capacity`,
    aiSuggestion: null,
  },
]

/**
 * Service options for the create incident form
 */
export const serviceOptions = [
  'api-gateway',
  'user-service',
  'payment-service',
  'order-service',
  'database-cluster',
  'cache-redis',
  'message-queue',
  'log-server',
  'cert-manager',
]

/**
 * Incident type/category options
 */
export const categoryOptions = [
  'INFRASTRUCTURE',
  'NETWORK',
  'SECURITY',
  'APPLICATION',
  'DATABASE',
  'PERFORMANCE',
]

/**
 * Severity levels
 */
export const severityLevels = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

/**
 * Status options
 */
export const statusOptions = ['NEW', 'PROCESSING', 'ASSIGNED', 'RESOLVED', 'CLOSED']

/**
 * Source options for incidents
 */
export const sourceOptions = ['MANUAL', 'PROMETHEUS', 'DATADOG', 'CLOUDWATCH', 'KUBERNETES', 'NAGIOS', 'CUSTOM']
