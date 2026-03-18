# Nacos Config 配置模板说明

## 目录目的

这个目录现在不再只是“迁移准备区”，而是当前主线已经投入使用的 `Nacos Config` 配置资产目录。

它承担三件事：

1. 保存当前主链路正在使用的核心 `Data ID` 模板
2. 作为本地 `Nacos` 配置同步的仓库内基线
3. 为后续维护者提供统一的配置查看与更新入口

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

## 当前使用方式

当前仓库主线已经默认收口到 `Nacos Config`，核心服务会直接从本地 `Nacos` 读取配置。

当前补充约定如下：

- 服务运行配置优先放到 `templates/*.yaml` 中，作为未来导入 Nacos 的 Data ID 内容
- `server-addr`、`file-extension` 这类连接 Nacos 本身的引导配置，不放在模板内，而是放在服务自己的 `bootstrap-*.yml`
- 这样可以把“如何连到 Nacos”和“从 Nacos 里读取什么配置”拆开，当前维护和后续扩展都更清晰

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

当前主链路验收脚本也会直接检查这四个 `Data ID` 是否存在：

```bat
python .tools\verify_local_migration.py
```

因此这四个配置文件现在已经不只是“模板留档”，而是当前本地完成态的一部分。

## 当前推荐维护顺序

1. 先修改 `templates/*.yaml`
2. 再执行 `python .tools\nacos_config_sync.py sync-core`
3. 如需复核，再执行 `python .tools\verify_local_migration.py`

补充说明：

- `index-codes-service-dev.yaml` 与 `index-data-service-dev.yaml` 当前主要保留为迁移过程留档
- 当前默认市场数据主线已经收敛到 `templates/market-data-service-dev.yaml`

## 当前状态结论

按当前主线口径，这个目录对应的迁移动作已经完成：

- `index-config-server` 已退场
- `Nacos Config` 已成为默认配置中心
- 核心 `Data ID` 已可通过仓库脚本同步和验收

后续如果还会继续改这里，性质已经不是“继续迁移”，而是日常维护和配置演进。
