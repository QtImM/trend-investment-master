# Nacos Config 配置模板说明

## 目录目的

这个目录用于承接从 `index-config-server` 迁移到 `Nacos Config` 之后的配置模板。

当前阶段先完成两件事：

1. 把原本分散在各模块 `application.yml` / `bootstrap.yml` 中的配置按服务整理出来
2. 为后续导入到 Nacos 的 `Data ID` 做落地准备

## 推荐命名方式

建议沿用下面的 Data ID 规则：

- `${spring.application.name}-dev.yaml`

当前模板文件位于 `templates/` 目录，例如：

- `templates/index-codes-service-dev.yaml`
- `templates/index-data-service-dev.yaml`
- `templates/market-data-service-dev.yaml`
- `templates/gateway-service-dev.yaml`
- `templates/trend-trading-backtest-service-dev.yaml`
- `templates/trend-trading-backtest-view-dev.yaml`

## 当前处理策略

当前项目还没有真正切换到 Nacos Config，原因是：

- 现有项目仍运行在 `Spring Boot 2.0.3 + Spring Cloud Finchley`
- 直接接入现代版本的 Nacos 有较高兼容风险

因此这一步先做“配置模板入库”，后续待版本底座继续升级后，再正式接入 Nacos Config。

当前补充约定如下：

- 服务运行配置优先放到 `templates/*.yaml` 中，作为未来导入 Nacos 的 Data ID 内容
- `server-addr`、`file-extension` 这类连接 Nacos 本身的引导配置，不放在模板内，而是放在服务自己的 `bootstrap-*.yml`
- 这样可以把“如何连到 Nacos”和“从 Nacos 里读取什么配置”拆开，后续扩展到其他服务时更清晰

## 当前本地同步方式

当前仓库已经提供了一个最小同步脚本：

- `.tools/nacos_config_sync.py`

常用用法如下：

```bat
python .tools\nacos_config_sync.py sync-core
python .tools\nacos_config_sync.py get gateway-service-dev.yaml
python .tools\nacos_config_sync.py put trend-trading-backtest-view-dev.yaml
```

其中 `sync-core` 会将当前主线联调所需的四个核心 `Data ID` 同步到本地 `Nacos`：

- `gateway-service-dev.yaml`
- `market-data-service-dev.yaml`
- `trend-trading-backtest-service-dev.yaml`
- `trend-trading-backtest-view-dev.yaml`

## 迁移顺序建议

1. 先整理配置模板
2. 再升级服务版本底座
3. 最后让服务从 `Config Server` 切换到 `Nacos Config`

补充说明：

- `index-codes-service-dev.yaml` 与 `index-data-service-dev.yaml` 当前主要保留为迁移过程留档
- 当前默认市场数据主线已经收敛到 `templates/market-data-service-dev.yaml`
