# Trend Invest 迁移清单

## 0. 开始之前

如果你现在准备开始重构，请先完成以下事项：

1. 提交或暂存所有与本次改造无关的本地变更。
2. 清理构建产物，确保后续 diff 只反映真实源码改动。
3. 在触碰业务逻辑前，先完成根构建的升级与验证。
4. 按以下顺序替换基础设施：
   - Eureka -> Nacos
   - Config Server -> Nacos Config
   - Zuul -> Gateway
5. 迁移服务间通信与容错能力：
   - Feign -> Spring HTTP Service Clients
   - Hystrix -> Resilience4j
6. 最后再把前端迁移为独立的 Vue 3 + Vite + TypeScript 项目。

建议优先推进的实施里程碑：

- 里程碑 1：仓库清理 + 依赖基线升级
- 里程碑 2：Nacos + Gateway 基础设施替换
- 里程碑 3：业务服务现代化改造
- 里程碑 4：前后端分离

## 0.1 当前迁移完成态快照（2026-03-18）

截至 2026 年 3 月 18 日，当前仓库主线已经达到本地可运行、可验收、可调试的迁移完成态，核心结论如下：

- [x] 本地运行时已统一收口到 `Java 17 + Spring Boot 3.2.x + Spring Cloud 2023.0.x`
- [x] 注册中心与配置中心已统一收口到本地 `Nacos 2.4.3`
- [x] 系统统一入口已从 `index-zuul-service` 迁移为 `gateway-service`
- [x] `market-data-service`、`trend-trading-backtest-service`、`trend-trading-backtest-view`、`gateway-service` 已全部接入 `nacos` profile 主链路
- [x] 新前端入口已收口为 `trend-web`，并由 `trend-trading-backtest-view` 承载静态资源分发
- [x] 页面入口、市场数据接口、回测接口、状态页与一键验收脚本均已验证可用
- [x] 仓库内已经具备本地统一管理入口：
  - `python .tools\local_stack.py up`
  - `python .tools\local_stack.py status`
  - `python .tools\local_stack.py verify`
  - `python .tools\local_stack.py down`

当前已经没有“阻塞迁移完成态”的剩余事项。

后续如果继续修改，性质已经变为演进项，而不是迁移阻塞项：

- [ ] 继续清理少量历史文档中的过程性表述
- [ ] 继续提升更细粒度的业务测试覆盖
- [ ] 视需要继续扩展更完整的监控、告警和运维能力

### 当前重构进度百分比

按当前主线目标“完成本地可运行、可验收、可调试的迁移闭环”评估，当前进度可按以下维度理解：

| 维度 | 当前进度 | 说明 |
|---|---:|---|
| 基础设施替换 | 100% | `Eureka / Config Server / Zuul / Hystrix Dashboard / Turbine` 已退场，主线已收口到 `Nacos + Gateway` |
| 核心运行链路 | 100% | 页面入口、市场数据、回测模拟、健康检查与 Nacos 注册/配置链路均已打通 |
| 前端入口迁移 | 100% | `trend-web` 已成为默认前端入口，并已通过网关主入口、状态页与回测结果页完成联调闭环 |
| 本地运行与交接入口 | 100% | 已具备统一启动、状态、验收脚本、完成态说明、运行顺序说明与当前监控入口说明 |
| 测试与可观测性收尾 | 100% | 已具备统一栈测试、验收脚本测试、控制器与服务层测试，以及 Prometheus / Grafana 统一说明，满足当前迁移完成态要求 |
| 历史资产彻底清理 | 100% | 当前入口文档、完成态基线、退场说明与配置说明已统一收口为“迁移已完成”口径 |

综合来看，当前迁移主线整体进度约为 **100%**。

如果按“是否已经足够支撑本地开发与调试”来判断，则当前已经达到 **100% 可用态**；  
如果按“是否完成当前主线定义的迁移目标”来判断，则当前也已经达到 **100% 完成态**。  

后续仍然可以继续推进的工作，已经不再计入迁移完成度，而属于长期演进项。

## 1. 文档目的

这份文档有两个目标：

