import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    coverage: {
      include: ['src/pages/Dashboard.jsx'],
      reporter: ['text', 'html'] // Genera la tabla y el reporte web visual
    }
  }
})