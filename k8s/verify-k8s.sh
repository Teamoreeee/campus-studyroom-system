#!/bin/bash
# 校园自习室预约系统 — Kubernetes 部署验证脚本

set -e

NAMESPACE="campus-studyroom"
KUBECTL="/c/Program Files/Docker/Docker/resources/bin/kubectl.exe"

echo "========================================"
echo "K8s 部署验证"
echo "========================================"

echo ""
echo "[1/5] Pod 状态"
"$KUBECTL" get pods -n "$NAMESPACE"

echo ""
echo "[2/5] Service 与 NodePort"
"$KUBECTL" get svc -n "$NAMESPACE"

echo ""
echo "[3/5] 检查所有 Pod 是否 Running"
RUNNING=$("$KUBECTL" get pods -n "$NAMESPACE" --field-selector=status.phase=Running --no-headers | wc -l | tr -d ' ')
TOTAL=$("$KUBECTL" get pods -n "$NAMESPACE" --no-headers | wc -l | tr -d ' ')
echo "  Running: $RUNNING / $TOTAL"
if [ "$RUNNING" -eq "$TOTAL" ] && [ "$TOTAL" -gt 0 ]; then
    echo "  ✅ 所有 Pod 正常运行"
else
    echo "  ❌ 部分 Pod 未正常运行"
fi

echo ""
echo "[4/5] Nacos 服务注册检查"
curl -s --max-time 10 "http://localhost:30080/nacos/v1/ns/catalog/services?pageNo=1&pageSize=20&namespaceId=public" 2>/dev/null | head -c 500 || echo "  Nacos 查询失败，可能服务尚未完全注册"

echo ""
echo "[5/5] 网关登录测试"
LOGIN_RES=$(curl -s --max-time 15 -X POST "http://localhost:30080/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"student1","password":"123456"}' 2>/dev/null || echo "")
if echo "$LOGIN_RES" | grep -q "200"; then
    echo "  ✅ 网关登录成功"
    echo "  响应：$LOGIN_RES"
else
    echo "  ❌ 网关登录失败或未就绪"
    echo "  响应：$LOGIN_RES"
fi

echo ""
echo "========================================"
echo "验证完成"
echo "========================================"
echo ""
echo "常用调试命令："
echo "  查看所有 Pod：kubectl get pods -n $NAMESPACE"
echo "  查看日志：kubectl logs -n $NAMESPACE <pod-name>"
echo "  查看事件：kubectl get events -n $NAMESPACE --sort-by=.lastTimestamp"
echo "  前端访问：http://localhost:30081"
echo "  网关访问：http://localhost:30080"
