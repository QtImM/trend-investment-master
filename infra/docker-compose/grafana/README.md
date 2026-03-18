# Grafana 本地监控样板

## 目录作用

这个目录提供当前迁移完成态下的最小 `Grafana` 运行样板，用于直接展示主链路服务的基础存活状态。

它与同级的 `prometheus` 目录配合使用：

- `Prometheus` 负责抓取指标
- `Grafana` 负责把抓取结果变成可视化面板

## 当前启动前提

建议按以下顺序启动：

1. `python .tools\local_stack.py up`
2. `python .tools\local_stack.py verify`
3. 在 `infra/docker-compose/prometheus` 下执行 `docker compose up -d`
4. 再在本目录下执行 `docker compose up -d`

这样可以避免打开 Grafana 后只看到空面板或全部红灯。

## 启动命令

在本目录下执行：

```bat
docker compose up -d
```

停止命令：

```bat
docker compose down
```

## 当前默认入口

- Grafana：`http://127.0.0.1:3000`
- 默认账号：`admin`
- 默认密码：`admin`

## 当前预置内容

当前目录已经预置以下资产：

- 数据源：`Trend Prometheus`
  - 指向 `http://host.docker.internal:9090`
- Dashboard Provider：
  - 自动加载 `provisioning/dashboards/json` 下的面板 JSON
- 默认面板：`Trend Services Overview`

## 当前总览面板包含什么

`Trend Services Overview` 当前包含三张基础状态卡片：

- `Gateway Service`
- `Backtest Service`
- `Market Data Service`

它们本质上都基于 `up{job="..."}` 查询，用来快速确认核心服务是否还在被 Prometheus 成功抓取。

## 推荐使用方式

当前最推荐的使用顺序是：

1. 先用 `python .tools\local_stack.py verify` 确认主链路本身是通的
2. 再打开 Grafana 看监控总览
3. 如果某张卡片显示 `Down`
   - 先去 Prometheus `targets` 页面看抓取是否失败
   - 再检查对应服务是否仍在本机运行

## 当前边界说明

当前 Grafana 样板仍然是“迁移完成态下的最小可用面板”，不是完整生产监控体系：

- 当前只展示服务基础存活状态
- 还没有补更丰富的 JVM、HTTP、业务指标图表
- 也还没有引入统一告警和日志联动

但对于当前仓库的目标来说，它已经足够承担：

- 统一监控入口
- 当前主链路服务是否存活的可视化确认
- 后续继续扩展 dashboard 的基础底座