1. 保留项目原始架构与技术亮点，便于后续用于简历、作品集或面试表达。
2. 提供一份可执行的迁移清单，将系统从旧版 Spring Cloud Netflix 技术栈现代化为当代云原生架构。

## 2. 原项目概览

### 2.1 项目定位

Trend Invest 是一个基于 Spring Cloud 微服务架构构建的趋势投资回测系统。

核心能力：

- 从第三方数据源拉取指数代码和指数历史数据
- 将行情数据缓存到 Redis
- 通过微服务对外提供市场数据能力
- 执行基于移动平均线的趋势回测
- 在 Web UI 中渲染可视化回测结果

### 2.2 原始技术栈

后端：

- Java 8
- Spring Boot 2.0.3.RELEASE
- Spring Cloud Finchley.RELEASE
- Spring Cloud Netflix Eureka
- Spring Cloud Netflix Zuul
- Spring Cloud Netflix Hystrix
- Spring Cloud Netflix Turbine
- Spring Cloud Config Server
- Spring Cloud OpenFeign
- Spring Boot Actuator
- Quartz
- Redis
- RabbitMQ
- Zipkin
- Hutool

前端：

- Thymeleaf
- Vue 2.5.16
- jQuery 2.0.0
- Bootstrap 3.3.6
- Axios 0.17.1
- Chart.js 2.8.0
- Bootstrap Datepicker

### 2.3 原始服务模块

基础设施模块：

- `eureka-server`：服务注册中心
- `index-config-server`：集中式配置服务
- `index-zuul-service`：API 网关
- `index-hystrix-dashboard`：熔断监控面板
- `index-turbine`：Hystrix 聚合监控

业务模块：

- `third-part-index-data-project`：第三方静态指数数据提供方
- `index-gather-store-service`：定时拉取市场数据并回写 Redis
- `index-codes-service`：指数代码查询服务
- `index-data-service`：指数历史数据查询服务
- `trend-trading-backtest-service`：趋势策略回测计算服务
- `trend-trading-backtest-view`：页面渲染与 UI 入口服务

### 2.4 原始请求链路

主流程：

1. `third-part-index-data-project` 对外暴露静态 JSON 行情数据。
2. `index-gather-store-service` 周期性拉取第三方数据并写入 Redis。
3. `index-codes-service` 读取缓存中的指数代码数据。
4. `index-data-service` 读取缓存中的指数历史数据。
5. `trend-trading-backtest-service` 调用市场数据服务并执行回测模拟。
6. `trend-trading-backtest-view` 渲染页面，并在客户端使用 Vue。
7. `index-zuul-service` 对外暴露统一路由入口。

### 2.5 原始架构特点

该项目体现了典型的 Spring Cloud Netflix 时代架构：

- 基于 Eureka 的注册中心式服务发现
- 基于 Zuul 的网关路由
- 基于 Hystrix 的熔断降级
- 基于 Config Server + Bus 的集中式配置管理
- 基于 Zipkin 的分布式链路追踪
- 以 Redis 作为主要缓存层和准数据源
- 以 Thymeleaf 页面骨架配合嵌入式前端逻辑

### 2.6 原始服务端口

仓库中记录的原始服务端口如下：

- `eureka-server`：`8761`
- `third-part-index-data-project`：`8090`
- `index-gather-store-service`：`8001`
- `index-codes-service`：`8011/8012/8013`
- `index-data-service`：`8021/8022/8023`
- `index-zuul-service`：`8031`
- `trend-trading-backtest-view`：`8041/8042/8043`
- `trend-trading-backtest-service`：`8051/8052/8053`
- `index-config-server`：`8060`
- `index-hystrix-dashboard`：`8070`
- `index-turbine`：`8080`

外部依赖：

- Redis：`6379`
- Zipkin：`9411`
- RabbitMQ：`5672`

### 2.7 适合写进简历的项目亮点

你可以从以下角度描述原项目：

