import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './tests/e2e',
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: 'http://127.0.0.1:5173',
    trace: 'retain-on-failure'
  },
  webServer: [
    {
      command: 'cmd /c "..\\backend\\mvnw.cmd spring-boot:run"',
      url: 'http://127.0.0.1:8088/api/dashboard/stats',
      reuseExistingServer: true,
      timeout: 120000
    },
    {
      command: 'npm run dev -- --host 127.0.0.1',
      url: 'http://127.0.0.1:5173',
      reuseExistingServer: true,
      timeout: 120000
    }
  ]
})
