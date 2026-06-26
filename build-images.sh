#!/bin/bash

# 构建所有 Docker 镜像
set -e

PROJECT_NAME="campus-studyroom"
SERVICES=("campus-auth" "campus-user" "campus-room" "campus-reservation" "campus-attendance" "campus-ai" "campus-gateway")

echo "=== 开始构建后端服务镜像 ==="
for service in "${SERVICES[@]}"; do
    echo "Building ${service}..."
    docker build -t ${PROJECT_NAME}/${service}:latest -f backend/${service}/Dockerfile .
done

echo "=== 开始构建前端镜像 ==="
docker build -t ${PROJECT_NAME}/campus-frontend:latest -f frontend/Dockerfile .

echo "=== 所有镜像构建完成 ==="
docker images | grep ${PROJECT_NAME}