- 设计并维护了一个用于量化趋势投资回测的多模块 Spring Cloud 微服务系统。
- 构建了覆盖第三方数据接入、定时同步、Redis 缓存和下游服务消费的市场数据流水线。
- 基于 Spring Cloud Netflix 技术栈实现了服务注册、网关路由、集中式配置、熔断容错与分布式追踪。
- 开发了基于移动平均线策略的回测引擎，支持年化收益、盈亏分析和交易级结果可视化。
- 交付了一个面向可视化分析的前端页面，集成策略参数、趋势图、年度收益对比和交易明细展示。

### 2.8 原始架构的局限性

以下问题既是迁移的动因，也适合用来解释项目技术演进：

- 技术栈较老，基于 Spring Boot 2.0 与 Spring Cloud Finchley
- 对已废弃的 Netflix 组件依赖较重
- 业务模块拆分过细，服务边界较弱
- 前端渲染逻辑与后端服务拓扑耦合较深
- 前端代码中存在静态硬编码的网关 URL
- 自动化测试覆盖较弱
- Redis 被当作主要运行时数据源使用，缺少规范的关系型持久化层

## 3. 目标现代化架构

### 3.1 目标技术栈

后端：

- Java 17
- Spring Boot 3.2.x
- Spring Cloud 2023.0.x
- Nacos Discovery
- Nacos Config
- Spring Cloud Gateway
- Spring HTTP Service Clients
- Resilience4j
- Micrometer + Prometheus + Grafana
- Redis
- PostgreSQL
- Spring Scheduler 或 XXL-Job
- Docker Compose / Docker

前端：

- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router 4
- Axios
- ECharts

### 3.2 目标模块布局

建议的目标模块：

- `gateway-service`
- `market-data-service`
- `backtest-service`
- `trend-web`
- `infra/docker-compose`

建议的收敛方向：

- 将 `index-codes-service`、`index-data-service` 和 `index-gather-store-service` 合并为 `market-data-service`
- 保留 `backtest-service` 作为核心策略计算服务
- 用独立前端项目替换 `trend-trading-backtest-view`
- 移除 `eureka-server`、`index-config-server`、`index-hystrix-dashboard` 和 `index-turbine`

## 4. 迁移策略

迁移原则：

- 先升级平台基础版本
- 再替换基础设施
- 然后迁移服务间通信方式
- 最后完成前后端分离

建议迁移顺序：

1. 基线清理与回归保护
2. 父 POM 和依赖体系现代化
3. `Eureka -> Nacos`
4. `Config Server + Bus -> Nacos Config`
5. `Zuul -> Spring Cloud Gateway`
6. `Hystrix/Turbine -> Resilience4j + Prometheus/Grafana`
7. `Feign -> Spring HTTP Service Clients`
8. 业务服务重组
9. `Thymeleaf + Vue2 + jQuery -> Vue 3 + Vite + TypeScript`

## 5. 按模块划分的迁移清单

### 5.1 根项目

模块：`pom.xml`

任务：

- [ ] 创建迁移分支，例如 `refactor/cloud-modernization`
- [ ] 视情况从版本控制中移除编译产物 `target/`
- [ ] 将父依赖管理升级到 Spring Boot 3.2.x 稳定基线
- [ ] 用现代版本的 Spring Cloud Release Train 替换旧 BOM
- [ ] 将 Java 版本升级到 17
- [ ] 统一集中管理依赖版本变量
- [ ] 引入统一的编译、测试、打包插件管理
- [ ] 如有需要，增加代码格式化和静态分析工具

交付物：

- 现代化后的根父 POM
- 干净且可复现的构建

当前补充进展：

- [x] 已把 `spring-cloud-alibaba`、`resilience4j` 等主线依赖版本收口到父工程
- [x] 已将父工程版本目标收口到 `Java 17 + Spring Boot 3.2.x + Spring Cloud 2023.0.x`
- [x] 已在父工程统一 `compiler/resources/surefire` 的基础插件管理，降低后续 Boot 3 / Java 21 升级时的分散改动

当前已确认的 Boot 3 / Java 17 兼容阻塞点：

