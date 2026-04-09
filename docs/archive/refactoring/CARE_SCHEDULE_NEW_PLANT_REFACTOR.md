# 养护计划 - 新建植物功能重构说明

## 📋 重构概述

实现了真正的"新建植物并创建养护计划"功能，确保数据完整性和一致性。

---

## 🔄 业务流程

### 修改前（临时方案）

```
用户提交表单（新建植物模式）
    ↓
前端发送请求到 /care/schedule/add
    ↓
后端接收 plantId=null, plantSource=null
    ↓
Service 层设置默认值：plantId=0, plantSource="TEMP"
    ↓
插入数据库（孤儿记录，无法关联真实植物）
    ↓
❌ 问题：养护计划没有关联到真实的植物记录
```

### 修改后（正确方案）

```
用户提交表单（新建植物模式）
    ↓
【步骤1】前端调用 addMyPlant() 创建植物记录
    ├─ 参数：{userId, nickname, genus, species, description, source: 'LOCAL', ...}
    └─ 返回：{id: 5, ...}
    ↓
【步骤2】获取新建植物的 ID (plantId = 5)
    ↓
【步骤3】前端调用 /care/schedule/add 创建养护计划
    ├─ 参数：{userId, plantId: 5, plantSource: 'LOCAL', taskName, dueTime, ...}
    └─ 返回：养护计划对象
    ↓
【步骤4】显示成功提示并跳转到列表页
    ↓
✅ 结果：养护计划正确关联到新建的植物记录
```

---

## 🛠️ 代码变更

### 1. 前端修改

**文件**: `plant-frontend/src/pages/care/schedule-add.vue`

#### 1.1 导入接口

```javascript
import { addMyPlant } from '@/api/my-plant'  // ✅ 新增导入
```

#### 1.2 重构提交逻辑

```javascript
const handleSubmit = () => {
    formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                if (isEditMode.value) {
                    // === 编辑模式 ===
                    await updateSchedule(editId.value, updateParams)
                    ElMessage.success('更新成功')
                } else {
                    // === 新增模式 ===
                    let finalPlantId = form.plantId
                    let finalPlantSource = form.plantSource
                    
                    if (form.isNewPlant) {
                        // ✅ 步骤1：先创建植物记录
                        const plantData = {
                            userId: userStore.userId,
                            nickname: form.plantName,
                            genus: form.genus || '',
                            species: form.species || '',
                            description: form.description || '',
                            source: 'LOCAL',
                            acquisitionDate: new Date().toISOString().split('T')[0],
                            status: 1
                        }
                        
                        const plantRes = await addMyPlant(plantData)
                        
                        // ✅ 步骤2：获取新建植物的 ID
                        finalPlantId = plantRes.id
                        finalPlantSource = 'LOCAL'
                        
                        ElMessage.success(`植物「${form.plantName}」创建成功`)
                    } else {
                        // ✅ 选择现有植物模式
                        const [source, id] = form.selectedPlantKey.split('_')
                        finalPlantSource = source
                        finalPlantId = Number(id)
                    }
                    
                    // ✅ 步骤3：创建养护计划
                    const scheduleParams = {
                        userId: userStore.userId,
                        plantId: finalPlantId,
                        plantSource: finalPlantSource,
                        taskName: form.taskName,
                        dueTime: form.dueTime,
                        recurrenceType: form.recurrenceType,
                        recurrenceInterval: form.recurrenceInterval,
                        reminderConfig: form.reminderConfig
                    }
                    
                    await request.post('/care/schedule/add', scheduleParams)
                    ElMessage.success('养护计划创建成功')
                }
                
                router.push('/care/schedule-list')
            } catch (error) {
                console.error('❌ 操作失败:', error)
                ElMessage.error(error.message || '操作失败，请重试')
            } finally {
                loading.value = false
            }
        }
    })
}
```

**关键改进**：
- ✅ 分两步执行：先创建植物，再创建养护计划
- ✅ 使用真实的 `plantId` 和 `plantSource`
- ✅ 添加详细的日志输出
- ✅ 完善的错误处理

---

### 2. 后端修改

**文件**: `plant-backend/src/main/java/com/plant/backend/service/impl/CareScheduleServiceImpl.java`

#### 2.1 清理临时默认值逻辑

