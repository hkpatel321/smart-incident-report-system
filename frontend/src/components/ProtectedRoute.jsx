import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Loader2 } from 'lucide-react'

/**
 * Wraps routes that require authentication.
 * Redirects to /login if user is not logged in.
 */
export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return children
}
