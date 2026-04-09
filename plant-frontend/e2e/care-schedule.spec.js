/**
 * 养护计划模块 E2E 测试
 * 测试养护计划的创建、查看和管理功能
 */

import { test, expect } from '@playwright/test';

test.describe('养护计划管理', () => {
  
  test.beforeEach(async ({ page }) => {
    // 每个测试前登录
    await page.goto('/login');
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'user123');
    await page.click('button:has-text("登录")');
    await page.waitForURL('**/dashboard');
  });

  test('应该能够访问养护计划列表页', async ({ page }) => {
    // 导航到养护计划列表
    await page.goto('/care/schedule-list');
    
    // 验证页面加载
    await expect(page.locator('.el-card')).toBeVisible();
    await expect(page).toHaveTitle(/.*养护计划.*/);
  });

  test('应该能够打开新建计划页面', async ({ page }) => {
    await page.goto('/care/schedule-list');
    
    // 点击新建按钮
    await page.click('button:has-text("新建计划")');
    
    // 验证跳转到新建页面
    await expect(page).toHaveURL(/.*\/schedule-add/);
    
    // 验证表单元素存在
    await expect(page.locator('input[placeholder="请输入植物名称搜索"]')).toBeVisible();
    await expect(page.locator('input[placeholder="例如：浇水、施肥（可自动生成）"]')).toBeVisible();
  });

  test('应该能够选择现有植物创建养护计划', async ({ page }) => {
    await page.goto('/care/schedule-add');
    
    // 确保选择"选择现有"模式
    await page.click('el-radio:has-text("选择现有") input');
    
    // 搜索植物
    const searchInput = page.locator('input[placeholder="请输入植物名称搜索 (官方/自定义)"]');
    await searchInput.fill('绿萝');
    await page.waitForTimeout(500); // 等待搜索结果
    
    // 选择第一个结果
    await page.click('.el-select-dropdown__item:first-child');
    
    // 填写任务信息
    await page.fill('input[placeholder="例如：浇水、施肥（可自动生成）"]', '浇水 - 绿萝');
    
    // 选择日期时间
    await page.click('input[placeholder="选择日期时间"]');
    await page.click('.el-date-picker__time-header');
    // 选择一个未来的日期
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateStr = tomorrow.toISOString().split('T')[0].replace(/-/g, '');
    await page.click(`td.available:has-text("${tomorrow.getDate()}")`);
    
    // 提交表单
    await page.click('button:has-text("创建计划")');
    
    // 验证成功提示
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('.el-message--success')).toContainText('养护计划创建成功');
    
    // 验证跳转到列表页
    await page.waitForURL('**/schedule-list');
  });

  test('应该能够创建新植物并添加养护计划', async ({ page }) => {
    await page.goto('/care/schedule-add');
    
    // 选择"创建新植物"模式
    await page.click('el-radio:has-text("创建新植物") input');
    
    // 填写植物信息
    const timestamp = Date.now();
    await page.fill('input[placeholder="例如：我的发财树"]', `测试植物_${timestamp}`);
    await page.fill('input[placeholder="科"]', '天南星科');
    await page.fill('input[placeholder="属"]', '麒麟叶属');
    await page.fill('textarea[placeholder="备注信息"]', 'E2E 测试创建的植物');
    
    // 选择任务类型
    await page.click('el-radio-button:has-text("浇水")');
    
    // 验证任务名称自动生成
    await expect(page.locator('input[placeholder="例如：浇水、施肥（可自动生成）"]')).toHaveValue(/浇水/);
    
    // 选择日期时间
    await page.click('input[placeholder="选择日期时间"]');
    await page.click('.el-date-picker__time-header');
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    await page.click(`td.available:has-text("${tomorrow.getDate()}")`);
    
    // 提交表单
    await page.click('button:has-text("创建计划")');
    
    // 验证成功提示（可能有两个：植物创建成功 + 养护计划创建成功）
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 15000 });
    
    // 验证跳转到列表页
    await page.waitForURL('**/schedule-list');
  });

  test('应该验证必填字段', async ({ page }) => {
    await page.goto('/care/schedule-add');
    
    // 直接提交空表单
    await page.click('button:has-text("创建计划")');
    
    // 验证表单验证错误
    await expect(page.locator('.el-form-item__error')).toBeVisible();
  });

  test('应该能够设置重复规则', async ({ page }) => {
    await page.goto('/care/schedule-add');
    
    // 选择"创建新植物"模式
    await page.click('el-radio:has-text("创建新植物") input');
    await page.fill('input[placeholder="例如：我的发财树"]', '重复测试植物');
    
    // 选择任务类型
    await page.click('el-radio-button:has-text("浇水")');
    
    // 选择日期
    await page.click('input[placeholder="选择日期时间"]');
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    await page.click(`td.available:has-text("${tomorrow.getDate()}")`);
    
    // 设置重复规则：每 7 天
    await page.click('.el-select');
    await page.click('.el-select-dropdown__item:has-text("按天重复")');
    await page.fill('input[type="number"]', '7');
    
    // 提交
    await page.click('button:has-text("创建计划")');
    
    // 验证成功
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 15000 });
  });

  test('应该能够编辑现有养护计划', async ({ page }) => {
    await page.goto('/care/schedule-list');
    
    // 等待列表加载
    await page.waitForSelector('.el-table__row', { timeout: 10000 });
    
    // 点击第一行的编辑按钮
    await page.click('.el-table__row:first-child .el-button:has-text("编辑")');
    
    // 验证进入编辑模式
    await expect(page).toHaveURL(/.*\/schedule-add.*id=/);
    await expect(page.locator('.card-header span')).toContainText('编辑养护计划');
    
    // 修改任务名称
    const taskNameInput = page.locator('input[placeholder="例如：浇水、施肥（可自动生成）"]');
    await taskNameInput.fill('修改后的任务名称');
    
    // 提交
    await page.click('button:has-text("更新计划")');
    
    // 验证成功
    await expect(page.locator('.el-message--success')).toBeVisible();
    await expect(page.locator('.el-message--success')).toContainText('更新成功');
  });

  test('应该能够删除养护计划', async ({ page }) => {
    await page.goto('/care/schedule-list');
    
    // 等待列表加载
    await page.waitForSelector('.el-table__row', { timeout: 10000 });
    
    // 记录删除前的行数
    const initialRowCount = await page.locator('.el-table__row').count();
    
    // 点击第一行的删除按钮
    await page.click('.el-table__row:first-child .el-button:has-text("删除")');
    
    // 确认删除
    await page.click('.el-message-box__btns .el-button--primary');
    
    // 验证成功提示
    await expect(page.locator('.el-message--success')).toBeVisible();
    
    // 验证行数减少
    const finalRowCount = await page.locator('.el-table__row').count();
    expect(finalRowCount).toBeLessThan(initialRowCount);
  });
});
