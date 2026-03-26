import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ShieldAlert, Loader2, Eye, EyeOff } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const navigate = useNavigate()
  const { register } = useAuth()
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'DEVELOPER' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form.name, form.email, form.password, form.role)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-950 to-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-2xl mb-4 shadow-lg">
            <ShieldAlert className="w-9 h-9 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-white">Smart Incident</h1>
          <p className="text-blue-300 mt-1">Response System</p>
        </div>

        {/* Card */}
        <div className="bg-white/10 backdrop-blur-md rounded-2xl border border-white/20 p-8 shadow-2xl">
          <h2 className="text-xl font-semibold text-white mb-6">Create an account</h2>

          {error && (
            <div className="mb-4 p-3 bg-red-500/20 border border-red-500/40 rounded-lg text-red-200 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-blue-200 mb-1">Full Name</label>
              <input
                type="text"
                required
                value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })}
                placeholder="John Doe"
                className="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/40 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-blue-200 mb-1">Email</label>
              <input
                type="email"
                required
                value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })}
                placeholder="you@example.com"
                className="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/40 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-blue-200 mb-1">Password</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  minLength={6}
                  value={form.password}
                  onChange={e => setForm({ ...form, password: e.target.value })}
                  placeholder="Min 6 characters"
                  className="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/40 focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-3 text-white/40 hover:text-white/70"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-blue-200 mb-1">Role</label>
              <select
                value={form.role}
                onChange={e => setForm({ ...form, role: e.target.value })}
                className="w-full px-4 py-2.5 bg-white/10 border border-white/20 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="DEVELOPER" className="bg-slate-800">Developer — can report &amp; manage own incidents</option>
                <option value="ADMIN" className="bg-slate-800">Admin — full control, can assign &amp; resolve</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 bg-blue-600 hover:bg-blue-500 text-white font-medium rounded-lg transition-colors disabled:opacity-50 flex items-center justify-center gap-2 mt-2"
            >
              {loading ? <><Loader2 className="w-4 h-4 animate-spin" /> Creating account...</> : 'Create Account'}
            </button>
          </form>

          <p className="text-center text-white/50 text-sm mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-300 hover:text-blue-200 font-medium">
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
