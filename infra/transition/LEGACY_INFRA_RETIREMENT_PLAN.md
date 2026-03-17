# 老基础设施退场方案

## 文档目的

这份文档用于明确当前项目中“老基础设施模块如何退场、新基础设施如何接管”的执行顺序。

当前项目的基础设施迁移不是简单删模块，而是分阶段完成以下工作：

1. 明确旧模块当前职责
2. 明确新模块或新平台接管方式
3. 明确退场前提
4. 明确最终删除时机

## 一、基础设施映射关系

| 当前模块 | 当前职责 | 目标替代方案 | 当前状态 |
|---|---|---|---|
| `eureka-server` | 服务注册与发现 | `Nacos Discovery` | 待退场 |
| `index-config-server` | 配置中心 | `Nacos Config` | 待退场 |
| `index-zuul-service` | 网关入口 | `gateway-service` + Spring Cloud Gateway | 已开始并行迁移 |
| `index-hystrix-dashboard` | 熔断监控看板 | Prometheus + Grafana | 待退场 |
| `index-turbine` | 熔断聚合监控 | Prometheus + Grafana | 待退场 |

## 二、退场原则

### 1. 不直接删除正在承载线上职责的模块

迁移过程中必须遵循：

- 先有替代者
- 再做并行验证
- 最后才让旧模块退场

### 2. 优先替换“外围基础设施”，再替换“核心业务服务”

优先级建议：

1. 注册中心
2. 配置中心
3. 网关
4. 服务间调用
5. 熔断与监控
6. 前端入口

### 3. 一次只做一种能力替换

例如：

- 不要在同一次改造里同时做 `Eureka -> Nacos` 和 `Feign -> HTTP Service Clients`
- 不要在同一次改造里同时做 `Gateway` 替换和前端入口切换

## 三、eureka-server 退场方案

### 当前职责

- 提供服务注册中心
- 所有服务通过 Eureka 做发现

### 新方案

- 使用 `Nacos Discovery` 替代

### 退场前提

满足以下条件后，`eureka-server` 才能退场：

1. Nacos 本地运行方案已具备
2. 至少一个服务已完成 `Nacos Discovery` 试点接入
3. 网关或服务调用链已验证能通过 Nacos 正常发现服务
4. 其余服务具备迁移模板与接入路径

### 退场步骤

1. 选一个试点服务完成 Nacos 注册
2. 再迁移核心调用链上的服务
3. 让新网关基于新注册中心工作
4. 确认旧的 Eureka 注册信息不再被依赖
5. 最后删除 `eureka-server` 模块和相关依赖

### 当前建议

当前不要直接删除 `eureka-server`，应先保留，作为旧体系的兜底基础设施。

### 当前阶段性结论

结合当前仓库状态，可以确认：

1. `index-codes-service`、`index-data-service`、`gateway-service`
   已具备 `Nacos Discovery` 试点能力
2. `trend-trading-backtest-service` 与 `trend-trading-backtest-view`
   已具备面向 `Nacos` 的试点入口
3. 当前新的主入口和关键业务链路已经不再只依赖 `Eureka`

因此本轮先执行了一个更轻量、也更符合当前阶段的退场动作：

1. 从父工程 `pom.xml` 中移除了 `eureka-server`
2. 当前先保留模块源码目录本身，作为旧体系参考与必要时回退依据
3. 后续再视 `index-gather-store-service`、`index-zuul-service` 等旧模块收缩情况，决定何时物理删除

## 四、index-config-server 退场方案

### 当前职责

- 提供集中式配置服务
- 基于 Git 读取远程配置
- 配合 Bus 做配置刷新

### 新方案

- 使用 `Nacos Config`

### 退场前提

满足以下条件后，`index-config-server` 才能退场：

1. 关键服务的配置模板已经整理完成
2. 至少一个服务已经能够从 Nacos 读取配置
3. 动态配置刷新链路有替代方案
4. 原 Git 配置仓里的关键配置项已完成迁移

### 退场步骤

1. 先把各服务配置模板沉淀到仓库
2. 逐步导入 Nacos Data ID
3. 先试点一个服务切换配置来源
4. 再扩展到其他服务
5. 确认没有服务再依赖 Config Server 后，删除 `index-config-server`

### 当前已确认的配置依赖盘点

截至当前仓库状态，可以先确认以下事实：