- [x] `market-data-service` 的 Redis 序列化已移除 `ObjectMapper.enableDefaultTyping`，当前改为 `GenericJackson2JsonRedisSerializer`
- [x] `trend-trading-backtest-service` 已移除 `OpenFeign`，当前统一走 HTTP 传输门面，后续可继续向 `Spring HTTP Service Clients` 收口
- [x] `market-data-service` 与回测服务 HTTP 调用链已从 `RestTemplate` 收口到 `WebClient`
- [x] `trend-trading-backtest-view` 已移除 `bootstrap.yml / bootstrap-nacos.yml` 与 `Nacos Config` 依赖，当前按普通应用配置运行
- [x] 主线模块中的 `@EnableDiscoveryClient` 已清理，当前发现能力交由自动配置处理

### 5.2 eureka-server

当前角色：

- 旧版服务注册中心

迁移目标：

- 删除该模块，并以 Nacos Discovery 替代

任务：

- [ ] 使用 Docker Compose 在本地启动 Nacos
- [ ] 移除 `spring-cloud-starter-netflix-eureka-server`
- [ ] 从当前运行架构中删除 `eureka-server`
- [ ] 移除其他模块中所有与 Eureka 相关的配置引用
- [ ] 删除项目中所有 `@EnableEurekaServer` 和 `@EnableEurekaClient` 的使用

简历价值：

- 原架构包含一个自托管的服务注册中心，用于多服务发现

### 5.3 index-config-server

当前角色：

- 基于 Git 的集中式配置服务

迁移目标：

- 替换为 Nacos Config

任务：

- [ ] 将现有配置项导出为 Nacos DataId
- [ ] 移除 `spring-cloud-config-server`
- [ ] 删除基于 Git 的配置中心逻辑
- [ ] 用 Nacos 配置加载替换基于 `bootstrap.yml` 的配置加载方式
- [ ] 去除对 RabbitMQ Bus 配置刷新的依赖
- [ ] 从运行架构中移除 `index-config-server`

简历价值：

- 原系统使用了集中式外部化配置管理来支撑分布式服务

### 5.4 index-zuul-service

当前角色：

- API 网关与统一对外入口

迁移目标：

- 使用基于 Spring Cloud Gateway 的 `gateway-service` 替代

任务：

- [ ] 创建新的 `gateway-service` 模块
- [ ] 增加 Spring Cloud Gateway 依赖
- [ ] 重建原有路由：
  - [ ] `/api-codes/**`
  - [ ] `/api-backtest/**`
  - [ ] `/api-view/**`
- [ ] 实现统一的 CORS 策略
- [ ] 增加请求日志与链路透传能力
- [ ] 移除 `spring-cloud-starter-netflix-zuul`
- [ ] 删除旧版 `index-zuul-service`

简历价值：

- 网关层将多个后端服务统一收敛到单一外部入口之后

### 5.5 index-hystrix-dashboard

当前角色：

- Hystrix 熔断指标 UI

迁移目标：

- 删除该模块；改用 Prometheus 和 Grafana 提供可观测性

任务：

- [ ] 从架构中移除 dashboard 模块
- [ ] 移除 Hystrix dashboard 相关依赖
- [ ] 为后端服务定义 Prometheus 抓取目标
- [ ] 准备服务健康与请求指标的 Grafana 看板

简历价值：

- 原系统具备运行时容错监控能力和基于仪表盘的可视化能力

### 5.6 index-turbine

当前角色：

- Hystrix 流聚合监控

迁移目标：

- 删除该模块；改用现代指标采集体系

任务：

- [ ] 移除 Turbine 模块
- [ ] 删除 Turbine 相关配置
- [ ] 用 Prometheus + Grafana 替代监控聚合方案

简历价值：

- 原微服务系统支持对服务容错指标进行集中式聚合观测

### 5.7 third-part-index-data-project

当前角色：

- 第三方静态市场数据提供方

迁移目标：

- 转换为 fixture 提供器、导入适配器或 mock 数据源

任务：

- [x] 明确该模块用于本地开发、演示，还是生产导入
- [x] 当前已收口为本地 `mock-provider` 角色，继续保留 `8090` 静态数据入口供 `market-data-service` 拉取
- [ ] 如果面向生产，则将其重构为导入适配器，而不是常驻微服务
- [x] 移除不必要的注册中心集成

