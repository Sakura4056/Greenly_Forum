# 📚 Greenly 文档体系说明

本文档说明 Greenly 项目的完整文档体系和组织结构。

---

## 🎯 文档设计理念

### 设计原则

1. **用户友好** - 按角色和使用场景组织
2. **层次清晰** - 从快速入门到深度使用
3. **内容完整** - 覆盖开发、测试、部署全流程
4. **易于维护** - 模块化，避免重复
5. **方便检索** - 清晰的索引和交叉引用

---

## 📁 文档结构

### 项目根目录

```
project-root/
├── README.md                     # 项目主文档（对外展示）
├── docs/                         # 📚 文档目录
│   ├── README.md                # 文档中心索引
│   ├── DATABASE_QUICK_REFERENCE.md
│   ├── PASSWORD_ENCRYPTION_FIX.md
│   ├── TROUBLESHOOTING.md
│   ├── MIGRATION_GUIDE.md
│   └── ... (其他专题文档)
├── plant-backend/               # 后端项目
└── plant-frontend/              # 前端项目
```

### 文档分类

#### 1️⃣ 项目主文档（README.md）
**位置**: 项目根目录  
**用途**: 对外展示，快速了解项目  
**内容**:
- 项目介绍
- 特性亮点
- 快速开始
- 文档导航
- 技术栈
- 常见问题

#### 2️⃣ 文档中心索引（docs/README.md）
**位置**: docs/README.md  
**用途**: 文档体系的总入口  
**内容**:
- 快速开始指引
- 完整文档目录
- 按角色分类推荐
- 文件位置指南
- 重要信息速查

#### 3️⃣ 专题文档
每个专题一个独立文档，聚焦特定主题。

---

## 📖 现有文档列表

### 核心文档（必备）

| 文档 | 文件名 | 说明 | 状态 |
|------|--------|------|------|
| **项目 README** | `README.md` | 项目主文档 | ✅ 已创建 |
| **文档索引** | `docs/README.md` | 文档中心入口 | ✅ 已创建 |
| **数据库快速参考** | `docs/DATABASE_QUICK_REFERENCE.md` | 数据库基本信息 | ✅ 已创建 |
| **故障排查** | `docs/TROUBLESHOOTING.md` | 常见问题解决 | ✅ 已创建 |

### 重要文档

| 文档 | 文件名 | 说明 | 状态 |
|------|--------|------|------|
| **密码加密修复** | `docs/PASSWORD_ENCRYPTION_FIX.md` | BCrypt 问题 | ✅ 已创建 |
| **迁移指南** | `docs/MIGRATION_GUIDE.md` | 数据库迁移 | ✅ 已创建 |

### 待创建文档

以下文档规划但尚未创建：

| 文档 | 文件名 | 说明 | 优先级 |
|------|--------|------|--------|
| **数据库完整指南** | `docs/DATABASE_COMPLETE_GUIDE.md` | 详细数据库文档 | 🔴 高 |
| **数据库设计文档** | `docs/DATABASE_DESIGN.md` | 表结构设计 | 🔴 高 |
| **后端开发指南** | `docs/BACKEND_DEVELOPMENT.md` | Spring Boot 开发 | 🟡 中 |
| **前端开发指南** | `docs/FRONTEND_DEVELOPMENT.md` | Vue 3 开发 | 🟡 中 |
| **技术栈说明** | `docs/TECH_STACK.md` | 技术介绍 | 🟡 中 |
| **代码规范** | `docs/CODING_STANDARDS.md` | 开发规范 | 🟢 低 |
| **部署指南** | `docs/DEPLOYMENT.md` | 生产部署 | 🟢 低 |

---

## 🎯 文档使用路径

### 👨‍💻 开发者路径

```
README.md (了解项目)
  ↓
docs/README.md (进入文档中心)
  ↓
docs/TECH_STACK.md (了解技术栈)
  ↓
docs/BACKEND_DEVELOPMENT.md (后端开发)
  ↓
docs/FRONTEND_DEVELOPMENT.md (前端开发)
  ↓
docs/CODING_STANDARDS.md (遵循规范)
```

### 🚀 运维人员路径

```
README.md (了解项目)
  ↓
docs/README.md (进入文档中心)
  ↓
docs/DATABASE_QUICK_REFERENCE.md (数据库信息)
  ↓
docs/DATABASE_COMPLETE_GUIDE.md (详细指南)
  ↓
docs/DEPLOYMENT.md (部署)
  ↓
docs/TROUBLESHOOTING.md (排错)
```

### 👶 新手用户路径

```
README.md (项目介绍)
  ↓
README.md#快速开始 (跟随指引)
  ↓
docs/DATABASE_QUICK_REFERENCE.md (安装数据库)
  ↓
docs/TROUBLESHOOTING.md (遇到问题)
```

---

## 📝 文档编写规范

### 标题层级

```markdown
# H1 - 文档标题（每篇文档只有一个）
## H2 - 主要章节
### H3 - 子章节
#### H4 - 细节内容
```

### 代码块

````markdown
```java
// 标注语言类型
public class Example {
    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
```
````

### 表格样式

```markdown
| 列 1 | 列 2 | 列 3 |
|------|------|------|
| 内容 | 内容 | 内容 |
```

### Emoji 使用

适度使用 Emoji 增强可读性：
- 🚀 快速开始
- 📚 文档资料
- 🛠️ 工具相关
- ⚠️ 注意事项
- ✅ 完成状态
- ❌ 错误问题

---

## 🔄 文档更新流程

### 何时更新文档

1. **新增功能** - 同步更新相关文档
2. **修改配置** - 更新配置说明
3. **发现问题** - 补充故障排查
4. **优化体验** - 改进文档结构

### 更新步骤

1. 确定影响的文档
2. 修改相关内容
3. 更新"最后更新"日期
4. 必要时更新文档索引

---

## 📊 文档完整性检查

### 检查清单

- [ ] README.md 包含快速开始
- [ ] docs/README.md 作为统一入口
- [ ] 每个主题有独立文档
- [ ] 文档之间有交叉引用
- [ ] 包含故障排查内容
- [ ] 提供示例代码
- [ ] 标注最后更新日期

### 质量要求

- ✅ **准确性** - 内容正确无误
- ✅ **完整性** - 覆盖必要信息
- ✅ **清晰性** - 表达清楚易懂
- ✅ **一致性** - 格式风格统一
- ✅ **可维护** - 便于更新扩展

---

## 🎯 文档优化目标

### 第一阶段（已完成）✅

- [x] 创建统一的文档索引
- [x] 整合重复的文档内容
- [x] 删除过时的文档
- [x] 建立清晰的文档结构

### 第二阶段（进行中）🚧

- [ ] 创建数据库完整指南
- [ ] 创建数据库设计文档
- [ ] 完善故障排查内容

### 第三阶段（计划中）📋

- [ ] 创建后端开发指南
- [ ] 创建前端开发指南
- [ ] 创建部署指南
- [ ] 添加更多示例和最佳实践

---

## 📞 文档反馈

如果您发现文档有问题或有改进建议：

1. 提交 Issue 说明问题
2. 创建 Pull Request 改进
3. 联系文档维护者

---

## ✨ 文档亮点

1. **统一入口** - docs/README.md 作为唯一入口
2. **角色分类** - 按用户角色组织内容
3. **层次清晰** - 从入门到精通
4. **易于维护** - 模块化，避免重复
5. **持续更新** - 随项目发展不断完善

---

*文档体系版本：v1.0*  
*最后更新：2026-04-03*  
*维护者：Greenly 开发团队*