1. `index-codes-service`、`index-data-service`、`gateway-service`
   - 已补齐 `bootstrap-nacos.yml`
   - 已具备 `Nacos Config` 试点入口
   - 已有对应的 `templates/*.yaml` 可作为未来导入 `Nacos` 的 Data ID 内容

2. `trend-trading-backtest-view`
   - 当前仍保留 `bootstrap.yml`
   - 仍通过 `spring.cloud.config.discovery.serviceId=index-config-server` 读取旧配置
   - 仍依赖 `spring-cloud-starter-bus-amqp`
   - 仍依赖本地 `RabbitMQ` 作为旧配置刷新链路

### 当前建议的首批迁移清单

建议把 `index-config-server` 的首批迁移对象明确为：

1. `trend-trading-backtest-view`
   - 优先迁移它的配置读取入口
   - 目标是让它在 `nacos` profile 下绕过 `Config Server + Bus + RabbitMQ`
   - 当前已具备 `Nacos Config` 试点入口，下一步是继续验证其 `Nacos Discovery` 路径

2. `trend-trading-backtest-service`
   - 当前已补齐 `Nacos Discovery` 与 `Nacos Config` 试点入口
   - 已修正回测服务误注入 Hystrix fallback 的问题，避免默认命中兜底假数据
   - 已抽出远程市场数据访问接缝，后续可在不改业务计算逻辑的前提下替换 `Feign`
   - 已把兜底返回逻辑从 Feign fallback 适配类中独立出来，后续替换 `Hystrix` 时可复用
   - 已预留并行 HTTP 调用实现入口，默认仍走 `Feign`
   - 已将远程调用失败后的兜底处理上提为通用门面，当前 `feign/http` 两种模式都可复用同一套降级策略
   - 已为 `Resilience4j` 增加最小并行试点入口，可按配置启用新的调用保护层而不影响默认旧路径
   - 已移除回测服务启动类中的 `@EnableCircuitBreaker`，继续弱化 `Hystrix` 时代的显式框架痕迹
   - 已从回测服务模块中移除 `spring-cloud-starter-netflix-hystrix` 依赖与相关配置开关
   - 当前回测服务已不再依赖 `Feign + Hystrix` 旧调用链，后续重点转向继续完善 `Resilience4j` 与指标体系

2. `index-config-server` 自身的 Git 配置来源
   - 当前仓库中记录的是远程 Git 地址 `https://github.com/how2j/trendConfig/`
   - 后续需要把其中仍被消费的关键配置项转写为 `infra/nacos-config/templates/*.yaml`

### 当前远程 Git 配置仓盘点结果

本轮已对 `https://github.com/how2j/trendConfig/` 做了实际盘点，当前确认结果如下：

1. 远程仓库的配置目录为 `respo/`
2. 当前实际存在的配置文件只有：
   - `trend-trading-backtest-view-dev.properties`
3. 当前已确认的配置项只有：
   - `version = how2j trend trading backtest view version 1.5`

这说明截至当前仓库状态：

- `index-config-server` 仍在承载的远程 Git 配置范围非常小
- 首批需要转写到 `Nacos Config` 的关键项已经可以明确落到 `trend-trading-backtest-view-dev.yaml`
- `trend-trading-backtest-service-dev.properties` 等其他服务配置文件在当前远程仓库中并不存在，后续若需要迁移，应先确认是否来自别的配置来源

### 当前建议

当前不要直接删除 `index-config-server`，应先把它视作“旧配置体系”的兼容保底模块。

## 五、index-zuul-service 退场方案

### 当前职责

- 对外统一网关入口
- 代理后端微服务访问

### 新方案

- `gateway-service`

### 退场前提

1. `gateway-service` 路由已补齐
2. 路由验证通过
3. 新网关具备跨域、监控和基础治理能力
4. 调用入口完成切换

### 当前状态

- 已新增 `gateway-service`
- 已平移核心路由
- 当前处于“新旧网关并行”阶段

## 六、index-hystrix-dashboard / index-turbine 退场方案

### 当前职责

- 熔断指标查看
- 熔断监控聚合

### 新方案

- `Resilience4j + Prometheus + Grafana`

### 退场前提

1. 核心服务已经不再依赖 Hystrix
2. Prometheus 指标可以正常采集
3. Grafana 看板已初步建立

