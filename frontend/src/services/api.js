import axios from 'axios'
import { mockIncidents } from '../data/mockData'

/**
 * API Configuration
 * 
 * Backend Services:
 * - INGEST_API: POST incidents (port 8081)
 * - DASHBOARD_API: GET incidents list/detail (port 8082)
 * - RAG_API: AI resolution suggestions (port 8084)
 * 
 * Vite proxy handles routing in development.
 * In production, use environment variables or a gateway.
 */
const API_BASE_URL = import.meta.env.VITE_API_URL || ''

// =====================================================
// TOGGLE: Set to false for real API calls
// =====================================================
const USE_MOCK_DATA = false

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
})

// Attach JWT token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('authToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor for error handling
api.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

/**
 * Incident API Service
 * Provides CRUD operations for incidents
 */
export const incidentApi = {
  /**
   * Fetch all incidents with optional filters
   * Endpoint: GET /api/dashboard/incidents
   * @param {Object} filters - { severity, status, search, page, size }
   */
  async getIncidents(filters = {}) {
    if (USE_MOCK_DATA) {
      await new Promise(resolve => setTimeout(resolve, 300))
      let filtered = [...mockIncidents]
      if (filters.severity) filtered = filtered.filter(i => i.severity === filters.severity)
      if (filters.status) filtered = filtered.filter(i => i.status === filters.status)
      if (filters.search) {
        const term = filters.search.toLowerCase()
        filtered = filtered.filter(i => 
          i.title.toLowerCase().includes(term) || i.serviceName.toLowerCase().includes(term)
        )
      }
      return { content: filtered, totalElements: filtered.length, totalPages: 1 }
    }

    const params = new URLSearchParams()
    if (filters.severity) params.append('severity', filters.severity)
    if (filters.status) params.append('status', filters.status)
    if (filters.search) params.append('search', filters.search)
    if (filters.page !== undefined) params.append('page', filters.page)
    if (filters.size) params.append('size', filters.size)

    const response = await api.get(`/api/dashboard/incidents?${params}`)
    return response.data
  },

  /**
   * Fetch single incident by ID
   * Endpoint: GET /api/dashboard/incidents/:id
   */
  async getIncidentById(id) {
    if (USE_MOCK_DATA) {
      await new Promise(resolve => setTimeout(resolve, 200))
      const incident = mockIncidents.find(i => i.id === id)
      if (!incident) throw new Error('Incident not found')
      return incident
    }

    const response = await api.get(`/api/dashboard/incidents/${id}`)
    return response.data
  },

  /**
   * Create a new incident
   * Endpoint: POST /api/incidents
   */
  async createIncident(incidentData) {
    if (USE_MOCK_DATA) {
      await new Promise(resolve => setTimeout(resolve, 500))
      return {
        incidentId: `INC-2026-${String(mockIncidents.length + 1).padStart(3, '0')}`,
        status: 'ACCEPTED',
        message: 'Incident submitted successfully',
      }
    }

    const response = await api.post('/api/incidents', incidentData)
    return response.data
  },

  /**
   * Get AI resolution suggestion for an incident
   * Endpoint: POST /api/ai/resolve
   */
  async getAiSuggestion(incident) {
    if (USE_MOCK_DATA) {
      await new Promise(resolve => setTimeout(resolve, 1000))
      return {
        rootCause: 'Based on similar past incidents, the likely cause is resource exhaustion.',
        resolutionSteps: [
          'Check system resource utilization',
          'Review recent configuration changes',
          'Restart affected services',
          'Monitor for recurrence',
        ],
        preventionTips: ['Implement proactive monitoring', 'Set up auto-scaling'],
        confidence: 0.82,
      }
    }

    const response = await api.post('/api/ai/resolve', {
      incidentId: incident.id,
      title: incident.title,
      description: incident.description,
      category: incident.category,
      severity: incident.severity,
    }, {
      timeout: 60000, // 60s — Gemini API calls can take longer than the default 10s
    })
    return response.data
  },

  /**
   * Resolve an incident
   * Endpoint: PATCH /api/dashboard/incidents/:id/resolve
   */
  async resolveIncident(id, aiRecommendation = null) {
    const body = aiRecommendation ? { aiRecommendation } : {}
    const response = await api.patch(`/api/dashboard/incidents/${id}/resolve`, body)
    return response.data
  },

  /**
   * Update an existing incident
   * Endpoint: PUT /api/dashboard/incidents/:id
   */
  async updateIncident(id, data) {
    const response = await api.put(`/api/dashboard/incidents/${id}`, data);
    return response.data;
  },

  /**
   * Delete an incident
   * Endpoint: DELETE /api/dashboard/incidents/:id
   */
  async deleteIncident(id) {
    await api.delete(`/api/dashboard/incidents/${id}`);
  },

  /**
   * Assign an incident to a user (Admin only)
   * Endpoint: PATCH /api/dashboard/incidents/:id/assign
   */
  async assignIncident(id, assignedTo) {
    const response = await api.patch(`/api/dashboard/incidents/${id}/assign`, { assignedTo })
    return response.data
  }
}

/**
 * Auth API — login, register, me
 */
export const authApi = {
  async login(email, password) {
    const response = await api.post('/api/auth/login', { email, password })
    return response.data
  },
  async register(name, email, password, role) {
    const response = await api.post('/api/auth/register', { name, email, password, role })
    return response.data
  },
  async me() {
    const response = await api.get('/api/auth/me')
    return response.data
  }
}

export default api
