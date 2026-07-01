import { fileURLToPath } from 'node:url'
import { defineConfig, configDefaults } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: { port: 5173 },
  build: {
    rollupOptions: {
      input: {
        main: fileURLToPath(new URL('./index.html', import.meta.url)),
        // The hidden-iframe OIDC silent-renew callback page (no React, tiny bundle).
        'silent-renew': fileURLToPath(new URL('./silent-renew.html', import.meta.url)),
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
    css: false,
    // e2e/ is driven by Playwright, not Vitest.
    exclude: [...configDefaults.exclude, 'e2e/**'],
  },
})
