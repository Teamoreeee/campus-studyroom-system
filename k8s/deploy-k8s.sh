#!/bin/bash
# 校园自习室预约系统 — Kubernetes 一键部署脚本（kind 集群版）
# 前置条件：kind 集群已创建并处于当前 kubectl context

set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
K8S_DIR="$PROJECT_DIR/k8s"
SQL_FILE="$PROJECT_DIR/test/sql/mysql/campus_studyroom.sql"
NAMESPACE="campus-studyroom"
KIND_CLUSTER="campus-k8s"

DOCKER="/c/Program Files/Docker/Docker/resources/bin/docker.exe"
KUBECTL="/c/Program Files/Docker/Docker/resources/bin/kubectl.exe"
KIND="/c/Users/TEAM0RE/AppData/Local/Microsoft/WinGet/Packages/Kubernetes.kind_Microsoft.Winget.Source_8wekyb3d8bbwe/kind.exe"

echo "========================================"
echo "校园自习室预约系统 — K8s 部署脚本"
echo "========================================"

# 1. 检查 kubectl 能否连上集群
echo "[1/8] 检查 Kubernetes 集群连接..."
if ! "$KUBECTL" cluster-info >/dev/null 2>&1; then
    echo "❌ 无法连接到 Kubernetes 集群"
    echo "   请先创建 kind 集群："
    echo "   kind create cluster --config k8s/kind-config.yaml"
    exit 1
fi
echo "✅ Kubernetes 集群可连接"

# 2. 加载镜像到 kind 集群
echo "[2/8] 加载镜像到 kind 集群..."
for svc in gateway auth user room reservation attendance ai frontend; do
    old_img="campus-studyroom-campus-$svc:latest"
    new_img="campus-studyroom/campus-$svc:latest"
    if "$DOCKER" image inspect "$old_img" >/dev/null 2>&1; then
        "$DOCKER" tag "$old_img" "$new_img"
        echo "  加载 $new_img ..."
        "$KIND" load docker-image "$new_img" --name "$KIND_CLUSTER"
    else
        echo "  ❌ 镜像不存在: $old_img，请先用 docker compose build"
        exit 1
    fi
done

# 基础镜像也需要加载（避免 kind 内部拉取失败）
for img in mysql:8.0.36 redis:7.0.15 nacos/nacos-server:v2.3.0; do
    if "$DOCKER" image inspect "$img" >/dev/null 2>&1; then
        echo "  加载 $img ..."
        "$KIND" load docker-image "$img" --name "$KIND_CLUSTER"
    else
        echo "  ⚠️ 基础镜像不存在: $img"
    fi
done
echo "✅ 镜像加载完成"

# 3. 创建命名空间
echo "[3/8] 创建命名空间..."
"$KUBECTL" apply -f "$K8S_DIR/01-namespace.yaml"

# 4. 创建 ConfigMap（环境变量）
echo "[4/8] 创建应用配置 ConfigMap..."
"$KUBECTL" apply -f "$K8S_DIR/02-configmap.yaml"

# 5. 创建 MySQL 初始化 ConfigMap（从 SQL 文件创建）
echo "[5/8] 创建 MySQL 初始化 ConfigMap..."
if [ ! -f "$SQL_FILE" ]; then
    echo "❌ SQL 文件不存在: $SQL_FILE"
    exit 1
fi
"$KUBECTL" create configmap campus-mysql-init \
    --from-file=init.sql="$SQL_FILE" \
    -n "$NAMESPACE" \
    --dry-run=client -o yaml | "$KUBECTL" apply -f -
echo "✅ MySQL 初始化 ConfigMap 创建完成"

# 6. 创建 ZHIPU_API_KEY Secret
echo "[6/8] 创建 AI 服务 Secret..."
if [ -z "$ZHIPU_API_KEY" ]; then
    echo "⚠️ 环境变量 ZHIPU_API_KEY 未设置，AI 服务将使用本地兜底策略"
    ZHIPU_API_KEY=""
fi
"$KUBECTL" create secret generic zhipu-api-secret \
    --from-literal=ZHIPU_API_KEY="$ZHIPU_API_KEY" \
    -n "$NAMESPACE" \
    --dry-run=client -o yaml | "$KUBECTL" apply -f -
echo "✅ Secret 创建完成"

# 7. 部署基础设施
echo "[7/8] 部署基础设施（MySQL / Redis / Nacos）..."
"$KUBECTL" apply -f "$K8S_DIR/14-mysql.yaml"
"$KUBECTL" apply -f "$K8S_DIR/15-redis.yaml"
"$KUBECTL" apply -f "$K8S_DIR/16-nacos.yaml"

echo "   等待 MySQL 就绪（约 60-90 秒）..."
"$KUBECTL" wait --for=condition=ready pod -l app=campus-mysql -n "$NAMESPACE" --timeout=240s

echo "   等待 Redis 就绪..."
"$KUBECTL" wait --for=condition=ready pod -l app=campus-redis -n "$NAMESPACE" --timeout=120s

echo "   等待 Nacos 就绪（约 60-90 秒）..."
"$KUBECTL" wait --for=condition=ready pod -l app=campus-nacos -n "$NAMESPACE" --timeout=240s

echo "✅ 基础设施部署完成"

# 8. 部署业务服务
echo "[8/8] 部署业务服务与前端..."
for f in 03-gateway.yaml 04-auth.yaml 05-user.yaml 06-room.yaml 07-reservation.yaml 08-attendance.yaml 09-ai.yaml 10-frontend.yaml; do
    echo "  应用 $f"
    "$KUBECTL" apply -f "$K8S_DIR/$f"
done

echo ""
echo "========================================"
echo "🎉 所有资源已下发到 Kubernetes"
echo "========================================"
echo ""
echo "等待所有 Pod 启动（约 2-3 分钟）..."
echo ""
"$KUBECTL" wait --for=condition=ready pod -l app=campus-gateway -n "$NAMESPACE" --timeout=300s
"$KUBECTL" wait --for=condition=ready pod -l app=campus-auth -n "$NAMESPACE" --timeout=300s
"$KUBECTL" wait --for=condition=ready pod -l app=campus-frontend -n "$NAMESPACE" --timeout=300s

echo ""
echo "查看 Pod 状态："
"$KUBECTL" get pods -n "$NAMESPACE"
echo ""
echo "查看 Service 与 NodePort："
"$KUBECTL" get svc -n "$NAMESPACE"
echo ""
echo "访问方式："
echo "  前端：http://localhost:30081"
echo "  网关：http://localhost:30080"
echo ""
echo "验证命令："
echo "  ./k8s/verify-k8s.sh"
