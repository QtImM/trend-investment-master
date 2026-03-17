# Nacos Config 配置模板说明

## 目录目的

这个目录用于承接从 `index-config-server` 迁移到 `Nacos Config` 之后的配置模板。

当前阶段先完成两件事：

1. 把原本分散在各模块 `application.yml` / `bootstrap.yml` 中的配置按服务整理出来
2. 为后续导入到 Nacos 的 `Data ID` 做落地准备

## 推荐命名方式

建议沿用下面的 Data ID 规则：

- `${spring.application.name}-dev.yaml`

例如：

- `index-codes-service-dev.yaml`
- `index-data-service-dev.yaml`
- `trend-trading-backtest-service-dev.yaml`
- `trend-trading-backtest-view-dev.yaml`

## 当前处理策略

当前项目还没有真正切换到 Nacos Config，原因是：

- 现有项目仍运行在 `Spring Boot 2.0.3 + Spring Cloud Finchley`
- 直接接入现代版本的 Nacos 有较高兼容风险

因此这一步先做“配置模板入库”，后续待版本底座继续升级后，再正式接入 Nacos Config。

## 迁移顺序建议

1. 先整理配置模板
2. 再升级服务版本底座
3. 最后让服务从 `Config Server` 切换到 `Nacos Config`
