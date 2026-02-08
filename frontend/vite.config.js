import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

/**
 * Vite Configuration
 * 
 * API Proxy Setup:
 * - /api/incidents -> Ingest Service (8081)
 * - /api/dashboard -> Processor Service (8082)
 * - /api/ai -> RAG Service (8084)
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // Ingest Service - POST incidents
      '/api/incidents': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // Processor Service - Dashboard & Stats
      '/api/dashboard': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/stats': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      // RAG Service - AI resolution
      '/api/ai': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      '/api/documents': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
  },
})
