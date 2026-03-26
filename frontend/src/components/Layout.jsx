import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { AlertTriangle, LayoutDashboard, Plus, LogOut, User } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

/**
 * Main Layout Component
 * Provides navigation sidebar, user info, and logout.
 */
function Layout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, logout } = useAuth()

  const navItems = [
    { path: '/', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/create', label: 'Create Incident', icon: Plus },
  ]

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-900 text-white flex flex-col">
        {/* Logo */}
        <div className="p-6 border-b border-gray-700">
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-8 h-8 text-orange-500" />
            <div>
              <h1 className="font-bold text-lg leading-tight">Smart Incident</h1>
              <p className="text-xs text-gray-400">Response System</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4">
          <ul className="space-y-2">
            {navItems.map(item => {
              const Icon = item.icon
              const isActive = location.pathname === item.path
              return (
                <li key={item.path}>
                  <Link
                    to={item.path}
                    className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                      isActive
                        ? 'bg-blue-600 text-white'
                        : 'text-gray-300 hover:bg-gray-800'
                    }`}
                  >
                    <Icon className="w-5 h-5" />
                    {item.label}
                  </Link>
                </li>
              )
            })}
          </ul>
        </nav>

        {/* User Info + Logout */}
        <div className="p-4 border-t border-gray-700">
          {user && (
            <div className="mb-3">
              <div className="flex items-center gap-2 mb-1">
                <User className="w-4 h-4 text-gray-400" />
                <span className="text-sm font-medium text-white truncate">{user.name}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-xs text-gray-400 truncate">{user.email}</span>
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                  user.role === 'ADMIN'
                    ? 'bg-orange-500/20 text-orange-300'
                    : 'bg-blue-500/20 text-blue-300'
                }`}>
                  {user.role}
                </span>
              </div>
            </div>
          )}
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-3 py-2 text-sm text-gray-400 hover:text-white hover:bg-gray-800 rounded-lg transition-colors"
          >
            <LogOut className="w-4 h-4" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}

export default Layout
