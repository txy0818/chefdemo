import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

const getElementPlusChunkName = (id) => {
  const match = id.match(/element-plus\/(?:es|lib)\/components\/([^/]+)/)
  const name = match?.[1]
  if (!name) {
    return 'vendor-element-plus-core'
  }
  const first = name[0]
  if (first >= 'a' && first <= 'h') {
    return 'vendor-element-plus-a-h'
  }
  if (first >= 'i' && first <= 'p') {
    return 'vendor-element-plus-i-p'
  }
  return 'vendor-element-plus-q-z'
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    chunkSizeWarningLimit: 800,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return
          }
          if (id.includes('@element-plus/icons-vue')) {
            return 'vendor-ep-icons'
          }
          if (id.includes('dayjs') || id.includes('async-validator') || id.includes('@floating-ui')) {
            return 'vendor-ep-utils'
          }
          if (id.includes('zrender')) {
            return 'vendor-zrender'
          }
          if (id.includes('echarts')) {
            return 'vendor-echarts'
          }
          if (id.includes('element-plus')) {
            return getElementPlusChunkName(id)
          }
          if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router')) {
            return 'vendor-vue'
          }
          return 'vendor'
        }
      }
    }
  }
})