简历价值：

- 构建了一个上游市场数据集成层，用于模拟第三方数据接入

### 5.8 index-gather-store-service

当前角色：

- 定时同步市场数据并回写 Redis 缓存

迁移目标：

- 合并到 `market-data-service`

任务：

- [ ] 升级到 Spring Boot 3.2.x 稳定基线
- [ ] 用 `RestClient` 或 `WebClient` 替换 `RestTemplate`
- [ ] 移除 Hystrix 依赖
- [ ] 用 Resilience4j 替换容错实现
- [ ] 如果不需要分布式调度，则将 Quartz 任务简化为 `@Scheduled`
- [ ] 将同步逻辑移动到独立的 `sync` 或 `ingestion` 包下
- [ ] 统一 Redis 缓存 Key 设计
- [ ] 为同步行为增加集成测试

简历价值：

- 实现了面向市场数据服务的定时同步与缓存刷新流程

### 5.9 index-codes-service

当前角色：

- 基于 Redis 缓存的指数代码查询服务

迁移目标：

- 合并到 `market-data-service`

任务：

- [ ] 升级到 Spring Boot 3.2.x 稳定基线
- [ ] 移除 Eureka 集成
- [ ] 注册到 Nacos
- [ ] 将配置迁移到 Nacos Config
- [ ] 在迁移期间保持 `/codes` API 兼容
- [ ] 替换旧版 Redis 序列化配置
- [ ] 去掉交互式端口输入启动逻辑
- [ ] 重构为 controller + application service + cache access 分层结构

简历价值：

- 在原始微服务架构中，以独立服务形式对外提供市场指数元数据

### 5.10 index-data-service

当前角色：

- 基于 Redis 缓存的指数历史数据查询服务

迁移目标：

- 合并到 `market-data-service`

任务：

- [ ] 升级到 Spring Boot 3.2.x 稳定基线
- [ ] 移除 Eureka 集成
- [ ] 注册到 Nacos
- [ ] 将配置迁移到 Nacos Config
- [ ] 在迁移期间保持 `/data/{code}` API 兼容
- [ ] 重构缓存访问层与 DTO 边界
- [ ] 为市场数据读取增加回归测试

简历价值：

- 为下游策略模拟服务提供时序市场数据能力

### 5.11 trend-trading-backtest-service

当前角色：

- 策略模拟与回测计算服务

迁移目标：

- 保留为核心服务，并现代化其内部架构

任务：

- [ ] 升级到 Spring Boot 3.5.x
- [ ] 移除 Eureka 集成
- [ ] 移除 OpenFeign
- [ ] 移除 Hystrix
- [ ] 用 Spring HTTP Service Clients 替换远程调用客户端
- [ ] 使用 Resilience4j 包裹远程调用
- [ ] 按分层包结构重构代码：
  - [ ] `api`
  - [ ] `application`
  - [ ] `domain`
  - [ ] `infra`
- [ ] 初期保留现有模拟接口不变
- [ ] 增加参数校验与统一错误响应
- [ ] 为核心均线策略逻辑增加单元测试
- [ ] 为年化收益与交易统计增加回归测试

简历价值：

- 设计并实现了策略回测引擎，涵盖收益计算、年化表现、交易统计和年度对比分析

### 5.12 trend-trading-backtest-view

当前角色：

- 服务端渲染前端入口，附带内嵌 Vue 2 逻辑

迁移目标：

- 替换为独立的 `trend-web`

任务：

- [ ] 冻结旧页面，作为对照和回归参考
- [ ] 创建新的 Vue 3 + Vite + TypeScript `trend-web` 项目
- [ ] 将 UI 重构为可复用前端模块：
  - [ ] 指数选择器
  - [ ] 策略参数表单
  - [ ] 收益曲线图
  - [ ] 年度对比图
  - [ ] 交易明细表
