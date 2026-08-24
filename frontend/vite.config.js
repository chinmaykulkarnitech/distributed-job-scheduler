import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.VITE_API_BASE_URL || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        // Every request the app makes to /api/* is forwarded to the Spring
        // Boot backend. This keeps the browser calls same-origin during
        // development so no CORS configuration is required on the backend.
        '/api': {
          target,
          changeOrigin: true,
        },
      },
    },
  }
})
