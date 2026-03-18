### 基于spring cloud的趋势投资项目

##### 当前项目定位

当前仓库主线已经完成从老 Spring Cloud Netflix 技术栈向当前本地可运行链路的迁移收口。

你现在真正会用到的主链路是：

* `gateway-service`：统一入口网关
* `market-data-service`：市场数据服务
* `trend-trading-backtest-service`：回测计算服务
* `trend-trading-backtest-view`：前端壳层与静态资源承载
* `trend-web`：新的默认前端入口

默认访问入口：

```text
http://127.0.0.1:8032/trend-web/
```

##### 运行项目顺序

当前最推荐的本地运行顺序如下：

1. 启动 Redis
2. 启动 Nacos 2.4.3 单机版
3. 启动四个核心服务
4. 访问 `http://127.0.0.1:8032/trend-web/`
5. 如需验收，执行 `python .tools\local_stack.py verify`

当前推荐的本地 `Nacos` 路径为 `C:\Tools\nacos-2.4.3\nacos`，启动命令为：

```bat
cd /d C:\Tools\nacos-2.4.3\nacos\bin
startup.cmd -m standalone
```

当前 `market-data-service` 已收口为优先使用仓库内样例数据，不再要求额外启动 `third-part-index-data-project` 或 `trend-web` 开发服务器即可完成主链路联调。

当前推荐直接使用仓库内统一脚本管理本地链路：

```bat
python .tools\local_stack.py up
python .tools\local_stack.py status
python .tools\local_stack.py verify
python .tools\local_stack.py down
```

命令说明：

* `up`：先启动本地 `Nacos 2.4.3`，再启动四个核心服务
* `status`：查看 `Redis / Nacos / 四个核心服务` 当前状态
* `verify`：执行整条迁移链路验收
* `down`：先停止四个核心服务，再停止本地 `Nacos`

如果你要按“从零开始”的顺序跑项目，最短命令路径就是：

```bat
python .tools\local_stack.py up
python .tools\local_stack.py verify
```

如果你想快速验证统一脚本没有被后续改坏，当前还提供了配套单元测试：

```bat
python .tools\test_local_stack.py
python .tools\test_verify_local_migration.py
```

如果你想验证回测服务最关键的业务回归测试，当前还可以直接执行：

```bat
.tools\apache-maven-3.9.9\bin\mvn.cmd -pl trend-trading-backtest-service test
```

这组测试当前已经覆盖：

* 空数据场景不再直接抛错
* 带尾斜杠的回测 URL 不再返回 `404`
* 核心回测服务对远端数据做倒序处理
* 空输入时 `simulate()` 仍返回稳定结构
* 典型买卖场景下会产出交易统计和年度收益结果

如果你只想单独执行验收，也可以直接使用：

```bat
python .tools\verify_local_migration.py
```

这个脚本会统一检查：

* 本地 `Nacos` 是否可达
* 四个核心服务是否都已在 `Nacos` 中注册为健康实例
* 四个核心 `Data ID` 是否已存在于本地 `Nacos Config`
* 网关页面入口、健康检查、市场数据接口、回测接口是否都返回正常结果

##### 功能说明

当前主线里每个核心服务的职责如下：

* `gateway-service`
  * 统一入口网关
  * 对外暴露 `/trend-web/`、`/api-market/**`、`/api-backtest/**`、`/api-view/**`
  * 当前默认对外访问都应优先经过它

* `market-data-service`
  * 提供指数列表和指数历史数据
  * 当前优先使用仓库内样例数据，不再依赖额外的第三方数据进程
  * 典型接口：
    * `/codes`
    * `/data/{code}`

* `trend-trading-backtest-service`
  * 接收策略参数并执行趋势回测
  * 返回收益曲线、年度收益、交易记录和统计指标
  * 典型接口：
    * `/simulate/{code}/{ma}/{buyThreshold}/{sellThreshold}/{serviceCharge}/{startDate}/{endDate}`

* `trend-trading-backtest-view`
  * 当前主要承担前端壳层与静态资源承载职责
  * 用于对接 `trend-web` 构建产物，并保留页面入口兼容层

