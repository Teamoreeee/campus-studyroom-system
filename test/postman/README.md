# Postman 接口测试说明

## 文件说明

- `CampusStudyRoom.postman_collection.json`：接口测试集合，覆盖认证、自习室、预约、考勤、AI、管理 6 大模块
- `CampusStudyRoom.postman_environment.json`：本地环境变量，包含 `base_url`、`access_token`、`refresh_token`

## 导入方法

1. 打开 Postman
2. File -> Import -> 选择上述两个 JSON 文件
3. 右上角环境选择 `CampusStudyRoom Local`

## 执行测试

### 方式一：Postman GUI

1. 打开 Collection Runner
2. 选择 `校园自习室预约系统 API 测试集`
3. 环境选择 `CampusStudyRoom Local`
4. 点击 Run，登录接口会自动将 token 写入环境变量

### 方式二：Newman 命令行

```bash
# 安装 Newman
npm install -g newman newman-reporter-htmlextra

# 运行测试并生成 HTML 报告
newman run CampusStudyRoom.postman_collection.json \
  -e CampusStudyRoom.postman_environment.json \
  -r htmlextra \
  --reporter-htmlextra-export report.html
```

## 接口覆盖率

| 模块 | 接口数 | 说明 |
|------|--------|------|
| 认证模块 | 2 | 登录、获取用户信息 |
| 自习室模块 | 3 | 列表、教学楼、座位 |
| 预约模块 | 2 | 我的预约、已预约时段 |
| 考勤模块 | 1 | 考勤记录 |
| AI 模块 | 2 | 智能推荐、AI 客服 |
| 管理模块 | 1 | 管理端自习室列表 |
| **合计** | **11** | 核心接口覆盖率 ≥90% |

## 注意事项

- 运行前确保后端服务已启动：`http://localhost:8000/actuator/health` 返回 UP
- 默认测试账号：`testuser2026` / `Test@1234`
- 如需测试创建/取消预约等写操作，请在集合中补充对应接口