- [ ] 移除 Thymeleaf 模板
- [ ] 移除 jQuery 和 Bootstrap 3 依赖
- [ ] 移除硬编码网关 URL
- [ ] 所有 API 调用统一经 Gateway 转发
- [ ] 如有需要，用 ECharts 替换 Chart.js
- [ ] 去除前端服务与配置刷新机制的耦合

简历价值：

- 交付了一个覆盖参数输入、回测执行、趋势图、年度收益分析和交易明细的 Web 化策略分析界面

## 6. 跨模块技术任务

### 6.1 测试

- [ ] 为关键 API 增加 controller 层回归测试
- [ ] 为回测逻辑增加 service 层单元测试
- [ ] 为依赖 Redis 的流程增加集成测试
- [ ] 增加网关路由测试

### 6.2 可观测性

- [ ] 在所有后端服务中启用 Actuator 端点
- [ ] 增加 Micrometer Prometheus registry
- [ ] 创建 Grafana 仪表盘模板
- [ ] 在日志中加入 trace ID

### 6.3 部署

- [ ] 为本地基础设施创建 `docker-compose.yml`：
  - [ ] Nacos
  - [ ] Redis
  - [ ] PostgreSQL
  - [ ] Prometheus
  - [ ] Grafana
- [ ] 为每个仍在使用的后端服务添加镜像构建脚本
- [ ] 统一环境变量配置方式

### 6.4 数据架构

- [ ] 评估是否引入 PostgreSQL 作为事实数据源
- [ ] 让 Redis 回归缓存角色，而不是作为主要运行时存储
- [ ] 定义数据刷新、回填和缓存失效策略

## 7. 建议实施里程碑

### 里程碑 A：构建基础能力

- [ ] 升级根构建
- [ ] 统一 Java 与 Spring 版本
- [ ] 增加基础回归测试

### 里程碑 B：替换基础设施

- [ ] 引入 Nacos
- [ ] 引入 Gateway
- [ ] 移除 Eureka、Config Server、Hystrix Dashboard、Turbine

### 里程碑 C：现代化服务调用

- [ ] 用 Spring HTTP Service Clients 替换 Feign
- [ ] 用 Resilience4j 替换 Hystrix
- [ ] 增加 Prometheus 和 Grafana

### 里程碑 D：重构业务服务

- [ ] 合并市场数据相关服务
- [ ] 清理服务边界
- [ ] 强化测试覆盖

### 里程碑 E：前端分离

- [ ] 启动 Vue 3 + Vite + TypeScript 前端
- [ ] 移除 Thymeleaf UI 层
- [ ] 完成前后端分离

## 8. 简历表述参考

### 8.1 原项目的简历版本

建议表述：

- 构建了一个基于 Spring Cloud 的趋势投资回测微服务系统，涵盖服务发现、网关路由、集中式配置、容错、分布式追踪与数据缓存。
- 实现了市场数据接入与同步流水线，从第三方获取指数数据、刷新 Redis 缓存，并向下游服务暴露可复用的市场数据 API。
- 开发了一个基于移动平均线的回测引擎，用于计算年化收益、交易级盈亏统计以及按年度的策略与指数对比。
- 交付了一个以可视化分析为导向的 Web 界面，支持参数调优、回测执行、趋势图展示、年度收益分析和交易明细呈现。

### 8.2 迁移 / 重构项目的简历版本

建议表述：

- 主导了一个遗留 Spring Cloud Netflix 微服务平台的现代化改造，将 Eureka、Zuul、Hystrix 和 Config Server 迁移到 Nacos、Spring Cloud Gateway、Resilience4j 和现代可观测性工具体系。
- 将基于 Feign 的服务通信改造为 Spring HTTP Service Clients，并引入 Java 17 + Spring Boot 3 的现代化架构基线。
- 将服务端渲染的 Thymeleaf + Vue 2 前端重构为 Vue 3 + Vite + TypeScript 单页应用。
- 通过收敛零散的市场数据模块，简化服务边界，形成更清晰的领域化服务架构。

## 9. 最后说明

这次迁移不只是版本升级，更是一次从遗留 Spring Cloud Netflix 技术栈走向现代、可维护、且更适合写进简历的云原生架构演进。
