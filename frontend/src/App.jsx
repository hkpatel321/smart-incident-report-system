import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import IncidentDetail from './pages/IncidentDetail'
import CreateIncident from './pages/CreateIncident'

/**
 * Main App Component
 * Routes:
 * - /           : Incident Dashboard (main screen)
 * - /incident/:id : Incident Detail View
 * - /create     : Create/Simulate Incident
 */
function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="incident/:id" element={<IncidentDetail />} />
        <Route path="create" element={<CreateIncident />} />
      </Route>
    </Routes>
  )
}

export default App
