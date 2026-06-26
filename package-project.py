#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""打包 campus-studyroom 项目为提交用 zip，自动排除 node_modules/target/logs 等。"""
import os
import shutil
import sys
from pathlib import Path

PROJECT_ROOT = Path(r"C:\Users\TEAM0RE\OneDrive\桌面\软件架构技术\campus-studyroom")
STAGING = Path(r"C:\temp\campus-submit-staging\CampusStudio_校园自习室预约系统_提交材料")
OUT_ZIP = Path(r"C:\temp\CampusStudio_校园自习室预约系统_提交材料")

IGNORE_PATTERNS = shutil.ignore_patterns(
    # 依赖/构建产物
    "node_modules",
    "target",
    "dist",
    # 日志/临时
    "logs",
    "*.log",
    "*.pid",
    "*.tmp",
    "nohup.out",
    # 仓库元数据
    ".git",
    ".claude",
    # 会话/测试遗留文件
    "ai_chat_*.json",
    "ai_rec_*.json",
    "ai_recommend_*.json",
    "2026-06-*.txt",
    "ontinuous.txt",
    # 开发工具配置（如有）
    ".idea",
    ".vscode",
)

README_CONTENT = """# 校园自习室预约系统 —— 提交材料说明

> 团队：CampusStudio
> 技术栈：Spring Cloud Alibaba + Vue3 + MySQL/达梦 + Redis + Docker/Kubernetes

---

## 一、材料清单

| 目录 | 内容 |
|------|------|
| `01-源码/backend/` | 7 个微服务 + API 网关源码 |
| `01-源码/frontend/` | Vue3 + TypeScript 前端源码 |
| `02-文档/` | 需求、架构、数据库、期末报告、PPT、评分报告等 |
| `03-部署脚本/` | docker-compose.yml、K8s YAML、一键部署脚本 |
| `04-测试数据/` | MySQL / 达梦初始化 SQL、Postman 测试集合 |
| `05-演示材料/` | 系统截图（docker/ + k8s/） |
| `06-其他/` | 选题申请表、评分标准 |

---

## 二、三种使用方式

### 方式 1：本地开发运行（推荐先尝试）

前置要求：
- JDK 17
- MySQL 8.0（端口 3306，root/123456）
- Redis（端口 6379，无密码）
- Node.js 18+

步骤：

```powershell
# 1. 初始化数据库
cd campus-studyroom
init-db.bat

# 2. 启动后端（会依次启动 6 个微服务 + 网关）
start-backend.bat

# 3. 启动前端
cd frontend
npm install
npm run dev
```

访问：
- 前端：`http://localhost:3001`
- 网关 Swagger：`http://localhost:8000/webjars/swagger-ui/index.html?url=/v3/api-docs`
- 账号：`student1` / `123456`（普通用户），`admin` / `123456`（管理员）

### 方式 2：Docker Compose 一键部署

前置要求：Docker Desktop 已安装并运行。

```bash
# 1. 编译所有 jar
mvn clean package -DskipTests

# 2. 构建镜像并启动 11 个容器
docker compose up -d --build

# 3. 查看状态
docker compose ps
```

访问：
- 前端：`http://localhost`
- 网关：`http://localhost:8000`
- Nacos：`http://localhost:8758/nacos`（nacos/nacos）

### 方式 3：Kubernetes（kind）部署

前置要求：Docker Desktop + kind CLI。

```bash
# 1. 创建 kind 集群
kind create cluster --config k8s/kind-config.yaml

# 2. 一键部署
./k8s/deploy-k8s.sh

# 3. 验证
./k8s/verify-k8s.sh
```

访问：
- 前端：`http://localhost:30081`
- 网关：`http://localhost:30080`

---

## 三、常见问题

| 问题 | 解决 |
|------|------|
| 前端 401 | 确认后端服务已启动，且前端 `.env` 中 API 地址正确 |
| 数据库表不存在 | 运行 `init-db.bat` 或执行 `test/sql/mysql/campus_studyroom.sql` |
| Docker 拉镜像慢 | 在 Docker Desktop `daemon.json` 中配置国内镜像加速器 |
| K8s Pod 镜像拉取失败 | 确认已执行 `./k8s/deploy-k8s.sh`（脚本会用 `kind load` 预加载镜像） |

---

## 四、项目亮点

- 7 个微服务完整落地：auth / user / room / reservation / attendance / ai / gateway
- JWT + RBAC 权限控制
- AI 协同过滤推荐 + RAG 智能客服（智谱 GLM-4）
- Docker Compose 11 容器一键部署
- Kubernetes（kind）云原生部署验证
- MySQL + 达梦 8 双库适配

---

*更多细节见 `02-文档/` 目录。*
"""


def main():
    if STAGING.exists():
        shutil.rmtree(STAGING)
    STAGING.parent.mkdir(parents=True, exist_ok=True)

    print(f"正在复制项目到 {STAGING} ...")
    shutil.copytree(PROJECT_ROOT, STAGING, ignore=IGNORE_PATTERNS)

    # 写入 README
    readme_path = STAGING / "README.md"
    readme_path.write_text(README_CONTENT, encoding="utf-8")

    # 删除顶层不需要的文件
    for name in ["2026-06-24-190000-local-command-caveatcaveat-the-messages-below.txt",
                 "2026-06-25-171425-token-cl.txt",
                 "auth.log",
                 "compile-errors.log",
                 "init-db-error.log",
                 "ontinuous.txt"]:
        p = STAGING / name
        if p.exists():
            p.unlink()

    # 删除顶层测试 JSON
    for p in STAGING.glob("ai_*.json"):
        p.unlink()

    # 删除 target/node_modules 残留（保险）
    for exclude in ["node_modules", "target", "dist", "logs"]:
        for p in STAGING.rglob(exclude):
            if p.is_dir():
                shutil.rmtree(p)

    # 打包
    print(f"正在生成 {OUT_ZIP}.zip ...")
    if OUT_ZIP.with_suffix(".zip").exists():
        OUT_ZIP.with_suffix(".zip").unlink()
    shutil.make_archive(str(OUT_ZIP), "zip", STAGING.parent, STAGING.name)

    size_mb = OUT_ZIP.with_suffix(".zip").stat().st_size / 1024 / 1024
    print(f"打包完成：{OUT_ZIP}.zip")
    print(f"文件大小：{size_mb:.2f} MB")


if __name__ == "__main__":
    main()