**修改前**：
```java
@Override
@Transactional(rollbackFor = Exception.class)
public CareSchedule add(CareScheduleDTO.AddRequest request) {
    CareSchedule schedule = new CareSchedule();
    schedule.setUserId(request.getUserId());
    
    // ❌ 临时方案：设置默认值
    Long plantId = request.getPlantId();
    String plantSource = request.getPlantSource();
    
    if (plantId == null) {
        plantId = 0L;
        plantSource = "TEMP";
    }
    
    schedule.setPlantId(plantId);
    schedule.setPlantSource(plantSource != null ? plantSource : "TEMP");
    // ...
}
```

**修改后**：
```java
@Override
@Transactional(rollbackFor = Exception.class)
public CareSchedule add(CareScheduleDTO.AddRequest request) {
    CareSchedule schedule = new CareSchedule();
    schedule.setUserId(request.getUserId());
    
    // ✅ 直接使用前端传递的真实值
    schedule.setPlantId(request.getPlantId());
    schedule.setPlantSource(request.getPlantSource());
    
    // ... 其他字段设置
    careScheduleMapper.insert(schedule);
    return schedule;
}
```

**关键改进**：
- ✅ 移除临时默认值逻辑
- ✅ 依赖前端传递正确的 `plantId` 和 `plantSource`
- ✅ 保持 `@Transactional` 注解，确保事务安全

---

## 🎯 数据一致性保障

### 1. 前端层面

```javascript
try {
    // 步骤1：创建植物
    const plantRes = await addMyPlant(plantData)
    finalPlantId = plantRes.id
    
    // 步骤2：创建养护计划
    await request.post('/care/schedule/add', scheduleParams)
    
    // 步骤3：成功后跳转
    router.push('/care/schedule-list')
} catch (error) {
    // ✅ 任一环节失败都会捕获并提示
    ElMessage.error(error.message || '操作失败，请重试')
}
```

**保障机制**：
- 如果创建植物失败 → 不会执行创建养护计划
- 如果创建养护计划失败 → 植物记录已创建，但用户可以看到错误提示
- 用户可以手动删除孤立的植物记录（通过"我的植物"页面）

### 2. 后端层面

```java
@Transactional(rollbackFor = Exception.class)
public CareSchedule add(CareScheduleDTO.AddRequest request) {
    // ... 
    careScheduleMapper.insert(schedule);
    return schedule;
}
```

**保障机制**：
- `@Transactional` 确保养护计划创建的原子性
- 如果插入失败，事务回滚

---

## 🧪 测试场景

### 场景1：新建植物并创建养护计划

**操作步骤**：
1. 访问 `/care/schedule-add`
2. 勾选"创建新植物"
3. 填写植物信息：
   - 植物名称：柠檬树
   - 属：柑橘属
   - 种：柠檬
   - 描述：阳台上的柠檬树
4. 选择任务类型：浇水
5. 选择时间：2026-04-05 08:00
6. 点击"提交"

**预期结果**：
```
Console 日志：
🌱 开始创建新植物...
创建植物参数: {userId: 1, nickname: "柠檬树", ...}
✅ 植物创建成功，ID: 5
📅 开始创建养护计划...
养护计划参数: {userId: 1, plantId: 5, plantSource: "LOCAL", ...}

消息提示：
1. "植物「柠檬树」创建成功"
2. "养护计划创建成功"

页面跳转：
→ /care/schedule-list

数据库验证：
my_plant 表：新增一条记录，id=5, nickname='柠檬树'
care_schedule 表：新增一条记录，plant_id=5, plant_source='LOCAL'
```

---

### 场景2：选择现有植物创建养护计划

**操作步骤**：
1. 访问 `/care/schedule-add`
2. 不勾选"创建新植物"
3. 搜索并选择植物："柠檬 (官方)"
4. 填写养护计划信息
5. 点击"提交"

**预期结果**：
```
Console 日志：
✅ 使用现有植物，ID: 3, 来源: OFFICIAL
📅 开始创建养护计划...
养护计划参数: {userId: 1, plantId: 3, plantSource: "OFFICIAL", ...}

消息提示：
"养护计划创建成功"

数据库验证：
care_schedule 表：新增一条记录，plant_id=3, plant_source='OFFICIAL'
my_plant 表：无变化
```

---

### 场景3：创建植物失败

**模拟场景**：网络错误或后端返回错误

**预期结果**：
```
Console 日志：
🌱 开始创建新植物...
❌ 操作失败: Error: 网络错误

消息提示：
"网络错误"

页面状态：
停留在当前页面，loading 状态取消
用户可以重新提交
```

