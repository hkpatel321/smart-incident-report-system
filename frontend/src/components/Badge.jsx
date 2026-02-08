/**
 * Severity Badge Component
 * Displays color-coded severity level
 */
export function SeverityBadge({ severity }) {
  const colors = {
    CRITICAL: 'bg-red-100 text-red-800 border-red-200',
    HIGH: 'bg-orange-100 text-orange-800 border-orange-200',
    MEDIUM: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    LOW: 'bg-green-100 text-green-800 border-green-200',
  }

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${
        colors[severity] || 'bg-gray-100 text-gray-800 border-gray-200'
      }`}
    >
      {severity}
    </span>
  )
}

/**
 * Status Badge Component
 * Displays color-coded status
 */
export function StatusBadge({ status }) {
  const colors = {
    NEW: 'bg-blue-100 text-blue-800 border-blue-200',
    PROCESSING: 'bg-purple-100 text-purple-800 border-purple-200',
    ASSIGNED: 'bg-amber-100 text-amber-800 border-amber-200',
    RESOLVED: 'bg-green-100 text-green-800 border-green-200',
    CLOSED: 'bg-gray-100 text-gray-600 border-gray-200',
  }

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${
        colors[status] || 'bg-gray-100 text-gray-800'
      }`}
    >
      {status}
    </span>
  )
}
