# 校园自习室预约系统

<p align="center">
  <b>Campus Study Room Reservation System</b><br>
  基于微服务架构的校园自习室资源预约与管理平台
</p>

---

## 项目简介

本项目是一个面向高校的**自习室预约管理系统**，采用前后端分离 + 微服务架构，解决高校自习室资源紧张、占座现象严重、考勤管理低效等问题。系统支持学生在线预约座位、签到签退、查看历史记录，并提供基于协同过滤的 AI 座位推荐和基于 RAG 的 AI 客服问答能力。

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.5 | 微服务基础框架 |
| Spring Cloud | 2023.0.1 | 微服务治理 |
| Spring Cloud Alibaba | 2023.0.1.2 | Nacos 注册配置中心、Gateway 网关 |
| MyBatis-Plus | 3.5.8 | ORM 框架 |
| MySQL | 8.0 | 业务数据库 |
| Redis | 7.0 | 缓存 / 会话 / 分布式锁 |
| JWT | 0.11.5 | 无状态身份认证 |
| Maven | - | 项目构建 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.21 | 前端框架 |
| TypeScript | 5.4.5 | 类型支持 |
| Vite | 5.2.6 | 构建工具 |
| Element Plus | 2.6.1 | UI 组件库 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.3.0 | 路由管理 |
| ECharts | 5.5.0 | 数据可视化 |

### AI 能力

- **协同过滤推荐**：基于用户历史预约行为，推荐相似座位/自习室
- **RAG 客服问答**：结合知识库检索与大模型生成，回答预约规则、使用流程等问题

### 部署

- Docker + Docker Compose
- Kubernetes（kind 本地集群）

---

## 项目结构

```text
campus-studyroom-system/
├── backend/                        # 后端微服务
│   ├── campus-auth/                # 认证服务：登录、注册、JWT 签发
│   ├── campus-user/                # 用户服务：个人信息、用户管理
│   ├── campus-room/                # 自习室服务：座位、房间信息
│   ├── campus-reservation/         # 预约服务：预约、取消、历史
│   ├── campus-attendance/          # 考勤服务：签到、签退、异常处理
│   ├── campus-ai/                  # AI 服务：推荐、RAG 问答
│   └── campus-gateway/             # API 网关：路由、鉴权、限流
├── frontend/                       # Vue3 前端
│   ├── src/
│   │   ├── api/                    # 接口请求
│   │   ├── components/             # 公共组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia 状态
│   │   ├── views/                  # 页面视图
│   │   └── styles/                 # 全局样式
│   └── package.json
├── docker/                         # Docker 镜像配置（nginx 等）
├── k8s/                            # Kubernetes 部署清单
├── test/                           # 测试数据与 Postman 集合
│   ├── sql/mysql/                  # MySQL 初始化脚本
│   └── sql/dameng/                 # 达梦 8 兼容脚本
├── docs/                           # 项目文档
│   ├── 01-需求规格说明书.md
│   ├── 02-架构设计文档.md
│   ├── 03-数据库设计文档.md
│   ├── 04-期末综合设计报告.md
│   ├── 答辩PPT.pptx
│   └── ...
├── docker-compose.yml              # 一键启动 11 容器
├── pom.xml                         # Maven 父工程
└── README.md                       # 本文件
```

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0
- Redis 7.0
- Nacos 2.3+

### 1. 启动基础设施

使用 Docker Compose 一键启动 MySQL、Redis、Nacos：

```bash
docker-compose up -d mysql redis nacos
```

### 2. 初始化数据库

```bash
# 方式一：执行 MySQL 初始化脚本
mysql -u root -p < test/sql/mysql/campus_studyroom.sql

# 方式二：Windows 下双击运行
init-db.bat
```

### 3. 启动后端

```bash
# 编译所有微服务
mvn clean install -DskipTests

# 依次启动（或使用脚本）
cd backend/campus-auth && mvn spring-boot:run
cd backend/campus-gateway && mvn spring-boot:run
# ... 其他服务同理
```

或使用提供的 PowerShell 脚本：

```powershell
.\start-backend.bat
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 http://localhost:5173，网关地址为 http://localhost:8000。

---

## 全容器部署

```bash
# 构建并启动全部服务（11 个容器）
docker-compose up -d

# 查看运行状态
docker-compose ps

# 查看日志
docker-compose logs -f campus-auth
```

部署完成后访问：

- 前端：http://localhost
- 网关：http://localhost:8000
- Nacos 控制台：http://localhost:8758/nacos

---

## 主要功能

| 模块 | 功能 |
|------|------|
| 用户认证 | 注册、登录、JWT Token 认证、角色权限控制 |
| 自习室管理 | 自习室/座位信息查询、实时状态展示 |
| 预约管理 | 座位预约、取消预约、预约历史、超时释放 |
| 考勤管理 | 扫码/按钮签到、签退、异常考勤记录 |
| AI 推荐 | 基于协同过滤的座位推荐 |
| AI 客服 | 基于 RAG 的预约规则问答 |
| 管理后台 | 数据统计、用户管理、预约记录查询 |

---

## 文档说明

项目文档统一放在 `docs/` 目录：

- `01-需求规格说明书.md` — 业务需求、用例、非功能需求
- `02-架构设计文档.md` — 4+1 架构视图、DDD 领域划分、设计模式
- `03-数据库设计文档.md` — ER 图、表结构、索引设计、达梦 8 适配
- `04-期末综合设计报告.md` — 完整综合报告
- `答辩PPT.pptx` — 答辩演示文稿
- `部署验证报告.md` — Docker / K8s 部署验证记录
- `本地开发启动指南.md` — 更详细的本地启动说明

---

## 注意事项

1. **配置文件**：`application.yml` 中的数据库密码、JWT Secret 等为本地开发示例值，生产环境请通过环境变量覆盖。
2. **AI 服务**：`campus-ai` 服务中的 `ZHIPU_API_KEY` 需通过环境变量注入，默认留空。
3. **node_modules / target**：已加入 `.gitignore`，克隆后需自行安装依赖和编译。

---

## 许可证

本项目仅用于学习交流，遵循 MIT 许可证。
