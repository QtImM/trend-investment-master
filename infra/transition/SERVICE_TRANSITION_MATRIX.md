# 服务迁移矩阵

## 目的

这份矩阵用于说明每个旧模块未来会如何迁移、保留、合并或退场。

## 当前迁移矩阵

| 当前模块 | 类型 | 当前状态 | 目标状态 | 处理方式 |
|---|---|---|---|---|
| `eureka-server` | 基础设施 | 已停止纳入主构建 | 删除 | Nacos Discovery 试点已覆盖关键链路，先退出主构建 |
| `index-config-server` | 基础设施 | 已停止纳入主构建 | 删除 | Nacos Config 试点已覆盖关键链路，先退出主构建 |
| `index-zuul-service` | 基础设施 | 已退场 | 删除 | 已由 `gateway-service` 接管且源码目录已删除 |
| `index-hystrix-dashboard` | 基础设施 | 已退场 | 删除 | 已从主构建移除且源码目录已删除 |
| `index-turbine` | 基础设施 | 已退场 | 删除 | 已从主构建移除且源码目录已删除 |
| `gateway-service` | 基础设施 | 新增 | 保留 | 新网关试点模块 |
| `third-part-index-data-project` | 业务 | 保留 | 待定 | 未来可能转为 fixture / mock-provider |
| `index-gather-store-service` | 业务 | 保留 | 合并 | 未来并入 `market-data-service` |
| `index-codes-service` | 业务 | 保留 | 合并 | 未来并入 `market-data-service` |
| `index-data-service` | 业务 | 保留 | 合并 | 未来并入 `market-data-service` |
| `trend-trading-backtest-service` | 业务 | 保留 | 保留 | 作为核心服务持续演进 |
| `trend-trading-backtest-view` | 前端/服务端混合 | 保留 | 删除 | 最终由 `trend-web` 替代 |

## 当前阶段结论

当前阶段已经进入“旧监控模块退场、旧注册/配置/网关模块持续退场”的状态：

- 新监控替代样板已入库
- `index-hystrix-dashboard` 与 `index-turbine` 已从主构建移除且源码目录已删除
- `eureka-server` 与 `index-config-server` 已停止纳入主构建
- `index-zuul-service` 已从主构建移除且源码目录已删除
- 其余旧模块仍按“并行迁移、逐步退场”继续推进
