# K8s 部署截图指南

> 本系统已通过 kind 集群在 K8s 上跑通。请按以下步骤截取答辩用图，保存到 `docs/screenshots/k8s/`。

---

## 已保存的验证输出

以下文本输出已自动保存，可直接作为截图参考：

| 文件 | 内容 |
|------|------|
| `docs/screenshots/k8s/k8s-pods.txt` | `kubectl get pods` 结果，11 个 Pod 全 Running |
| `docs/screenshots/k8s/k8s-services.txt` | `kubectl get svc` 结果，含 NodePort 30080/30081 |
| `docs/screenshots/k8s/k8s-gateway-health.json` | 网关健康检查 `{"status":"UP"}` |
| `docs/screenshots/k8s/k8s-login-response.json` | 网关登录返回 200 + JWT Token |
| `docs/screenshots/k8s/k8s-frontend-status.txt` | 前端 HTTP 200 状态码 |

---

## 建议截取的画面

### 1. 终端：`kubectl get pods -n campus-studyroom`

**操作**：
```bash
kubectl get pods -n campus-studyroom
```

**预期画面**：11 个 Pod 全部 `1/1 Running`

**保存C

---

### 2. 终端：`kubectl get svc -n campus-studyroom`

**操作**：
```bash
kubectl get svc -n campus-studyroom
```

**预期画面**：Service 列表，重点显示 `campus-gateway` 的 `30080` 和 `campus-frontend` 的 `30081`

**保存命名**：`02-k8s-services.png`

---

### 3. 浏览器：Nacos 控制台服务列表

**操作**：
1. 打开浏览器访问 http://localhost:30080/nacos
2. 登录账号/密码：`nacos` / `nacos`
3. 左侧菜单：服务管理 → 服务列表

**预期画面**：显示 7 个微服务：campus-gateway、campus-auth、campus-user、campus-room、campus-reservation、campus-attendance、campus-ai

**保存命名**：`03-k8s-nacos-services.png`

---

### 4. 浏览器：前端登录页面

**操作**：
1. 打开 http://localhost:30081
2. 显示登录页

**预期画面**：校园自习室预约系统登录页

**保存命名**：`04-k8s-frontend-login.png`

---

### 5. 终端/Postman：网关登录返回 Token

**操作**：
```bash
curl -X POST http://localhost:30080/api/auth/login -H "Content-Type: application/json" -d '{"username":"student1","password":"123456"}'
```

**预期画面**：返回 JSON，包含 `code:200` 和 `accessToken`

**保存命名**：`05-k8s-login-token.png`

---

### 6. 浏览器：前端登录后主界面

**操作**：
1. 在前端登录页输入账号 `student1`，密码 `123456`
2. 点击登录
3. 截取主界面

**预期画面**：登录后的系统主界面（自习室列表或仪表盘）

**保存命名**：`06-k8s-frontend-main.png`

---

### 7.（可选）kind 集群信息

**操作**：
```bash
kind get clusters
kubectl cluster-info
kubectl get nodes
```

**预期画面**：显示 `campus-k8s` 集群和 Ready 的节点

**保存命名**：`07-k8s-cluster-info.png`

---

## 截图后做什么

1. 把截图文件放到 `docs/screenshots/k8s/` 目录
2. 在 `docs/部署验证报告.md` 的「A.7 K8s 截图清单」中，把对应的截图文件名填入
3. 在 PPT 和视频脚本中引用这些截图

---

*当前 K8s 集群访问地址：*
- *前端：http://localhost:30081*
- *网关：http://localhost:30080*
- *Nacos：http://localhost:30080/nacos*
