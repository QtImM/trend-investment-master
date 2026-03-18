# gateway-service

## 作用

这是重构过程中新增的网关试点模块，用于逐步替代原有的 `index-zuul-service`。

当前阶段的目标不是立即删除 Zuul，而是先把新网关模块落到仓库中，完成以下准备：

- 建立新的网关模块结构
- 平移原有的核心路由规则
- 为后续从 `Zuul -> Spring Cloud Gateway` 的正式切换做试点

## 当前路由

- `/trend-web/**` -> `trend-trading-backtest-view`
- `/api-market/**` -> `market-data-service`
- `/api-backtest/**` -> `trend-trading-backtest-service`
- `/api-view/**` -> `trend-trading-backtest-view`

## 当前状态

当前 `gateway-service` 还属于迁移试点模块：

- 旧的 `index-zuul-service` 仍然保留
- 新模块先与旧模块并存
- 端口使用 `8032`，避免和现有 Zuul 的 `8031` 冲突
- 当前 `trend-web` 页面入口已经优先通过 `trend-trading-backtest-view` 承接，不再依赖单独的 Vite 开发服务器

## 后续计划

1. 继续完善网关层配置
2. 后续切换服务注册中心时，把 `Eureka` 依赖迁到 `Nacos`
3. 验证通过后，再正式移除 `index-zuul-service`
