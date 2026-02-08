/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Enterprise color palette
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        },
        severity: {
          critical: '#dc2626',
          high: '#ea580c',
          medium: '#eab308',
          low: '#22c55e',
        },
        status: {
          new: '#3b82f6',
          processing: '#8b5cf6',
          assigned: '#f59e0b',
          resolved: '#22c55e',
          closed: '#6b7280',
        },
      },
    },
  },
  plugins: [],
}
