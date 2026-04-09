# Greenly E2E 测试文档

## 概述

本项目使用 Playwright 进行端到端（E2E）测试，确保核心业务流程的正确性。

## 安装

```bash
cd plant-frontend
npm install
npx playwright install
```

## 运行测试

### 运行所有测试
```bash
npm run test:e2e
```

### 运行特定测试文件
```bash
npx playwright test e2e/auth.spec.js
```

### 以 UI 模式运行（推荐用于调试）
```bash
npm run test:e2e:ui
```

### 调试模式
```bash
npm run test:e2e:debug
```

## 测试覆盖范围

### 1. 用户认证 (auth.spec.js)
- ✅ 管理员登录
- ✅ 登录错误处理
- ✅ 表单验证
- ✅ 用户注册
- ✅ 密码一致性验证

### 2. 养护计划 (care-schedule.spec.js)
- ✅ 访问计划列表
- ✅ 创建新计划（选择现有植物）
- ✅ 创建新计划（新建植物）
- ✅ 表单验证
- ✅ 设置重复规则
- ✅ 编辑计划
- ✅ 删除计划

### 3. 植物管理 (plant.spec.js) - TODO
- [ ] 查看官方植物库
- [ ] 添加我的植物
- [ ] 编辑植物信息
- [ ] 删除植物

### 4. 养护记录 (care-record.spec.js) - TODO
- [ ] 添加养护记录
- [ ] 查看统计报表
- [ ] 导出统计数据

## 测试结构

```
e2e/
├── auth.spec.js           # 认证测试
├── care-schedule.spec.js  # 养护计划测试
├── fixtures/              # 测试夹具
│   └── test-helpers.js    # 通用辅助函数
├── utils/                 # 工具函数
│   └── test-data.js       # 测试数据生成
└── README.md              # 本文档
```

## 编写新测试

### 基本模板

```javascript
import { test, expect } from '@playwright/test';

test.describe('功能模块名称', () => {
  
  test.beforeEach(async ({ page }) => {
    // 前置操作（如登录）
    await page.goto('/login');
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'user123');
    await page.click('button:has-text("登录")');
    await page.waitForURL('**/dashboard');
  });

  test('应该执行某个操作', async ({ page }) => {
    // 导航到目标页面
    await page.goto('/target-page');
    
    // 执行操作
    await page.click('button:has-text("操作按钮")');
    
    // 验证结果
    await expect(page.locator('.success-message')).toBeVisible();
  });
});
```

### 最佳实践

1. **使用语义化选择器**
   ```javascript
   // ✅ 好
   await page.click('button:has-text("提交")');
   
   // ❌ 避免
   await page.click('.el-button:nth-child(3)');
   ```

2. **添加适当的等待**
   ```javascript
   // ✅ 自动等待
   await expect(page.locator('.element')).toBeVisible();
   
   // ❌ 避免硬编码等待
   await page.waitForTimeout(5000);
   ```

3. **使用有意义的测试名称**
   ```javascript
   // ✅ 清晰描述测试内容
   test('应该成功登录管理员账号', async ({ page }) => { ... });
   
   // ❌ 模糊不清
   test('test login', async ({ page }) => { ... });
   ```

4. **隔离测试数据**
   ```javascript
   // 使用时间戳避免数据冲突
   const timestamp = Date.now();
   const username = `testuser_${timestamp}`;
   ```

## CI/CD 集成

### GitHub Actions 示例

```yaml
name: E2E Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          
      - name: Install dependencies
        run: |
          cd plant-frontend
          npm ci
          npx playwright install --with-deps
          
      - name: Run E2E tests
        run: |
          cd plant-frontend
          npm run test:e2e
          
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: plant-frontend/playwright-report/
```

## 故障排查

### 问题 1: 测试超时

**解决方案**:
- 增加超时时间
  ```javascript
  test.setTimeout(60000);
  ```
- 检查开发服务器是否正常运行
- 确认数据库连接正常

### 问题 2: 元素找不到

**解决方案**:
- 使用 Playwright Inspector 调试
  ```bash
  npx playwright test --debug
  ```
- 检查选择器是否正确
- 确认页面已完全加载

### 问题 3: 测试不稳定（flaky tests）

**解决方案**:
- 避免硬编码等待时间
- 使用可靠的等待条件
  ```javascript
  await expect(locator).toBeVisible();
  ```
- 启用重试机制
  ```javascript
  // playwright.config.js
  retries: 2
  ```

## 报告查看

测试完成后，HTML 报告会保存在 `playwright-report/` 目录：

```bash
npx playwright show-report
```

## 资源链接

- [Playwright 官方文档](https://playwright.dev/)
- [Playwright for Vue](https://playwright.dev/docs/test-components#vue)
- [Element Plus 测试建议](https://element-plus.org/en-US/guide/testing.html)

---

**最后更新**: 2026-04-04
**维护者**: Greenly 开发团队
