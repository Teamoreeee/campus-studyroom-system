# Kubernetes 部署说明

## 前置条件

1. Docker Desktop 已安装并正常运行
2. 安装 kind CLI：
   ```bash
   winget install --id Kubernetes.kind
   ```

## 快速部署

本项目使用 **kind 自建集群** 而非 Docker Desktop 内置 K8s，原因：Docker Desktop 内置 kind 集群在国内网络环境下拉取 Docker Hub 镜像会失败（registry mirror 500 错误）。自建 kind 集群可通过配置国内镜像加速器解决该问题。

### 1. 创建 kind 集群

```bash
cd "C:/Users/TEAM0RE/OneDrive/桌面/软件架构技术/campus-studyroom"
kind create cluster --config k8s/kind-config.yaml
```

`k8s/kind-config.yaml` 中已配置：
- 国内镜像加速器（daocloud、1ms、1panel、ustc、dockerproxy、hub.rat.dev）
- registry.k8s.io 转发到阿里云镜像
- NodePort 30080/30081 映射到宿主机

### 2. 一键部署系统

```bash
./k8s/deploy-k8s.sh
```

脚本会自动完成：
1. 检查 kubectl 能否连接 kind 集群
2. 将 docker-compose 生成的镜像 `docker tag` 成 K8s YAML 期望的格式
3. 使用 `kind load docker-image` 把所有镜像预加载到 kind 节点
4. 创建命名空间、ConfigMap、MySQL 初始化 ConfigMap、ZHIPU_API_KEY Secret
5. 部署 MySQL、Redis、Nacos 并等待就绪
6. 部署 7 个微服务 + 前端
7. 等待核心 Pod 就绪

### 3. 验证部署

```bash
./k8s/verify-k8s.sh
```

## 访问方式

| 入口 | 地址 |
|------|------|
| 前端 | http://localhost:30081 |
| API 网关 | http://localhost:30080 |
| Nacos 控制台 | http://localhost:30080/nacos（账号/密码 nacos/nacos） |

## 常用调试命令

```bash
# 查看 Pod
kubectl get pods -n campus-studyroom

# 查看 Service
kubectl get svc -n campus-studyroom

# 查看日志
kubectl logs -n campus-studyroom deployment/campus-gateway --tail=50

# 进入 Pod
kubectl exec -it -n campus-studyroom deployment/campus-auth -- bash
```

## 清理

```bash
# 删除 kind 集群
kind delete cluster --name campus-k8s

# 或仅删除命名空间
kubectl delete namespace campus-studyroom
```

## 已知问题

| 问题 | 根因 | 解决方案 |
|------|------|----------|
| Docker Desktop 自带 kind 拉镜像失败 | 内部 registry mirror 对 Docker Hub 返回 500 | 使用 kind CLI 自建集群并配置国内镜像加速器 |
| kind 节点没有本地镜像 | kind 使用独立 containerd 镜像存储 | `kind load docker-image` 预加载 |
| YAML 镜像名与 docker-compose 不一致 | compose 默认命名带连字符 | 脚本中先 `docker tag` 再加载 |

---

*本说明配合 `k8s/kind-config.yaml`、`k8s/deploy-k8s.sh` 和 `k8s/verify-k8s.sh` 使用。*
