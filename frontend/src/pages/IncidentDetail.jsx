import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { ArrowLeft, Clock, AlertCircle, CheckCircle, CheckCircle2, Sparkles, Loader2, Trash2, Edit, UserCheck } from 'lucide-react'
import { format } from 'date-fns'
import { incidentApi } from '../services/api'
import { SeverityBadge, StatusBadge } from '../components/Badge'
import EditIncidentModal from '../components/EditIncidentModal'
import { useAuth } from '../context/AuthContext'

/**
 * Incident Detail Page
 * 
 * Features:
 * - Full incident description
 * - Log snippet (read-only)
 * - Severity & status highlighted
 * - AI-generated resolution suggestion
 * - Incident timeline
 * - Edit/Delete Management
 */
function IncidentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user, isAdmin } = useAuth()
  const [incident, setIncident] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  
  // AI Suggestion state
  const [aiSuggestion, setAiSuggestion] = useState(null)
  const [aiLoading, setAiLoading] = useState(false)
  const [resolving, setResolving] = useState(false)

  // Edit/Delete state
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [deleting, setDeleting] = useState(false)

  // Assign state (admin only)
  const [assignEmail, setAssignEmail] = useState('')
  const [assigning, setAssigning] = useState(false)

  useEffect(() => {
    fetchIncident()
  }, [id])

  const fetchIncident = async () => {
    setLoading(true)
    try {
      const data = await incidentApi.getIncidentById(id)
      setIncident(data)
      if (data.aiRecommendation) {
          // Parse if it's a string from older incidents
          try {
              setAiSuggestion(typeof data.aiRecommendation === 'string' ? JSON.parse(data.aiRecommendation) : data.aiRecommendation)
          } catch (e) {
              // Fallback for plain text
              setAiSuggestion({ rootCause: data.aiRecommendation })
          }
      }
    } catch (err) {
      setError('Failed to load incident details')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const fetchAiSuggestion = async () => {
    if (!incident) return
    setAiLoading(true)
    try {
      const suggestion = await incidentApi.getAiSuggestion(incident)
      setAiSuggestion(suggestion)
    } catch (err) {
      console.error('Failed to get AI suggestion:', err)
    } finally {
      setAiLoading(false)
    }
  }

  const handleResolve = async () => {
    if (!incident) return
    setResolving(true)
    try {
      const aiRec = aiSuggestion ? JSON.stringify(aiSuggestion) : null
      await incidentApi.resolveIncident(incident.id, aiRec)
      await fetchIncident() // Refresh to show updated status + timeline
    } catch (err) {
      console.error('Failed to resolve incident:', err)
    } finally {
      setResolving(false)
    }
  }
  
  const handleDelete = async () => {
      if (!window.confirm('Are you sure you want to delete this incident? This action cannot be undone.')) {
          return
      }
      setDeleting(true)
      try {
          await incidentApi.deleteIncident(incident.id)
          navigate('/')
      } catch (err) {
          console.error('Failed to delete incident:', err)
          alert('Failed to delete incident')
          setDeleting(false)
      }
  }

  const handleIncidentUpdated = (updatedIncident) => {
      setIncident(updatedIncident)
  }

  const handleAssign = async () => {
    if (!assignEmail.trim()) return
    setAssigning(true)
    try {
      const updated = await incidentApi.assignIncident(incident.id, assignEmail.trim())
      setIncident(updated)
      setAssignEmail('')
    } catch (err) {
      console.error('Failed to assign incident:', err)
      alert('Failed to assign incident — ' + (err.response?.data?.error || err.message))
    } finally {
      setAssigning(false)
    }
  }

  // Ownership: ADMIN can edit/delete any; DEVELOPER only their own
  const canEditOrDelete = isAdmin() || incident?.reporterEmail === user?.email

  if (loading) {
    return (
      <div className="p-8 flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    )
  }

  if (error || !incident) {
    return (
      <div className="p-8">
        <div className="bg-red-50 border border-red-200 text-red-700 px-6 py-4 rounded-lg">
          {error || 'Incident not found'}
        </div>
        <Link to="/" className="mt-4 inline-flex items-center gap-2 text-blue-600">
          <ArrowLeft className="w-4 h-4" /> Back to Dashboard
        </Link>
      </div>
    )
  }

  return (
    <div className="p-8 max-w-5xl">
      {/* Back Link */}
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to Dashboard
      </Link>

      {/* Header */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
        <div className="flex items-start justify-between mb-4">
          <div>
            <p className="text-sm font-mono text-blue-600 mb-1">{incident.id}</p>
            <h1 className="text-2xl font-bold text-gray-900">{incident.title}</h1>
            <p className="text-gray-500 mt-1">{incident.serviceName || incident.source}</p>
          </div>
          <div className="flex items-center gap-3">
            <SeverityBadge severity={incident.severity || incident.classifiedSeverity} />
            <StatusBadge status={incident.status} />
            
            {/* Action Buttons — conditional on role/ownership */}
            <div className="flex items-center gap-2 ml-4 pl-4 border-l border-gray-200">
                {canEditOrDelete && (
                  <>
                    <button
                        onClick={() => setIsEditModalOpen(true)}
                        className="p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Edit Incident"
                    >
                        <Edit className="w-5 h-5" />
                    </button>
                    <button
                        onClick={handleDelete}
                        disabled={deleting}
                        className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                        title="Delete Incident"
                    >
                        {deleting ? <Loader2 className="w-5 h-5 animate-spin" /> : <Trash2 className="w-5 h-5" />}
                    </button>
                  </>
                )}
            </div>

            {/* Resolve — ADMIN only */}
            {isAdmin() && incident.status !== 'RESOLVED' && (
              <button
                onClick={handleResolve}
                disabled={resolving}
                className="ml-2 flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 text-sm font-medium"
              >
                {resolving ? (
                  <><Loader2 className="w-4 h-4 animate-spin" />Resolving...</>
                ) : (
                  <><CheckCircle2 className="w-4 h-4" />Resolve</>
                )}
              </button>
            )}
          </div>
        </div>

        <p className="text-gray-700 leading-relaxed">{incident.description}</p>

        {/* Metadata */}
        <div className="mt-6 pt-4 border-t border-gray-100 flex gap-6 text-sm text-gray-500">
          <span>Source: <strong>{incident.source}</strong></span>
          <span>Category: <strong>{incident.category}</strong></span>
          <span>Reporter: <strong>{incident.reporterEmail || 'N/A'}</strong></span>
          <span>Assigned: <strong>{incident.assignedTo || 'Unassigned'}</strong></span>
        </div>

        {/* Assign panel — ADMIN only */}
        {isAdmin() && (
          <div className="mt-4 pt-4 border-t border-gray-100">
            <p className="text-sm font-medium text-gray-700 mb-2">Assign to (email):</p>
            <div className="flex gap-2">
              <input
                type="email"
                value={assignEmail}
                onChange={e => setAssignEmail(e.target.value)}
                placeholder={incident.assignedTo || 'developer@example.com'}
                className="flex-1 px-3 py-1.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                onClick={handleAssign}
                disabled={assigning || !assignEmail.trim()}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {assigning ? <Loader2 className="w-4 h-4 animate-spin" /> : <UserCheck className="w-4 h-4" />}
                Assign
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Two Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Log Snippet */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Log Output</h2>
            <pre className="bg-gray-900 text-green-400 p-4 rounded-lg text-sm font-mono overflow-x-auto max-h-64 whitespace-pre-wrap">
              {incident.logs || 'No logs available'}
            </pre>
          </div>

          {/* AI Resolution Suggestion */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-purple-500" />
                <h2 className="text-lg font-semibold text-gray-900">Suggested Resolution</h2>
              </div>
              {!aiSuggestion && (
                <button
                  onClick={fetchAiSuggestion}
                  disabled={aiLoading}
                  className="flex items-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 text-sm"
                >
                  {aiLoading ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" />
                      Analyzing...
                    </>
                  ) : (
                    <>
                      <Sparkles className="w-4 h-4" />
                      Get AI Suggestion
                    </>
                  )}
                </button>
              )}
            </div>

            {aiSuggestion ? (
              <div className="space-y-4">
                {/* Confidence */}
                <div className="flex items-center gap-2 text-sm">
                  <span className="text-gray-500">Confidence:</span>
                  <div className="flex-1 bg-gray-200 rounded-full h-2 max-w-xs">
                    <div
                      className="bg-purple-500 h-2 rounded-full"
                      style={{ width: `${(aiSuggestion.confidence || 0) * 100}%` }}
                    />
                  </div>
                  <span className="font-medium">{Math.round((aiSuggestion.confidence || 0) * 100)}%</span>
                </div>

                {/* Root Cause */}
                <div>
                  <h3 className="font-medium text-gray-900 mb-2">Root Cause Analysis</h3>
                  <p className="text-gray-700 bg-gray-50 p-3 rounded-lg">
                    {aiSuggestion.rootCause}
                  </p>
                </div>

                {/* Resolution Steps */}
                <div>
                  <h3 className="font-medium text-gray-900 mb-2">Resolution Steps</h3>
                  <ol className="list-decimal list-inside space-y-2">
                    {(aiSuggestion.resolutionSteps || []).map((step, i) => (
                      <li key={i} className="text-gray-700 bg-gray-50 p-2 rounded">
                        {step}
                      </li>
                    ))}
                  </ol>
                </div>
              </div>
            ) : (
              <p className="text-gray-500 text-center py-8">
                Click "Get AI Suggestion" to analyze this incident
              </p>
            )}
          </div>
        </div>

        {/* Sidebar - Timeline */}
        <div className="space-y-6">
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Timeline</h2>
            <div className="space-y-4">
              <TimelineItem
                icon={AlertCircle}
                label="Created"
                time={incident.createdAt}
                color="text-blue-500"
                active={true}
              />
              <TimelineItem
                icon={Clock}
                label="Processed"
                time={incident.processedAt}
                color="text-purple-500"
                active={!!incident.processedAt}
              />
              <TimelineItem
                icon={Edit}
                label="Last Updated"
                time={incident.updatedAt}
                color="text-orange-500"
                active={incident.updatedAt && incident.updatedAt !== incident.createdAt}
              />
              <TimelineItem
                icon={CheckCircle}
                label="Resolved"
                time={incident.resolvedAt}
                color="text-green-500"
                active={!!incident.resolvedAt}
              />
            </div>
          </div>
        </div>
      </div>

      {/* Edit Modal */}
      <EditIncidentModal
        incident={incident}
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        onIncidentUpdated={handleIncidentUpdated}
      />
    </div>
  )
}

/**
 * Timeline Item Component
 */
function TimelineItem({ icon: Icon, label, time, color, active }) {
  return (
    <div className={`flex items-start gap-3 ${active ? '' : 'opacity-40'}`}>
      <div className={`mt-0.5 ${color}`}>
        <Icon className="w-5 h-5" />
      </div>
      <div>
        <p className="font-medium text-gray-900">{label}</p>
        <p className="text-sm text-gray-500">
          {time ? format(new Date(time), 'MMM d, yyyy HH:mm:ss') : 'Pending'}
        </p>
      </div>
    </div>
  )
}

export default IncidentDetail
