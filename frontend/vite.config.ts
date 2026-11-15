import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  define: { global: 'globalThis' },
  plugins: [vue()],
  resolve: {
    alias: { '@': resolve(__dirname, 'src') }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'https://localhost', secure: false },
      '/ws': { target: 'https://localhost', ws: true, secure: false }
    }
  }
})
