/**
 * Greenly 全模块 API 测试脚本
 * 测试所有核心功能模块的后端API
 */

const axios = require('axios');

const BASE_URL = 'http://localhost:9090';
let authToken = '';

// 测试结果统计
const results = {
  passed: 0,
  failed: 0,
  total: 0,
  details: []
};

/**
 * 记录测试结果
 */
function recordTest(module, testName, success, message = '') {
  results.total++;
  if (success) {
    results.passed++;
    console.log(`✅ [${module}] ${testName}`);
  } else {
    results.failed++;
    console.log(`❌ [${module}] ${testName} - ${message}`);
  }
  results.details.push({ module, testName, success, message });
}

/**
 * 登录获取Token
 */
async function login() {
  try {
    console.log('\n📝 正在登录...');
    
    // 先尝试注册一个新用户
    const testUsername = `test_${Date.now()}`;
    const testPassword = 'Test@123456';
    
    try {
      console.log('尝试注册新用户...');
      const registerResponse = await axios.post(`${BASE_URL}/api/user/register`, {
        username: testUsername,
        password: testPassword,
        nickname: '测试用户',
        email: `${testUsername}@test.com`
      });
      
      if (registerResponse.data && registerResponse.data.code === 200) {
        console.log('✅ 注册成功，使用新用户登录');
        authToken = registerResponse.data.data.token;
        return true;
      }
    } catch (e) {
      console.log('注册失败，尝试使用现有账号登录');
    }
    
    // 尝试多个可能的密码
    const passwords = ['user123', 'admin123', 'password', '123456'];
    let success = false;
    
    for (const password of passwords) {
      try {
        const response = await axios.post(`${BASE_URL}/api/user/login`, {
          username: 'admin',
          password: password
        });
        
        if (response.data && response.data.code === 200) {
          authToken = response.data.data.token;
          console.log(`✅ 登录成功（密码: ${password}），Token已获取\n`);
          success = true;
          break;
        }
      } catch (e) {
        // 继续尝试下一个密码
      }
    }
    
    if (!success) {
      console.log('❌ 所有密码尝试失败');
      console.log('\n提示：请检查数据库中用户的实际密码，或手动注册一个新用户');
      return false;
    }
    
    return true;
  } catch (error) {
    console.log('❌ 登录请求失败:', error.message);
    if (error.response) {
      console.log('错误状态码:', error.response.status);
      console.log('错误数据:', JSON.stringify(error.response.data, null, 2));
    }
    return false;
  }
}

/**
 * 测试用户认证模块
 */
