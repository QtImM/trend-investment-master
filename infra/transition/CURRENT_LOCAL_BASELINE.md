# 当前本地完成态基线

## 文档目的

这份文档用于描述当前仓库主线已经收口出的“本地可运行、可验收、可调试”基线，方便后续用于：

- 新同学快速接手
- 本地环境复现
- 面试或简历中说明当前重构完成度
- 后续继续推进测试、监控和长期治理时的统一参照

## 一、当前推荐运行时

当前主线默认基于以下本地运行时：

- Java 17
- Redis `6379`
- Nacos `2.4.3`
  - 路径：`C:\Tools\nacos-2.4.3\nacos`
  - 端口：`8848`

当前推荐通过仓库内统一脚本管理本地链路：

```bat
python .tools\local_stack.py up
python .tools\local_stack.py status
python .tools\local_stack.py verify
python .tools\local_stack.py down
```

## 二、当前核心服务基线

当前主线已收口为以下四个核心服务：

| 服务 | 角色 | 端口 | 当前状态 |
|---|---|---|---|
| `gateway-service` | 统一入口网关 | `8032` | 保留 |
| `market-data-service` | 市场数据读能力 | `8061` | 保留 |
| `trend-trading-backtest-service` | 回测计算核心服务 | `8051` | 保留 |
| `trend-trading-backtest-view` | 页面壳层与静态资源承载 | `8041` | 保留 |

当前前端默认入口为：

- `http://127.0.0.1:8032/trend-web/`

## 三、当前主链路事实

截至当前仓库主线，已经确认以下事实成立：

1. 服务注册中心已从 `Eureka` 收口到本地 `Nacos Discovery`
2. 配置中心已从 `Config Server` 收口到本地 `Nacos Config`
3. 对外统一入口已从 `index-zuul-service` 收口到 `gateway-service`
4. `trend-web` 已成为默认前端入口，不再依赖单独开发服务器
5. 市场数据、回测服务、页面壳层都已接入当前 `nacos` 主链路
6. 本地主链路联调默认不再要求启动 `third-part-index-data-project`
7. 仓库内已具备：
   - 统一启动入口
   - 统一状态检查入口
   - 统一验收入口

## 四、当前验收标准

当前认定“本地迁移链路可用”的统一标准为：

```bat
python .tools\local_stack.py verify
```

脚本通过时，表示以下内容全部成立：

- 本地 `Nacos` 可达
- 四个核心服务都已在 `Nacos` 中注册为健康实例
- 四个核心 `Data ID` 已存在于本地 `Nacos Config`
- 页面入口、市场数据接口、回测服务健康检查、回测模拟接口都返回正常结果

## 五、当前不再作为主链路前置条件的模块

以下模块仍保留在仓库语境中，但当前主链路默认不要求它们参与启动：

| 模块 | 当前角色 |
|---|---|
| `third-part-index-data-project` | 历史 fixture / mock-provider，可选 |
| `eureka-server` | 已退场 |
| `index-config-server` | 已退场 |
| `index-zuul-service` | 已退场 |
| `index-hystrix-dashboard` | 已退场 |
| `index-turbine` | 已退场 |

## 六、当前完成度判断

按“能否支撑本地联调和后续开发”这个标准，当前仓库已经达到迁移完成态的主目标：

- 可以启动
- 可以访问
- 可以验收
- 可以调试
- 可以交接

当前剩余工作已经不再是“主链路是否跑通”，而是后续治理项，例如：

- 继续提升测试覆盖
- 继续完善可观测性说明
- 继续清理少量历史文档口径
- 视需要推进更彻底的模块收敛
