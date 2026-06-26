# Docker Compose 部署说明

## 前置条件

1. Docker Desktop 已安装并正常运行
2. WSL2 后端已启用
3. 已配置国内镜像加速器（否则拉取 Docker Hub 镜像会超时）
4. 项目已编译生成 jar 包（`backend/campus-*/target/*.jar`）
5. 前端 dist 已构建（`frontend/dist/`）
6. 智谱 API Key 已设置为环境变量 `ZHIPU_API_KEY`（可选，未设置时 AI 服务降级为本地兜底）

## 快速部署

在项目根目录下执行：

```bash
cd "C:/Users/TEAM0RE/OneDrive/桌面/软件架构技术/campus-studyroom"
mvn clean package -DskipTests -Dfile.encoding=UTF-8
docker compose up -d --build
```

部署完成后，11 个容器将全部启动：
- 3 个基础容器：MySQL、Redis、Nacos
- 7 个微服务容器：gateway、auth、user、room、reservation、attendance、ai
- 1 个前端容器：nginx

## 访问方式

| 入口 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost | nginx 反向代理到网关 |
| API 网关 | http://localhost:8000 | 统一入口，JWT 校验 |
| Nacos 控制台 | http://localhost:8758/nacos | 账号/密码 nacos/nacos |
| MySQL | localhost:3307 | 宿主端口 3307 → 容器 3306 |
| Redis | localhost:6380 | 宿主端口 6380 → 容器 6379 |

## 端口映射说明

为避免与本地已运行的 MySQL/Redis/Nacos 冲突，compose 中部分端口做了调整：

| 服务 | 容器端口 | 宿主端口 | 调整原因 |
|------|----------|----------|----------|
| MySQL | 3306 | 3307 | 避开本地 MySQL 3306 |
| Redis | 6379 | 6380 | 避开本地 Redis 6379 |
| Nacos HTTP | 8848 | 8758 | 避开 Windows 保留端口段 8835–8934 |
| Nacos gRPC | 9848 | 9848 | 无需调整 |
| Gateway | 8000 | 8000 | 无冲突 |
| AI | 8006 | 8006 | 无冲突 |
| 前端 | 80 | 80 | 无冲突 |

容器间通信仍使用标准内部端口（如 `campus-mysql:3306`），不受宿主端口影响。

## 验证部署

### 1. 查看容器状态

```bash
docker compose ps
```

预期输出：11 个容器全部 `Up`，基础设施为 `healthy`。

### 2. 检查网关健康

```bash
curl http://localhost:8000/actuator/health
```

预期输出：
```json
{"status":"UP"}
```

### 3. 测试登录链路

```bash
# Windows CMD
curl -X POST http://localhost:8000/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"student1\",\"password\":\"123456\"}"

# PowerShell
curl -X POST http://localhost:8000/api/auth/login -H "Content-Type: application/json" -d '{"username":"student1","password":"123456"}'
```

预期输出：返回 `code:200` 和 `accessToken`。

### 4. 访问前端

浏览器打开 http://localhost，应显示登录页。

账号/密码：`student1` / `123456`

## 常用命令

```bash
# 查看日志
docker compose logs -f campus-gateway
docker compose logs -f campus-auth

# 重启单个服务
docker compose restart campus-gateway

# 停止整个栈
docker compose down

# 停止并删除数据卷（谨慎）
docker compose down -v

# 重新构建并启动
docker compose up -d --build
```

## 常见问题

| 问题 | 根因 | 解决方案 |
|------|------|----------|
| 镜像拉取超时 | Docker Hub 国内访问受限 | 配置国内镜像加速器，见下文 |
| Nacos 端口绑定失败 | 8848 落在 Windows 保留端口段 | compose 中已改为 8758 |
| 服务启动崩溃 | `spring.config.import: nacos:` 触发配置中心 | 已关闭 Nacos 配置中心，仅保留服务发现 |
| Redis 连接失败 | Spring Boot 3.2 配置项为 `spring.data.redis` | 已修正为 `spring.data.redis.host` |
| 网关路由 503 | 缺少 `spring-cloud-loadbalancer` | 网关 pom 已增加 loadbalancer 依赖 |
| 本地 jar 被占用 | 本地 java 服务还在跑 | 先停止本地 java 服务再 `docker compose up` |

## 配置国内镜像加速器

如果拉取 `mysql:8.0.36`、`redis:7.0.15`、`nacos/nacos-server:v2.3.0` 超时，请在 Docker Desktop 设置中配置镜像加速器：

1. 打开 Docker Desktop → Settings
2. 选择 Docker Engine
3. 在 JSON 配置中添加：
   ```json
   {
     "registry-mirrors": [
       "https://docker.m.daocloud.io",
       "https://docker.1ms.run",
       "https://docker.1panel.live",
       "https://dockerproxy.net",
       "https://hub.rat.dev",
       "https://docker.mirrors.ustc.edu.cn"
     ]
   }
   ```
4. 点击 Apply & Restart

## 清理部署

```bash
# 停止并删除容器
docker compose down

# 同时删除 MySQL 数据卷
docker compose down -v
```

---

*本说明配合项目根目录 `docker-compose.yml` 使用。*
