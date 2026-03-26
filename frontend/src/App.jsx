import { Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import IncidentDetail from './pages/IncidentDetail'
import CreateIncident from './pages/CreateIncident'
import Login from './pages/Login'
import Register from './pages/Register'

/**
 * Main App Component
 * Public routes: /login, /register
 * Protected routes: /, /incident/:id, /create
 */
function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Protected */}
        <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          <Route index element={<Dashboard />} />
          <Route path="incident/:id" element={<IncidentDetail />} />
          <Route path="create" element={<CreateIncident />} />
        </Route>
      </Routes>
    </AuthProvider>
  )
}

export default App
