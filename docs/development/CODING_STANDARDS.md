# Greenly 代码规范

**文档版本**: v1.0  
**最后更新: 2026-04-09  
**维护者**: Greenly 开发团队

---

## 📋 目录

- [Java 代码规范](#java-代码规范)
- [Vue 代码规范](#vue-代码规范)
- [Git 提交规范](#git-提交规范)
- [数据库规范](#数据库规范)
- [API 设计规范](#api-设计规范)

---

## ☕ Java 代码规范

### 命名规范

#### 类名 - PascalCase（大驼峰）

```java
// ✅ 正确
public class UserController { }
public class PlantDiaryService { }
public class BusinessException { }

// ❌ 错误
public class userController { }
public class plant_diary_service { }
```

**规则**:
- 控制器以 `Controller` 结尾
- 服务层以 `Service` 结尾
- 实现类以 `ServiceImpl` 结尾
- 数据访问层以 `Mapper` 结尾
- 实体类使用业务名称（无后缀）
- 异常类以 `Exception` 结尾
- DTO类以 `DTO` 或 `Request/Response` 结尾

#### 方法名 - camelCase（小驼峰）

```java
// ✅ 正确
public User getUserById(Long id) { }
public List<Plant> queryPlantsByName(String name) { }
public void deletePlantDiary(Long diaryId) { }

// ❌ 错误
public User GetUserById(Long id) { }
public List<Plant> query_plants(String name) { }
```

**规则**:
- getter/setter遵循JavaBean规范
- 查询方法以 `get/query/find/list` 开头
- 新增方法以 `create/add/save` 开头
- 更新方法以 `update/modify` 开头
- 删除方法以 `delete/remove` 开头
- 布尔方法以 `is/has/can` 开头

#### 变量名 - camelCase

```java
// ✅ 正确
private String userName;
private Long plantId;
private List<MyPlant> myPlants;
private boolean isDeleted;

// ❌ 错误
private String user_name;
private Long PlantID;
private List<MyPlant> my_plant_list;
```

#### 常量 - UPPER_SNAKE_CASE

```java
// ✅ 正确
public static final String DEFAULT_ROLE = "USER";
public static final int MAX_RETRY_COUNT = 3;
public static final long TOKEN_EXPIRATION = 86400000L;

// ❌ 错误
public static final String defaultRole = "USER";
public static final int maxRetryCount = 3;
```

#### 包名 - 全小写

```java
// ✅ 正确
package com.plant.backend.controller;
package com.plant.backend.service.impl;
package com.plant.backend.mapper;

// ❌ 错误
package com.plant.backend.Controller;
package com.plant.Backend.controller;
```

---

### 注释规范

#### 类注释 - JavaDoc

```java
/**
 * 用户控制器
 * <p>
 * 提供用户注册、登录、信息查询等接口
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-03
 * @version 1.0
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    // ...
}
```

#### 方法注释 - JavaDoc（公共方法必须有）

```java
/**
 * 根据ID获取用户信息
 *
 * @param userId 用户ID，不能为null
 * @return 用户信息对象
 * @throws BusinessException 当用户不存在时抛出
 */
public User getUserById(Long userId) {
    // ...
}
```

#### 行内注释

```java
// ✅ 正确：解释复杂逻辑
if (user.getRole().equals("ADMIN")) {
    // 管理员拥有所有权限，跳过权限检查
    return true;
}

// ❌ 错误：注释显而易见的代码
user.setName(name); // 设置用户名
```

**规则**:
- 注释应解释"为什么"，而不是"做什么"
- 避免中文乱码，确保文件编码为UTF-8
- 临时注释使用 `// TODO:` 或 `// FIXME:` 标记

---

### 异常处理

#### 统一使用 BusinessException

```java
// ✅ 正确
if (user == null) {
    throw new BusinessException(ResultCode.USER_NOT_FOUND);
}

// ❌ 错误：吞掉异常
try {
    userService.deleteUser(userId);
} catch (Exception e) {
    // 什么都不做
}
```

#### Controller层统一异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统内部错误");
    }
}
```

---

### 日志规范

#### 使用 SLF4J + Lombok

```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public void createUser(User user) {
        log.info("创建新用户: username={}", user.getUsername());
        try {
            // 业务逻辑
            log.debug("用户创建成功: userId={}", user.getId());
        } catch (Exception e) {
            log.error("创建用户失败: username={}", user.getUsername(), e);
            throw new BusinessException("创建用户失败");
        }
    }
}
```

**日志级别使用**:
- `ERROR`: 系统错误、异常
- `WARN`: 警告信息、降级处理
- `INFO`: 关键业务流程（登录、创建、删除等）
- `DEBUG`: 调试信息（详细参数、中间状态）

**禁止事项**:
- ❌ 禁止使用 `System.out.println()`
- ❌ 禁止在循环中打印大量日志
- ❌ 禁止输出敏感信息（密码、Token等）

---

### 代码风格

#### 使用 Lombok 简化代码

```java
// ✅ 推荐
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
}

// ❌ 不推荐：手动编写getter/setter
public class UserDTO {
    private Long userId;
    private String username;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    // ...
}
```

#### 依赖注入使用构造器注入

```java
// ✅ 推荐：构造器注入（配合 @RequiredArgsConstructor）
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
}