* `trend-web`
  * 当前默认前端入口
  * 提供：
    * 回测参数表单
    * 收益指标卡片
    * 收益曲线
    * 年度收益分布
    * 交易明细
    * 状态页调试面板

##### 推荐使用方式

日常开发或调试，建议优先按这个节奏使用：

1. `python .tools\local_stack.py up`
   作用：拉起当前本地主链路
2. `python .tools\local_stack.py status`
   作用：确认 Redis、Nacos、四个核心服务都在线
3. 打开 `http://127.0.0.1:8032/trend-web/`
   作用：进入新的默认前端入口
4. `python .tools\local_stack.py verify`
   作用：做整条主链路验收
5. `python .tools\local_stack.py down`
   作用：停止当前本地栈

##### 当前完成态说明

截至当前仓库主线，本地迁移完成态已经收口为以下事实：

* 注册中心与配置中心统一收口到本地 `Nacos 2.4.3`
* 统一入口为 `gateway-service`
* 市场数据、回测计算、页面壳层均已通过 `nacos` profile 接入主链路
* `trend-web` 已作为新的默认前端入口，不再依赖额外开发服务器
* 当前主链路联调不再要求启动 `third-part-index-data-project`
* 当前最推荐的本地操作方式是：
  * `python .tools\local_stack.py up`
  * `python .tools\local_stack.py verify`

如果需要一份更短、更适合交接的当前完成态基线说明，可直接查看：

* `infra/transition/CURRENT_LOCAL_BASELINE.md`

##### 监控与可观测性

当前仓库已经补齐最小可观测性样板，推荐按以下顺序理解和使用：

1. 先保证主链路已启动：
   * `python .tools\local_stack.py up`
2. 再确认主链路仍健康：
   * `python .tools\local_stack.py verify`
3. 如需查看指标与面板，再启动监控样板：
   * `infra/docker-compose/prometheus`
   * `infra/docker-compose/grafana`

当前可直接使用的监控入口如下：

* Prometheus：`http://127.0.0.1:9090`
* Grafana：`http://127.0.0.1:3000`
  * 默认账号：`admin`
  * 默认密码：`admin`

当前 Prometheus 样板默认抓取以下服务的 `/actuator/prometheus`：

* `gateway-service`：`http://127.0.0.1:8032/actuator/prometheus`
* `trend-trading-backtest-service`：`http://127.0.0.1:8051/actuator/prometheus`
* `market-data-service`：`http://127.0.0.1:8061/actuator/prometheus`

当前 Grafana 样板已经预置：

* `Trend Prometheus` 数据源
* `Trend Services Overview` 总览面板

如果你需要更具体的启动命令、预置面板说明和当前监控边界，可直接查看：

* `infra/docker-compose/prometheus/README.md`
* `infra/docker-compose/grafana/README.md`

##### 微服务端口

微服务 | 项目名 |  端口  
-|-|-
统一入口网关|gateway-service | 8032
市场数据服务|market-data-service | 8061
回测计算服务|trend-trading-backtest-service | 8051
前端壳层服务|trend-trading-backtest-view | 8041

##### 历史/可选模块端口

模块 | 当前角色 | 端口
-|-|-
third-part-index-data-project | 历史 fixture / mock-provider，当前主链路默认不要求启动 | 8090

##### 第三方工具端口

工具 |  端口  
-|-
redis | 6379 
zipkin | 9411
rabbitmq | 5672

##### 项目图片
![微信截图_20191109183212.png](https://i.loli.net/2019/11/09/xJi8fqaWRXlcv9u.png)

![微信截图_20191109183253.png](https://i.loli.net/2019/11/09/E1wSUz6dL4IluVs.png)

![微信截图_20191109183243.png](https://i.loli.net/2019/11/09/7tCIKqunAQjLxfG.png)

![微信截图_20191109183306.png](https://i.loli.net/2019/11/09/gmr4CRve5WwhHDQ.png)

![微信截图_20191109183232.png](https://i.loli.net/2019/11/09/THCUszRpmkb9Jvo.png)

![微信截图_20191109183313.png](https://i.loli.net/2019/11/09/TxzqU6t8ldWFAus.png)