async function testAuth() {
  console.log('\n========== 测试1: 用户认证模块 ==========');
  
  // 测试注册
  try {
    const response = await axios.post(`${BASE_URL}/api/user/register`, {
      username: `testuser_${Date.now()}`,
      password: 'test123456',
      nickname: '测试用户',
      email: `test${Date.now()}@example.com`
    });
    recordTest('认证模块', '用户注册', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('认证模块', '用户注册', false, error.response?.data?.message || error.message);
  }
  
  // 测试获取用户信息
  try {
    const response = await axios.get(`${BASE_URL}/api/user/info`, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    recordTest('认证模块', '获取用户信息', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('认证模块', '获取用户信息', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试官方植物库模块
 */
async function testOfficialPlant() {
  console.log('\n========== 测试2: 官方植物库模块 ==========');
  
  // 测试植物列表
  try {
    const response = await axios.get(`${BASE_URL}/api/plant/official/query`, {
      params: { pageNum: 1, pageSize: 10 }
    });
    recordTest('官方植物库', '植物列表查询', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('官方植物库', '植物列表查询', false, error.response?.data?.message || error.message);
  }
  
  // 测试植物详情
  try {
    const response = await axios.get(`${BASE_URL}/api/plant/official/1`);
    recordTest('官方植物库', '植物详情查询', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('官方植物库', '植物详情查询', false, error.response?.data?.message || error.message);
  }
  
  // 测试植物搜索
  try {
    const response = await axios.get(`${BASE_URL}/api/plant/official/query`, {
      params: { keyword: '绿萝' }
    });
    recordTest('官方植物库', '植物搜索', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('官方植物库', '植物搜索', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试我的植物模块
 */
async function testMyPlant() {
  console.log('\n========== 测试3: 我的植物模块 ==========');
  
  // 测试我的植物列表
  try {
    const response = await axios.get(`${BASE_URL}/api/my-plant/list`, {
      headers: { Authorization: `Bearer ${authToken}` },
      params: { current: 1, size: 10 }
    });
    recordTest('我的植物', '我的植物列表', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('我的植物', '我的植物列表', false, error.response?.data?.message || error.message);
  }
  
  // 测试添加植物
  try {
    const response = await axios.post(`${BASE_URL}/api/my-plant`, {
      officialId: 1,
      nickname: '测试植物',
      location: '阳台',
      acquisitionDate: '2024-01-01'
    }, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    recordTest('我的植物', '添加植物', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('我的植物', '添加植物', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试养护计划模块
 */
async function testCareSchedule() {
  console.log('\n========== 测试4: 养护计划模块 ==========');
  
  // 测试养护计划列表
  try {
    const response = await axios.get(`${BASE_URL}/api/care/schedule/list`, {
      headers: { Authorization: `Bearer ${authToken}` },
      params: { pageNum: 1, pageSize: 10 }
    });
    recordTest('养护计划', '养护计划列表', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('养护计划', '养护计划列表', false, error.response?.data?.message || error.message);
  }
  
  // 测试创建养护计划
  try {
    const response = await axios.post(`${BASE_URL}/api/care/schedule`, {
      plantId: 1,
      plantSource: 'OFFICIAL',
      taskName: '浇水',
      dueTime: '2024-12-31T10:00:00',
      recurrenceType: 'WEEK',
      recurrenceInterval: 7
    }, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    recordTest('养护计划', '创建养护计划', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('养护计划', '创建养护计划', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试养护记录模块
 */
async function testCareRecord() {
  console.log('\n========== 测试5: 养护记录模块 ==========');
  
  // 测试养护记录列表
  try {
    const response = await axios.get(`${BASE_URL}/api/care/record/list`, {
      headers: { Authorization: `Bearer ${authToken}` },
      params: { pageNum: 1, pageSize: 10 }
    });
    recordTest('养护记录', '养护记录列表', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('养护记录', '养护记录列表', false, error.response?.data?.message || error.message);
  }
  
  // 测试添加养护记录
  try {
    const response = await axios.post(`${BASE_URL}/api/care/record`, {
      plantId: 1,
      plantSource: 'OFFICIAL',
      recordTime: '2024-01-01T10:00:00',
      operations: JSON.stringify({ water: true, fertilize: false }),
      remarks: '测试记录'
    }, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    recordTest('养护记录', '添加养护记录', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('养护记录', '添加养护记录', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试植物照片模块
 */
async function testPhoto() {
  console.log('\n========== 测试6: 植物照片模块 ==========');
  
  // 测试照片列表
  try {
    const response = await axios.get(`${BASE_URL}/api/photo/list`, {
      headers: { Authorization: `Bearer ${authToken}` },
      params: { pageNum: 1, pageSize: 10 }
    });
    recordTest('植物照片', '照片列表查询', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('植物照片', '照片列表查询', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试植物日记模块
 */
async function testDiary() {
  console.log('\n========== 测试7: 植物日记模块 ==========');
  
  // 测试日记列表
  try {
    const response = await axios.get(`${BASE_URL}/api/diary/list`, {
      headers: { Authorization: `Bearer ${authToken}` },
      params: { pageNum: 1, pageSize: 10 }
    });
    recordTest('植物日记', '日记列表查询', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('植物日记', '日记列表查询', false, error.response?.data?.message || error.message);
  }
  
  // 测试创建日记
  try {
    const response = await axios.post(`${BASE_URL}/api/diary`, {
      plantId: 1,
      content: '今天植物长势很好',
      mood: 'happy',
      weather: 'sunny',
      diaryDate: '2024-01-01'
    }, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    recordTest('植物日记', '创建日记', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('植物日记', '创建日记', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试提醒配置模块
 */
async function testReminderConfig() {
  console.log('\n========== 测试8: 提醒配置模块 ==========');
  
  // 测试获取提醒配置
  try {
    const response = await axios.get(`${BASE_URL}/api/reminder/config`, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    recordTest('提醒配置', '获取提醒配置', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('提醒配置', '获取提醒配置', false, error.response?.data?.message || error.message);
  }
  
  // 测试更新提醒配置
  try {
    const response = await axios.put(`${BASE_URL}/api/reminder/config`, {
      emailEnabled: true,
      smsEnabled: false,
      popupEnabled: true,
      bellEnabled: true,
      sceneConfig: JSON.stringify({ summaryTime: '09:00' })
    }, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    recordTest('提醒配置', '更新提醒配置', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('提醒配置', '更新提醒配置', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试消息提醒模块
 */
async function testReminder() {
  console.log('\n========== 测试9: 消息提醒模块 ==========');
  
  // 测试提醒列表
  try {
    const response = await axios.get(`${BASE_URL}/api/reminder/list`, {
      headers: { Authorization: `Bearer ${authToken}` },
      params: { pageNum: 1, pageSize: 10 }
    });
    recordTest('消息提醒', '提醒列表查询', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('消息提醒', '提醒列表查询', false, error.response?.data?.message || error.message);
  }
  
  // 测试未读数量
  try {
    const response = await axios.get(`${BASE_URL}/api/reminder/unread-count`, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    recordTest('消息提醒', '未读数量查询', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('消息提醒', '未读数量查询', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试论坛模块
 */
async function testForum() {
  console.log('\n========== 测试10: 论坛模块 ==========');
  
  // 测试帖子列表
  try {
    const response = await axios.get(`${BASE_URL}/api/forum/posts`, {
      params: { pageNum: 1, pageSize: 10 }
    });
    recordTest('论坛', '帖子列表查询', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('论坛', '帖子列表查询', false, error.response?.data?.message || error.message);
  }
  
  // 测试发布帖子
  try {
    const response = await axios.post(`${BASE_URL}/api/forum/posts`, {
      title: '测试帖子',
      content: '这是一个测试帖子的内容',
      category: 'general'
    }, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    recordTest('论坛', '发布帖子', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('论坛', '发布帖子', false, error.response?.data?.message || error.message);
  }
}

/**
 * 测试公告模块
 */
async function testAnnouncement() {
  console.log('\n========== 测试11: 公告模块 ==========');
  
  // 测试公告列表
  try {
    const response = await axios.get(`${BASE_URL}/api/admin/announcements/public/list`, {
      params: { pageNum: 1, pageSize: 10 }
    });
    recordTest('公告', '公告列表查询', response.data?.code === 200, response.data?.message);
  } catch (error) {
    recordTest('公告', '公告列表查询', false, error.response?.data?.message || error.message);
  }
}

/**
 * 打印测试报告
 */
function printReport() {
  console.log('\n\n');
  console.log('╔════════════════════════════════════════════════════╗');
  console.log('║           Greenly 全模块测试报告                   ║');
  console.log('╚════════════════════════════════════════════════════╝');
  console.log(`\n总测试数: ${results.total}`);
  console.log(`✅ 通过: ${results.passed}`);
  console.log(`❌ 失败: ${results.failed}`);
  console.log(`通过率: ${((results.passed / results.total) * 100).toFixed(2)}%`);
  
  console.log('\n\n详细结果:');
  console.log('─'.repeat(60));
  
  // 按模块分组显示
  const modules = {};
  results.details.forEach(detail => {
    if (!modules[detail.module]) {
      modules[detail.module] = [];
    }
    modules[detail.module].push(detail);
  });
  
  Object.keys(modules).forEach(module => {
    console.log(`\n【${module}】`);
    modules[module].forEach(detail => {
      const status = detail.success ? '✅' : '❌';
      console.log(`  ${status} ${detail.testName}${detail.message ? ' - ' + detail.message : ''}`);
    });
  });
  
  console.log('\n' + '═'.repeat(60));
}

/**
 * 主函数
 */
async function main() {
  console.log('╔════════════════════════════════════════════════════╗');
  console.log('║     Greenly 全模块功能测试开始                     ║');
  console.log('╚════════════════════════════════════════════════════╝');
  console.log(`测试时间: ${new Date().toLocaleString('zh-CN')}`);
  console.log(`API地址: ${BASE_URL}`);
  
  // 登录
  const loginSuccess = await login();
  if (!loginSuccess) {
    console.log('\n❌ 登录失败，终止测试');
    return;
  }
  
  // 执行各模块测试
  await testAuth();
  await testOfficialPlant();
  await testMyPlant();
  await testCareSchedule();
  await testCareRecord();
  await testPhoto();
  await testDiary();
  await testReminderConfig();
  await testReminder();
  await testForum();
  await testAnnouncement();
  
  // 打印报告
  printReport();
}

// 运行测试
main().catch(error => {
  console.error('\n💥 测试执行出错:', error);
  process.exit(1);
});
