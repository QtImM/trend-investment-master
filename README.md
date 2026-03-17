### 基于spring cloud的趋势投资项目

##### 当前本地样例数据链路
* 启动 redis
* 启动 ThirdPartIndexDataApplication
* 启动 MarketDataApplication

当前第三方数据模块已经收口为本地样例数据提供器，`market-data-service` 会从 `http://127.0.0.1:8090/indexes` 拉取静态 JSON 样例数据。

##### 项目启动顺序
* 启动 redis
* 启动 MarketDataApplication
* 启动 GatewayServiceApplication
* 启动 TrendTradingBackTestServiceApplication
* 启动 TrendTradingBackTestViewApplication
* 启动 trend-web
* 访问 http://127.0.0.1:8032/trend-web/

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
