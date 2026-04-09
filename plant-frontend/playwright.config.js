import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright E2E 测试配置
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: './e2e',
  
  /* 完全并行运行测试 */
  fullyParallel: true,
  
  /* 禁止 test.only 提交到 CI */
  forbidOnly: !!process.env.CI,
  
  /* 重试次数 */
  retries: process.env.CI ? 2 : 0,
  
  /* 并行工作进程数 */
  workers: process.env.CI ? 1 : undefined,
  
  /* Reporter 配置 */
  reporter: [
    ['html', { open: 'never' }],
    ['list'],
    ['json', { outputFile: 'test-results/results.json' }]
  ],
  
  /* 共享配置 */
  use: {
    /* 基础 URL（如果测试中使用相对路径） */
    baseURL: process.env.BASE_URL || 'http://localhost:5173',
    
    /* 每次测试后收集 trace */
    trace: 'on-first-retry',
    
    /* 截图 */
    screenshot: 'only-on-failure',
    
    /* 视频录制 */
    video: 'retain-on-failure',
    
    /* 超时设置 */
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },
  
  /* 针对不同浏览器的配置 */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    /* 移动端模拟测试（可选） */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },
  ],
  
  /* 启动开发服务器的配置 */
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
});
