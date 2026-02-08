import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Send, CheckCircle, AlertCircle } from 'lucide-react'
import { incidentApi } from '../services/api'
import { serviceOptions, categoryOptions } from '../data/mockData'

/**
 * Create/Simulate Incident Page
 * 
 * Simple form for submitting new incidents
 * Integrates with POST /api/incidents endpoint
 */
function CreateIncident() {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    serviceName: '',
    category: '',
    reporterEmail: '',
  })
  
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null) // { success: boolean, message: string }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setResult(null)

    try {
      const response = await incidentApi.createIncident(formData)
      setResult({
        success: true,
        message: `Incident ${response.incidentId} created successfully!`,
        incidentId: response.incidentId,
      })
      // Reset form
      setFormData({
        title: '',
        description: '',
        serviceName: '',
        category: '',
        reporterEmail: '',
      })
    } catch (err) {
      setResult({
        success: false,
        message: err.message || 'Failed to create incident. Please try again.',
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="p-8 max-w-2xl">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Create Incident</h1>
        <p className="text-gray-500 mt-1">Simulate a new incident for testing</p>
      </div>

      {/* Result Message */}
      {result && (
        <div
          className={`mb-6 px-4 py-3 rounded-lg flex items-center gap-3 ${
            result.success
              ? 'bg-green-50 border border-green-200 text-green-700'
              : 'bg-red-50 border border-red-200 text-red-700'
          }`}
        >
          {result.success ? (
            <CheckCircle className="w-5 h-5" />
          ) : (
            <AlertCircle className="w-5 h-5" />
          )}
          <span>{result.message}</span>
          {result.success && result.incidentId && (
            <Link
              to={`/incident/${result.incidentId}`}
              className="ml-auto text-sm underline"
            >
              View Incident
            </Link>
          )}
        </div>
      )}

      {/* Form */}
      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="space-y-6">
          {/* Title */}
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
              Incident Title *
            </label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              required
              placeholder="e.g., Database connection timeout"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            />
          </div>

          {/* Service Name */}
          <div>
            <label htmlFor="serviceName" className="block text-sm font-medium text-gray-700 mb-1">
              Service Name *
            </label>
            <select
              id="serviceName"
              name="serviceName"
              value={formData.serviceName}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            >
              <option value="">Select a service</option>
              {serviceOptions.map(service => (
                <option key={service} value={service}>{service}</option>
              ))}
            </select>
          </div>

          {/* Category */}
          <div>
            <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
              Incident Type *
            </label>
            <select
              id="category"
              name="category"
              value={formData.category}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            >
              <option value="">Select type</option>
              {categoryOptions.map(cat => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>
          </div>

          {/* Description */}
          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
              Description *
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              required
              rows={5}
              placeholder="Describe the incident in detail..."
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none resize-none"
            />
          </div>

          {/* Reporter Email */}
          <div>
            <label htmlFor="reporterEmail" className="block text-sm font-medium text-gray-700 mb-1">
              Reporter Email
            </label>
            <input
              type="email"
              id="reporterEmail"
              name="reporterEmail"
              value={formData.reporterEmail}
              onChange={handleChange}
              placeholder="email@company.com"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
            />
          </div>

          {/* Submit Button */}
          <div className="pt-4">
            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 font-medium transition-colors"
            >
              {loading ? (
                <>
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  Submitting...
                </>
              ) : (
                <>
                  <Send className="w-5 h-5" />
                  Submit Incident
                </>
              )}
            </button>
          </div>
        </div>
      </form>

      {/* API Integration Note */}
      <div className="mt-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
        <h3 className="text-sm font-medium text-gray-700 mb-2">API Integration</h3>
        <code className="text-xs text-gray-600">
          POST /api/incidents
        </code>
        <p className="text-xs text-gray-500 mt-1">
          Submits to the Incident Ingest Service (port 8081)
        </p>
      </div>
    </div>
  )
}

export default CreateIncident
