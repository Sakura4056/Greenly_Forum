# 百度AI图像识别配置指南

## 概述

系统已集成百度AI开放平台的图像识别API，用于植物识别功能。

## 配置步骤

### 1. 注册百度AI账号

1. 访问 [百度AI开放平台](https://ai.baidu.com/)
2. 注册并登录账号
3. 进入控制台

### 2. 创建应用

1. 在控制台中选择"图像识别"服务
2. 点击"创建应用"
3. 填写应用信息：
   - 应用名称：Greenly Plant Recognition
   - 应用描述：植物识别与诊断系统
4. 创建成功后，获取以下凭证：
   - **App ID**
   - **API Key**
   - **Secret Key**

### 3. 配置环境变量

#### 方式一：直接修改配置文件（开发环境）

编辑 `plant-backend/src/main/resources/application.yml`：

```yaml
baidu:
  ai:
    app-id: your_app_id_here
    api-key: your_api_key_here
    secret-key: your_secret_key_here
```

#### 方式二：使用环境变量（推荐生产环境）

设置以下环境变量：

```bash
# Windows PowerShell
$env:BAIDU_AI_APP_ID="your_app_id"
$env:BAIDU_AI_API_KEY="your_api_key"
$env:BAIDU_AI_SECRET_KEY="your_secret_key"

# Linux/Mac
export BAIDU_AI_APP_ID="your_app_id"
export BAIDU_AI_API_KEY="your_api_key"
export BAIDU_AI_SECRET_KEY="your_secret_key"
```

### 4. 验证配置

启动后端服务后，检查日志输出：

- 成功：`Baidu AI client initialized successfully`
- 失败：`Baidu AI credentials not configured. Image recognition will use mock data.`

## API使用说明

### 植物识别

- **端点**: `POST /api/ai/diagnose-image`
- **请求体**:
```json
{
  "imageUrl": "base64_encoded_image_or_url",
  "diagnosisType": "identification",
  "provider": "baidu"
}
```

### 养护建议

- **诊断类型**: `care`
- 先识别植物种类，然后生成养护建议

## 注意事项

1. **API调用限制**: 百度AI免费额度为每天500次调用
2. **图片格式**: 支持JPG、PNG格式，大小不超过4MB
3. **网络要求**: 服务器需要能够访问百度AI API
4. **降级策略**: 如果未配置百度AI凭证，系统将返回模拟数据

## 故障排除

### 问题1: 编译错误 "找不到符号 PostConstruct"

**解决方案**: 已添加 `jakarta.annotation-api` 依赖

### 问题2: 运行时错误 "Baidu AI client not initialized"

**原因**: 未配置百度AI凭证

**解决方案**: 按照上述步骤配置App ID、API Key和Secret Key

### 问题3: API调用超时

**原因**: 网络连接问题或百度API服务异常

**解决方案**:
- 检查网络连接
- 验证API凭证是否正确
- 查看百度AI平台状态

## 技术实现

### 后端架构

```
Frontend -> AiController -> AiServiceImpl -> BaiduAiService -> Baidu AI API
```

### 关键文件

- `BaiduAiService.java`: 百度AI服务封装
- `AiServiceImpl.java`: AI服务实现，集成百度AI
- `application.yml`: 配置文件
- `pom.xml`: Maven依赖配置

### 依赖版本

- 百度AI Java SDK: 4.16.19
- Jakarta Annotation API: 2.1.1

## 扩展建议

1. **添加缓存**: 对相同图片的识别结果进行缓存
2. **异步处理**: 对于大图识别，使用异步任务
3. **结果优化**: 结合多种AI提供商提高准确率
4. **监控统计**: 记录API调用次数和成功率
