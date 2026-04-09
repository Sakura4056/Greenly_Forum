/**
 * 用户认证模块 E2E 测试
 * 测试登录和注册功能
 */

import { test, expect } from '@playwright/test';

test.describe('用户认证', () => {
  
  test.beforeEach(async ({ page }) => {
    // 每个测试前访问登录页
    await page.goto('/login');
  });

  test('应该成功登录管理员账号', async ({ page }) => {
    // 填写登录表单
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'user123');
    
    // 点击登录按钮
    await page.click('button:has-text("登录")');
    
    // 等待跳转并验证
    await page.waitForURL('**/dashboard');
    await expect(page).toHaveURL(/.*dashboard/);
    
    // 验证欢迎信息
    await expect(page.locator('.welcome-message')).toBeVisible();
  });

  test('应该显示登录错误提示', async ({ page }) => {
    // 输入错误密码
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'wrongpassword');
    
    // 点击登录
    await page.click('button:has-text("登录")');
    
    // 验证错误提示
    await expect(page.locator('.el-message--error')).toBeVisible();
    await expect(page.locator('.el-message--error')).toContainText('用户名或密码错误');
  });

  test('应该验证必填字段', async ({ page }) => {
    // 直接点击登录（不填任何内容）
    await page.click('button:has-text("登录")');
    
    // 验证表单验证提示
    await expect(page.locator('.el-form-item__error')).toHaveCount(2);
  });

  test('应该能够跳转到注册页面', async ({ page }) => {
    // 点击注册链接
    await page.click('a:has-text("注册")');
    
    // 验证跳转到注册页
    await expect(page).toHaveURL(/.*\/register/);
  });

  test('应该成功注册新用户', async ({ page }) => {
    // 跳转到注册页
    await page.goto('/register');
    
    const timestamp = Date.now();
    const username = `testuser_${timestamp}`;
    
    // 填写注册表单
    await page.fill('input[placeholder="请输入用户名"]', username);
    await page.fill('input[placeholder="请输入密码"]', 'Test@123456');
    await page.fill('input[placeholder="请确认密码"]', 'Test@123456');
    await page.fill('input[placeholder="请输入邮箱"]', `${username}@test.com`);
    await page.fill('input[placeholder="请输入手机号"]', `138${String(timestamp).slice(-8)}`);
    
    // 点击注册
    await page.click('button:has-text("注册")');
    
    // 等待成功提示
    await expect(page.locator('.el-message--success')).toBeVisible();
    
    // 验证跳转到登录页
    await page.waitForURL('**/login');
  });

  test('应该验证密码一致性', async ({ page }) => {
    await page.goto('/register');
    
    // 输入不一致的密码
    await page.fill('input[placeholder="请输入密码"]', 'Password123');
    await page.fill('input[placeholder="请确认密码"]', 'Password456');
    
    // 点击注册
    await page.click('button:has-text("注册")');
    
    // 验证错误提示
    await expect(page.locator('.el-form-item__error')).toContainText('两次输入的密码不一致');
  });
});
