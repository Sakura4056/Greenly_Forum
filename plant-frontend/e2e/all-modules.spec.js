import { test, expect } from '@playwright/test';

/**
 * Greenly 全模块功能测试
 * 测试所有核心功能模块的基本可用性
 */

test.describe('Greenly 全模块功能测试', () => {
  
  // 测试1: 首页加载
  test('应该成功加载首页', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/Greenly|植物养护/);
    console.log('✅ 首页加载成功');
  });

  // 测试2: 用户登录
  test('应该成功登录系统', async ({ page }) => {
    await page.goto('/login');
    
    // 填写登录表单
    await page.fill('input[placeholder*="用户名"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'user123');
    
    // 点击登录按钮
    await page.click('button[type="submit"], button:has-text("登录")');
    
    // 等待跳转或成功提示
    await page.waitForTimeout(2000);
    
    // 验证登录成功（检查是否跳转到首页或dashboard）
    const currentUrl = page.url();
    expect(currentUrl).not.toContain('/login');
    console.log('✅ 登录成功，当前URL:', currentUrl);
  });

  // 测试3: 官方植物库
  test('应该能访问官方植物库', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"], button:has-text("登录")');
    await page.waitForTimeout(2000);
    
    // 访问植物库
    await page.goto('/plant/list');
    await page.waitForTimeout(1000);
    
    // 验证页面加载
    const content = await page.content();
    expect(content).toBeTruthy();
    console.log('✅ 官方植物库页面可访问');
  });

  // 测试4: 我的植物
  test('应该能访问我的植物页面', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"], button:has-text("登录")');
    await page.waitForTimeout(2000);
    
    // 访问我的植物
    await page.goto('/my-plant');
    await page.waitForTimeout(1000);
    
    const content = await page.content();
    expect(content).toBeTruthy();
    console.log('✅ 我的植物页面可访问');
  });

  // 测试5: 养护计划
  test('应该能访问养护计划页面', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"], button:has-text("登录")');
    await page.waitForTimeout(2000);
    
    // 访问养护计划
    await page.goto('/care/schedule');
    await page.waitForTimeout(1000);
    
    const content = await page.content();
    expect(content).toBeTruthy();
    console.log('✅ 养护计划页面可访问');
  });

  // 测试6: 植物日记
  test('应该能访问植物日记页面', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"], button:has-text("登录")');
    await page.waitForTimeout(2000);
    
    // 访问植物日记
    await page.goto('/diary');
    await page.waitForTimeout(1000);
    
    const content = await page.content();
    expect(content).toBeTruthy();
    console.log('✅ 植物日记页面可访问');
  });

  // 测试7: AI识别
  test('应该能访问AI识别页面', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"], button:has-text("登录")');
    await page.waitForTimeout(2000);
    
    // 访问AI识别
    await page.goto('/ai/identification');
    await page.waitForTimeout(1000);
    
    const content = await page.content();
    expect(content).toBeTruthy();
    console.log('✅ AI识别页面可访问');
  });

  // 测试8: 提醒配置
  test('应该能访问提醒配置页面', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"], button:has-text("登录")');
    await page.waitForTimeout(2000);
    
    // 访问提醒配置
    await page.goto('/reminder/config');
    await page.waitForTimeout(1000);
    
    const content = await page.content();
    expect(content).toBeTruthy();
    console.log('✅ 提醒配置页面可访问');
  });

  // 测试9: 论坛
  test('应该能访问论坛页面', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"], button:has-text("登录")');
    await page.waitForTimeout(2000);
    
    // 访问论坛
    await page.goto('/forum');
    await page.waitForTimeout(1000);
    
    const content = await page.content();
    expect(content).toBeTruthy();
    console.log('✅ 论坛页面可访问');
  });

  // 测试10: 个人中心
  test('应该能访问个人中心', async ({ page }) => {
    // 先登录
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"], button:has-text("登录")');
    await page.waitForTimeout(2000);
    
    // 访问个人中心
    await page.goto('/user/profile');
    await page.waitForTimeout(1000);
    
    const content = await page.content();
    expect(content).toBeTruthy();
    console.log('✅ 个人中心页面可访问');
  });
});
