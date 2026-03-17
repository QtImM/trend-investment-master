# Nacos 本地运行说明

## 作用

这个目录用于承接重构后的基础设施运行方案，当前第一步先落地 `Nacos`，用于替代原来的：

- `Eureka`
- `Config Server`

## 文件说明

- `docker-compose.yml`：本地单机版 Nacos 启动文件

## 端口说明

- `8848`：Nacos 控制台与 HTTP 接口
- `9848`：Nacos 2.x gRPC 端口

## 启动方式

在当前目录执行：

```bash
docker compose up -d
```

或者：

```bash
docker-compose up -d
```

启动后访问：

```text
http://localhost:8848/nacos
```

## 当前状态

本仓库已经把 Nacos 的基础设施定义写入版本库，但当前开发机尚未安装 Docker，因此这一步暂时只完成了：

1. 基础设施配置入库
2. 目录结构建立
3. 后续服务迁移所需的本地运行入口准备

真正启动容器需要先在本机安装 Docker Desktop 或其他兼容 Docker Compose 的运行环境。

## 后续迁移计划

接下来会围绕这个 Nacos 运行入口继续推进：

1. 让业务服务从 `Eureka` 迁移到 `Nacos Discovery`
2. 让配置从 `Config Server` 迁移到 `Nacos Config`
3. 再开始替换 `Zuul` 为 `Spring Cloud Gateway`
