### 基于spring cloud的趋势投资项目

##### 当前本地完整链路
* 启动 Redis
* 启动 Nacos 2.4.3 单机版
* 启动 market-data-service
* 启动 trend-trading-backtest-service
* 启动 trend-trading-backtest-view
* 启动 gateway-service
* 访问 http://127.0.0.1:8032/trend-web/

当前推荐的本地 `Nacos` 路径为 `C:\Tools\nacos-2.4.3\nacos`，启动命令为：

```bat
cd /d C:\Tools\nacos-2.4.3\nacos\bin
startup.cmd -m standalone
```

当前 `market-data-service` 已收口为优先使用仓库内样例数据，不再要求额外启动 `third-part-index-data-project` 或 `trend-web` 开发服务器即可完成主链路联调。

##### 项目启动顺序
* 启动 Redis
* 启动 Nacos：`C:\Tools\nacos-2.4.3\nacos\bin\startup.cmd -m standalone`
* 以 `nacos` profile 启动 `market-data-service`
* 以 `nacos` profile 启动 `trend-trading-backtest-service`
* 以 `nacos` profile 启动 `trend-trading-backtest-view`
* 以 `nacos` profile 启动 `gateway-service`
* 访问 `http://127.0.0.1:8032/trend-web/`

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
