import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = process.env.VITE_PROXY_TARGET || env.VITE_PROXY_TARGET || 'http://localhost:8081'
  const devPort = parseInt(process.env.VITE_PORT || env.VITE_PORT || '3000', 10)
  const devHost = process.env.VITE_HOST || env.VITE_HOST || 'localhost'

  return {
    plugins: [vue(), vueDevTools()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: devPort,
      host: devHost,
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          timeout: 300000,
          proxyTimeout: 300000,
          ws: true,
          configure: (proxy, _options) => {
            proxy.on('error', () => {})
            proxy.on('proxyReq', (proxyReq) => {
              proxyReq.setHeader('Connection', 'keep-alive')
            })
            proxy.on('proxyRes', (proxyRes, req, _res) => {
              const contentType = proxyRes.headers['content-type'] || ''
              const isStreaming = contentType.includes('application/x-ndjson') || 
                                 contentType.includes('text/event-stream') ||
                                 contentType.includes('application/ndjson')
              if (isStreaming) {
                delete proxyRes.headers['content-length']
                proxyRes.headers['cache-control'] = 'no-cache'
                proxyRes.headers['connection'] = 'keep-alive'
                proxyRes.headers['x-accel-buffering'] = 'no'
                if (_res && typeof _res.setHeader === 'function') {
                  _res.setHeader('Cache-Control', 'no-cache')
                  _res.setHeader('Connection', 'keep-alive')
                  _res.setHeader('X-Accel-Buffering', 'no')
                }
              }
            })
          }
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: false,
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: true,
          drop_debugger: true
        }
      },
      rollupOptions: {
        output: {
          chunkFileNames: 'assets/js/[name]-[hash].js',
          entryFileNames: 'assets/js/[name]-[hash].js',
          assetFileNames: 'assets/[ext]/[name]-[hash].[ext]'
        }
      }
    }
  }
})