// ❌ 不推荐：字段注入
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
}
```

#### 集合判空使用 CollectionUtils

```java
// ✅ 推荐
if (CollectionUtils.isEmpty(plants)) {
    return Collections.emptyList();
}

// ❌ 不推荐
if (plants == null || plants.size() == 0) {
    return new ArrayList<>();
}
```

---

## 🎨 Vue 代码规范

### 组件命名

#### 文件名 - kebab-case（短横线）

```
✅ 正确
src/pages/user/profile.vue
src/components/plant-card.vue
src/layouts/default-layout.vue

❌ 错误
src/pages/UserProfile.vue
src/components/PlantCard.vue
```

#### 组件名 - PascalCase（在 `<script>` 中）

```javascript
// ✅ 正确
<script setup>
defineOptions({
    name: 'UserProfile'
})
</script>

// ✅ 也接受（单文件组件可省略name）
<script setup>
// 组件名由路由或父组件决定
</script>
```

---

### Composition API 规范

#### 使用 `<script setup>` 语法糖

```vue
<!-- ✅ 推荐 -->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const userInfo = computed(() => userStore.userInfo)

const fetchUser = async () => {
    loading.value = true
    try {
        await userStore.fetchUserInfo()
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchUser()
})
</script>
```

#### 响应式数据声明顺序

```javascript
// ✅ 推荐的顺序
<script setup>
// 1. 导入
import { ref, computed } from 'vue'

// 2. Store/Router
const userStore = useUserStore()
const router = useRouter()

// 3. 响应式状态
const loading = ref(false)
const form = reactive({ name: '', email: '' })

// 4. 计算属性
const isValid = computed(() => form.name && form.email)

// 5. 方法
const handleSubmit = () => { /* ... */ }

// 6. 生命周期
onMounted(() => { /* ... */ })
</script>
```

---

### 模板规范

#### 属性绑定使用简写

```vue
<!-- ✅ 推荐 -->
<img :src="imageUrl" :alt="title" />
<button @click="handleClick">提交</button>

<!-- ❌ 不推荐 -->
<img v-bind:src="imageUrl" v-bind:alt="title" />
<button v-on:click="handleClick">提交</button>
```

#### 条件渲染优先级

```vue
<!-- ✅ 推荐：v-if 用于切换频率低的场景 -->
<div v-if="isLoggedIn">欢迎回来</div>
<div v-else>请登录</div>

<!-- ✅ 推荐：v-show 用于频繁切换 -->
<div v-show="isLoading">加载中...</div>
```

#### 列表渲染必须带 key

```vue
<!-- ✅ 正确 -->
<li v-for="plant in plants" :key="plant.id">{{ plant.name }}</li>

<!-- ❌ 错误：使用index作为key -->
<li v-for="(plant, index) in plants" :key="index">{{ plant.name }}</li>
```

---

### 样式规范

#### 使用 scoped CSS

```vue
<style scoped lang="scss">
.user-profile {
    padding: 20px;

    .avatar {
        width: 100px;
        height: 100px;
        border-radius: 50%;
    }
}
</style>
```

#### 避免 !important

```scss
// ✅ 推荐：提高选择器优先级
.user-profile .card .title {
    color: red;
}

