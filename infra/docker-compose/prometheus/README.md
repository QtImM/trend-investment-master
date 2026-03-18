# Prometheus 本地监控样板

## 目录作用

这个目录提供当前迁移完成态下的最小 `Prometheus` 运行样板，用于抓取本地主链路的基础指标。

当前目的不是替代主链路验收脚本，而是补齐一条独立的“监控观察入口”：

- `local_stack.py verify` 负责验收
- `Prometheus` 负责看指标是否被稳定暴露

## 当前启动前提

在启动 Prometheus 之前，建议先保证本地主链路已经正常运行：

```bat
python .tools\local_stack.py up
python .tools\local_stack.py verify
```

当前默认抓取目标都指向宿主机的本地服务端口，因此本机四个核心服务至少应已启动。

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

- Prometheus UI：`http://127.0.0.1:9090`
- 热加载配置接口：`http://127.0.0.1:9090/-/reload`

## 当前默认抓取目标

当前 `prometheus.yml` 已预置三组抓取配置：

- `gateway-service`
  - `http://host.docker.internal:8032/actuator/prometheus`
- `trend-trading-backtest-service`
  - `http://host.docker.internal:8051/actuator/prometheus`
- `market-data-service`
  - `http://host.docker.internal:8061/actuator/prometheus`

这三组目标对应当前主链路里最关键的入口层、回测层和市场数据层。

## 推荐检查方式

启动后，可以按这个顺序检查：

1. 打开 `http://127.0.0.1:9090/targets`
2. 确认三个 target 都是 `UP`
3. 在查询框里执行：
   - `up`
   - `jvm_memory_used_bytes`
   - `http_server_requests_seconds_count`

如果 `targets` 页面里出现 `DOWN`，优先先检查对应服务本身是否仍在运行，而不是先改 Prometheus 配置。

## 当前边界说明

当前样板是为了支撑“迁移完成态”的最小统一监控入口，因此它有意保持轻量：

- 只抓当前主链路最关键的三个服务
- 主要用于确认 `/actuator/prometheus` 是否被稳定暴露
- 不负责替代更完整的日志、追踪或报警体系

如果后续要继续推进长期治理，可以在这个目录基础上继续扩展：

- 更多服务抓取目标
- 录制规则
- 告警规则
- 更细粒度的业务指标
