### 基于spring cloud的趋势投资项目

##### 当前本地完整链路
* 启动 Redis
* 启动 Nacos 2.4.3 单机版
* 启动四个核心服务
* 访问 http://127.0.0.1:8032/trend-web/

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

如果你只想单独执行验收，也可以直接使用：

```bat
python .tools\verify_local_migration.py
```

这个脚本会统一检查：

* 本地 `Nacos` 是否可达
* 四个核心服务是否都已在 `Nacos` 中注册为健康实例
* 四个核心 `Data ID` 是否已存在于本地 `Nacos Config`
* 网关页面入口、健康检查、市场数据接口、回测接口是否都返回正常结果

##### 项目启动顺序
* 启动 Redis
* 执行 `python .tools\local_stack.py up`
* 访问 `http://127.0.0.1:8032/trend-web/`
* 如需查看状态，执行 `python .tools\local_stack.py status`
* 如需一键验收，执行 `python .tools\local_stack.py verify`
* 如需停机，执行 `python .tools\local_stack.py down`

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

##### 微服务端口

微服务 | 项目名 |  端口  
-|-|-
第三方数据中心|third-part-index-data-project | 8090 
市场数据服务|market-data-service | 8061
路由|gateway-service | 8032
模拟回测视图服务|trend-trading-backtest-view | 8041,8042,8043 
模拟回测服务|trend-trading-backtest-service | 8051,8052,8053 

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