### 当前建议

这两个模块属于后退场模块，应排在注册中心、配置中心、网关之后处理。

### 当前试点进展

1. `trend-trading-backtest-service`
   - 已补最小 `Prometheus` 指标暴露入口
   - 已引入 `micrometer-registry-prometheus`
   - 已在配置中暴露 `/actuator/prometheus`
   - 当前可作为后续 `Hystrix Dashboard / Turbine -> Prometheus / Grafana` 的第一个业务服务试点

2. 本地 `Prometheus` 抓取方案
   - 已新增 `infra/docker-compose/prometheus/docker-compose.yml`
   - 已新增 `infra/docker-compose/prometheus/prometheus.yml`
   - 当前默认抓取：
     - `trend-trading-backtest-service` 的 `http://host.docker.internal:8051/actuator/prometheus`
     - `gateway-service` 的 `http://host.docker.internal:8032/actuator/prometheus`
   - 后续可以继续在同一配置里扩展其他业务服务抓取目标

3. `gateway-service`
   - 已补最小 `Prometheus` 指标暴露入口
   - 已引入 `micrometer-registry-prometheus`
   - 已在配置中暴露 `/actuator/prometheus`
   - 当前可作为入口层的监控替代试点

4. 本地 `Grafana` 运行样板
   - 已新增 `infra/docker-compose/grafana/docker-compose.yml`
   - 已新增 `infra/docker-compose/grafana/provisioning/datasources/prometheus.yml`
   - 当前默认预置 `Trend Prometheus` 数据源，指向 `http://host.docker.internal:9090`
   - 已新增 dashboard provisioning 配置与最小总览面板样板
   - 当前可直接展示：
     - `gateway-service`
     - `trend-trading-backtest-service`
     - `index-data-service`
     - `index-codes-service`
     的基础存活状态

5. `index-data-service`
   - 已补最小 `Prometheus` 指标暴露入口
   - 已引入 `micrometer-registry-prometheus`
   - 已在配置中暴露 `/actuator/prometheus`
   - 当前可作为市场数据链路的监控替代试点

6. `index-codes-service`
   - 已补最小 `Prometheus` 指标暴露入口
   - 已引入 `micrometer-registry-prometheus`
   - 已在配置中暴露 `/actuator/prometheus`
   - 当前可作为市场元数据链路的监控替代试点

7. `index-gather-store-service`
   - 已补最小 `Prometheus` 指标暴露入口
   - 已引入 `micrometer-registry-prometheus`
   - 已在配置中暴露 `/actuator/prometheus`
   - 当前可作为市场数据采集链路的监控替代试点

### 当前阶段性结论

结合当前仓库状态，可以确认：

1. `trend-trading-backtest-service` 已退出 `Hystrix` 模块依赖
2. `trend-trading-backtest-service` 与 `gateway-service` 已暴露最小 `Prometheus` 指标入口
3. 本地 `Prometheus` 与 `Grafana` 的最小运行样板都已入库

因此当前已具备把 `index-hystrix-dashboard` 与 `index-turbine` 从父工程主构建中摘除的条件。

本轮已执行的阶段性退场动作如下：

1. 从父工程 `pom.xml` 中移除了：
   - `index-hystrix-dashboard`
   - `index-turbine`
2. 已物理删除以下模块目录中的源码与模块文件：
   - `index-hystrix-dashboard`
   - `index-turbine`
3. 当前旧监控体系已完成“从主构建移除 + 源码目录删除”的阶段性退场

## 七、推荐执行顺序

建议按以下顺序推进：

1. Nacos 基础设施方案入库
2. Nacos Config 模板入库
3. Gateway 试点模块落库
4. 试点一个服务接入 Nacos Discovery
5. 试点一个服务接入 Nacos Config
6. 验证新网关基于新注册中心工作
7. 再让 `eureka-server` 与 `index-config-server` 退场
8. 最后处理 Hystrix 体系退场

## 八、当前结论

截至当前阶段，项目的基础设施迁移状态可以总结为：

- 新基础设施已经开始进入仓库
- 老基础设施仍保留运行价值
- 当前适合采用“并行迁移、逐步接管、最后退场”的策略

这也是最适合在简历和面试中描述的方式：

- 不是一次性推倒重来
- 而是在保障系统可持续演进的前提下，完成遗留基础设施的渐进式替换
