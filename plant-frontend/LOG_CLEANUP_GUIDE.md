# 生产环境日志清理指南

## 概述

本文档指导如何清理生产环境中的调试日志，避免性能损耗和敏感信息泄露。

---

## 后端日志清理

### 1. 已实施措施

✅ **生产环境配置文件** (`application-prod.yml`)
- 根日志级别设置为 `WARN`
- 应用包日志级别设置为 `INFO`
- SQL 日志已关闭（使用 `NoLoggingImpl`）
- 日志文件轮转配置完成

### 2. 使用方法

```bash
# 使用生产配置启动
java -jar plant-backend.jar --spring.profiles.active=prod

# 或设置环境变量
export SPRING_PROFILES_ACTIVE=prod
java -jar plant-backend.jar
```

### 3. 日志文件位置

```
logs/
├── plant-backend.log           # 当前日志
├── plant-backend.log.2026-04-01.0.gz  # 归档日志
├── plant-backend.log.2026-04-02.0.gz
└── ...
```

### 4. 需要手动清理的代码

以下文件包含 `System.out.println`，但属于工具类或测试用途，可以保留：

- ✅ `util/PasswordUtils.java` - main 方法仅开发时使用
- ✅ `test/` 目录下的所有测试文件

如需完全清理，请检查：
```bash
cd plant-backend
grep -r "System.out.println" src/main/java --include="*.java"
```

---

## 前端日志清理

### 1. 自动清理脚本

创建以下文件并运行：

**文件**: `scripts/cleanup-console-logs.js`

```javascript
const fs = require('fs');
const path = require('path');

const SRC_DIR = path.join(__dirname, '../src');
const EXCLUDE_DIRS = ['node_modules', '.git', 'e2e'];
const LOG_PATTERN = /console\.(log|warn|error|info|debug|trace)\(/g;

function shouldExclude(dirPath) {
  return EXCLUDE_DIRS.some(exclude => dirPath.includes(exclude));
}

function cleanFile(filePath) {
  let content = fs.readFileSync(filePath, 'utf8');
  const originalContent = content;
  
  // 移除 console.log 等语句
  content = content.replace(LOG_PATTERN, '// [PRODUCTION-REMOVED] console.');
  
  if (content !== originalContent) {
    fs.writeFileSync(filePath, content, 'utf8');
    console.log(`✓ Cleaned: ${path.relative(SRC_DIR, filePath)}`);
    return true;
  }
  return false;
}

function walkDir(dir) {
  const files = fs.readdirSync(dir);
  
  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);
    
    if (stat.isDirectory()) {
      if (!shouldExclude(filePath)) {
        walkDir(filePath);
      }
    } else if (file.endsWith('.js') || file.endsWith('.vue')) {
      cleanFile(filePath);
    }
  });
}

console.log('Starting console.log cleanup...');
walkDir(SRC_DIR);
console.log('Cleanup complete!');
```

运行：
```bash
node scripts/cleanup-console-logs.js
```

### 2. 手动清理清单

以下文件包含 `console.log` 语句，建议在生产构建前清理：

#### 高优先级（包含敏感信息）
- [ ] `api/request.js` - 请求/响应拦截器中的详细日志
- [ ] `stores/user.js` - 用户状态管理日志

#### 中优先级（调试信息）
- [ ] `pages/care/schedule-add.vue` - 养护计划创建日志
- [ ] `pages/care/record-add.vue` - 养护记录添加日志
- [ ] `pages/plant/my-list.vue` - 植物列表日志

#### 低优先级（路由/通用）
- [ ] `router/index.js` - 路由守卫日志
- [ ] `pages/login/index.vue` - 登录页面日志
- [ ] `pages/plant/official-detail.vue` - 植物详情日志

### 3. 使用环境变量控制日志

创建日志工具类，根据环境自动禁用日志：

**文件**: `src/utils/logger.js`

```javascript
const isProduction = import.meta.env.PROD;

const logger = {
  log: (...args) => {
    if (!isProduction) {
      console.log(...args);
    }
  },
  warn: (...args) => {
    if (!isProduction) {
      console.warn(...args);
    }
  },
  error: (...args) => {
    // 生产环境也保留错误日志
    console.error(...args);
  },
  info: (...args) => {
    if (!isProduction) {
      console.info(...args);
    }
  }
};

export default logger;
```

**使用方法**:
```javascript
// 替换前
console.log('调试信息:', data);

// 替换后
import logger from '@/utils/logger';
logger.log('调试信息:', data);
```

### 4. Vite 构建时自动移除

在 `vite.config.js` 中添加：

```javascript
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig(({ mode }) => ({
  plugins: [vue()],
  build: {
    minify: 'terser',
    terserOptions: {
      compress: {
        // 生产环境移除 console
        drop_console: mode === 'production',
        drop_debugger: true,
      },
    },
  },
}));
```

安装依赖：
```bash
npm install -D terser
```

---

## 验证清理效果

### 后端验证

```bash
# 启动生产环境
java -jar plant-backend.jar --spring.profiles.active=prod

# 观察日志输出（应该只有 WARN 和 ERROR）
tail -f logs/plant-backend.log
```

### 前端验证

```bash
# 生产构建
npm run build

# 预览构建结果
npm run preview

# 打开浏览器控制台，确认没有调试日志
```

---

## 最佳实践

### 1. 日志分级

```javascript
// ✅ 推荐
logger.error('数据库连接失败', error);  // 生产环境保留
logger.warn('缓存未命中，使用默认值');   // 生产环境可选
logger.info('用户登录成功');            // 生产环境可选
logger.debug('变量值:', variable);      // 仅开发环境
logger.trace('函数调用栈');             // 仅开发环境
```

### 2. 敏感信息保护

```javascript
// ❌ 避免
console.log('用户密码:', password);
console.log('JWT Token:', token);
console.log('API Key:', apiKey);

// ✅ 推荐
logger.info('用户登录成功', { userId, username }); // 不包含敏感信息
```

### 3. 结构化日志

```java
// ✅ 后端推荐（使用 SLF4J）
private static final Logger log = LoggerFactory.getLogger(UserService.class);
log.info("用户登录成功, userId={}, username={}", userId, username);

// ❌ 避免
System.out.println("用户登录成功: " + userId + ", " + username);
```

---

## CI/CD 集成

在部署流水线中添加日志检查步骤：

**.github/workflows/deploy.yml**

```yaml
- name: Check for debug logs
  run: |
    if grep -r "console\.log" src/ --include="*.js" --include="*.vue" | grep -v "// \[PRODUCTION"; then
      echo "❌ Found debug console.log statements!"
      exit 1
    fi
    echo "✅ No debug logs found"
```

---

**最后更新**: 2026-04-04
**维护者**: Greenly 开发团队