// ❌ 不推荐
.title {
    color: red !important;
}
```

#### 使用 CSS 变量

```scss
// ✅ 推荐
.card {
    background-color: var(--color-bg-primary);
    color: var(--color-text-main);
    border: 1px solid var(--color-border);
}

// ❌ 不推荐：硬编码颜色
.card {
    background-color: #ffffff;
    color: #333333;
}
```

---

### 组件通信规范

#### Props 定义

```javascript
// ✅ 推荐：完整的props定义
const props = defineProps({
    userId: {
        type: Number,
        required: true
    },
    userName: {
        type: String,
        default: ''
    },
    tags: {
        type: Array,
        default: () => []
    }
})
```

#### Emit 事件

```javascript
// ✅ 推荐：定义emit
const emit = defineEmits(['update', 'delete'])

const handleUpdate = () => {
    emit('update', newData)
}
```

---

## 📝 Git 提交规范

### Commit Message 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(auth): 添加微信登录功能` |
| `fix` | Bug修复 | `fix(photo): 修复图片上传403错误` |
| `docs` | 文档更新 | `docs: 更新部署指南` |
| `style` | 代码格式（不影响功能） | `style: 格式化前端代码` |
| `refactor` | 重构 | `refactor(user): 重构用户服务层` |
| `perf` | 性能优化 | `perf: 优化植物列表查询` |
| `test` | 测试相关 | `test: 添加用户登录单元测试` |
| `chore` | 构建/工具变动 | `chore: 升级Element Plus版本` |

### 示例

```bash
# ✅ 好的提交
git commit -m "feat(reminder): 添加邮件提醒配置功能"
git commit -m "fix(ai): 修复DeepSeek API流式响应解析错误"
git commit -m "docs: 更新技术栈说明文档"

# ❌ 不好的提交
git commit -m "update"
git commit -m "fix bug"
git commit -m "修改代码"
```

### 分支命名

```
main              # 主分支
develop           # 开发分支
feature/xxx       # 功能分支
fix/xxx           # 修复分支
hotfix/xxx        # 紧急修复分支
release/v1.x      # 发布分支
```

---

## 🗄️ 数据库规范

### 表命名

- 使用小写字母和下划线
- 业务表加前缀（如 `sys_`, `plant_`）
- 复数形式或单数形式保持一致（本项目使用单数）

```sql
-- ✅ 正确
CREATE TABLE sys_user ( ... );
CREATE TABLE official_plant ( ... );
CREATE TABLE care_schedule ( ... );

-- ❌ 错误
CREATE TABLE SysUser ( ... );
CREATE TABLE OfficialPlant ( ... );
```

### 字段命名

- 使用小写字母和下划线
- 主键统一为 `id` 或 `{table}_id`
- 外键命名为 `{referenced_table}_id`
- 布尔字段以 `is_` 或 `has_` 开头

```sql
-- ✅ 正确
id BIGINT PRIMARY KEY AUTO_INCREMENT,
user_id BIGINT NOT NULL,
username VARCHAR(50) NOT NULL,
is_deleted TINYINT DEFAULT 0,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP

-- ❌ 错误
Id BIGINT,
UserId BIGINT,
UserName VARCHAR(50),
Deleted TINYINT
```

### 索引规范

- 主键自动创建索引
- 外键字段建立索引
- 频繁查询字段建立索引
- 联合索引遵循最左前缀原则

```sql
-- ✅ 推荐
CREATE INDEX idx_user_id ON care_schedule(user_id);
CREATE INDEX idx_status_created ON care_schedule(status, created_at);
```

---

## 🔌 API 设计规范

### RESTful 风格

```
GET    /api/users          # 获取用户列表
GET    /api/users/{id}     # 获取单个用户
POST   /api/users          # 创建用户
PUT    /api/users/{id}     # 更新用户
DELETE /api/users/{id}     # 删除用户
```

### 统一响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": 1,
        "username": "admin"
    }
}
```

### 错误码规范

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 📚 相关文档

- [技术栈说明](TECH_STACK.md)
- [项目结构](../PROJECT_STRUCTURE.md)
- [快速开始](../QUICK_START.md)

---

*本文档将随项目发展持续更新*
