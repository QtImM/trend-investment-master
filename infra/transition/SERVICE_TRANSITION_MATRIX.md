# 服务迁移矩阵

## 目的

这份矩阵用于说明每个旧模块未来会如何迁移、保留、合并或退场。

## 当前迁移矩阵

| 当前模块 | 类型 | 当前状态 | 目标状态 | 处理方式 |
|---|---|---|---|---|
| `eureka-server` | 基础设施 | 保留 | 删除 | 待 Nacos Discovery 接管后退场 |
| `index-config-server` | 基础设施 | 保留 | 删除 | 待 Nacos Config 接管后退场 |
| `index-zuul-service` | 基础设施 | 保留 | 删除 | 待 `gateway-service` 验证完成后退场 |
| `index-hystrix-dashboard` | 基础设施 | 保留 | 删除 | 待 Prometheus/Grafana 接管后退场 |
| `index-turbine` | 基础设施 | 保留 | 删除 | 待 Prometheus/Grafana 接管后退场 |
| `gateway-service` | 基础设施 | 新增 | 保留 | 新网关试点模块 |
| `third-part-index-data-project` | 业务 | 保留 | 待定 | 未来可能转为 fixture / mock-provider |
| `index-gather-store-service` | 业务 | 保留 | 合并 | 未来并入 `market-data-service` |
| `index-codes-service` | 业务 | 保留 | 合并 | 未来并入 `market-data-service` |
| `index-data-service` | 业务 | 保留 | 合并 | 未来并入 `market-data-service` |
| `trend-trading-backtest-service` | 业务 | 保留 | 保留 | 作为核心服务持续演进 |
| `trend-trading-backtest-view` | 前端/服务端混合 | 保留 | 删除 | 最终由 `trend-web` 替代 |

## 当前阶段结论

当前不是大规模删模块的阶段，而是：

- 新模块开始落库
- 新旧模块并行存在
- 等新链路验证稳定后，再逐步让旧模块退场