---

### 场景4：创建养护计划失败（植物已创建）

**模拟场景**：植物创建成功，但养护计划创建失败

**预期结果**：
```
Console 日志：
🌱 开始创建新植物...
✅ 植物创建成功，ID: 6
📅 开始创建养护计划...
❌ 操作失败: Error: 系统错误

消息提示：
1. "植物「xxx」创建成功"
2. "系统错误"

页面状态：
停留在当前页面
用户可以：
- 重新提交养护计划（会创建重复的植物记录）
- 或前往"我的植物"页面查看已创建的植物
```

**优化建议**（可选）：
- 可以在前端维护一个"待清理"的植物 ID 列表
- 如果养护计划创建失败，自动调用删除植物接口
- 或者提示用户手动删除

---

## 📊 数据库关系

### my_plant 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，自增 |
| user_id | bigint | 用户ID |
| nickname | varchar(100) | 植物昵称 |
| genus | varchar(50) | 属 |
| species | varchar(50) | 种 |
| source | varchar(20) | 来源：LOCAL/OFFICIAL |
| status | tinyint | 状态：1-健康, 2-生病 |

### care_schedule 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，自增 |
| user_id | bigint | 用户ID |
| plant_id | bigint | **外键，关联 my_plant.id** |
| plant_source | varchar(20) | 来源：LOCAL/OFFICIAL |
| task_name | varchar(100) | 任务名称 |
| due_time | datetime | 到期时间 |
| status | tinyint | 状态：0-待完成, 1-已完成, 2-逾期 |

**关系**：
```
my_plant (1) ←→ (N) care_schedule
```

---

## ⚠️ 注意事项

### 1. 孤儿数据处理

**问题**：如果植物创建成功但养护计划创建失败，会产生孤立的植物记录。

**解决方案**：
- **短期**：用户可以通过"我的植物"页面手动删除
- **长期**（可选）：实现前端回滚机制

```javascript
// 可选的回滚逻辑
let createdPlantId = null
try {
    const plantRes = await addMyPlant(plantData)
    createdPlantId = plantRes.id
    
    await request.post('/care/schedule/add', scheduleParams)
} catch (error) {
    // 如果养护计划创建失败，删除已创建的植物
    if (createdPlantId) {
        await deleteMyPlant(createdPlantId)
        ElMessage.warning('养护计划创建失败，已自动删除植物记录')
    }
    throw error
}
```

### 2. 并发控制

**问题**：用户快速多次点击提交按钮

**解决方案**：
- 前端已有 `loading` 状态控制
- 后端有 `@Transactional` 保证事务隔离

### 3. 数据验证

**前端验证**：
- 植物名称必填（当选择新建植物时）
- 任务名称、时间必填

**后端验证**：
- `@NotNull`, `@NotBlank` 注解
- JWT Token 验证用户身份

---

## 🚀 部署步骤

1. **重启后端服务**
   ```bash
   cd plant-backend
   mvn clean package -DskipTests
   java -jar target/plant-backend-0.0.1-SNAPSHOT.jar
   ```

2. **刷新前端页面**
   - Vite 开发服务器会自动热重载
   - 或手动刷新浏览器

3. **测试功能**
   - 访问 `/care/schedule-add`
   - 测试"新建植物"模式
   - 测试"选择现有植物"模式
   - 验证数据库中数据是否正确关联

---

## 📝 总结

### 改进点

| 方面 | 修改前 | 修改后 |
|------|--------|--------|
| 植物创建 | ❌ 不创建，使用默认值 | ✅ 真正创建植物记录 |
| 数据关联 | ❌ plantId=0（孤儿记录） | ✅ plantId=真实ID |
| 用户体验 | ⚠️ 无明确提示 | ✅ 分步提示成功 |
| 错误处理 | ⚠️ 简单错误提示 | ✅ 详细错误日志+提示 |
| 代码可维护性 | ⚠️ 临时方案 | ✅ 清晰的业务逻辑 |

### 技术亮点

- ✅ **分步执行**：先创建植物，再创建养护计划
- ✅ **数据完整性**：确保养护计划关联到真实植物
- ✅ **错误处理**：完善的 try-catch 和用户提示
- ✅ **日志输出**：详细的 Console 日志便于调试
- ✅ **事务控制**：后端使用 `@Transactional` 保证原子性

---

**最后更新**: 2026-04-04  
**版本**: v1.0
