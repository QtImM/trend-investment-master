# 重构执行记录

## 文档目的

这份文档用于持续记录项目重构过程中已经做过的事情，方便后续用于：

- 日常开发追踪
- 架构演进复盘
- 简历项目描述整理
- 面试时说明重构过程

## 执行日志

### 2026-03-17 - 阶段 0：仓库清理与重构起步

#### 本阶段目标

- 为正式重构建立一个干净、可持续推进的起点
- 先处理仓库噪音，避免后续 diff 被编译产物淹没
- 把原始架构和迁移方案沉淀成文档，便于后面写简历和做过程复盘

#### 已完成事项

1. 补充了 `.gitignore`
   - 新增忽略 `target/`
   - 新增忽略 `.idea/`
   - 新增忽略 `.vscode/`
   - 新增忽略 `*.iml`

2. 新增了迁移主文档 `MIGRATION_CHECKLIST.md`
   - 记录了原始架构
   - 记录了原始技术栈
   - 记录了模块职责
   - 记录了迁移目标架构
   - 记录了按模块拆分的重构清单
   - 记录了可用于简历描述的项目亮点

3. 执行了方案 A 的核心动作
   - 开始将仓库中已被 Git 跟踪的 `target/` 构建产物从版本控制中移除
   - 当前 `git diff --cached --name-status` 中已能看到大批 `target/` 文件处于删除状态
   - 这说明“取消跟踪构建产物”已经生效，后续提交后仓库会明显更干净

#### 当前结果

当前仓库状态表明：

- `.gitignore` 已正确更新
- 历史上被提交进仓库的 Maven 编译产物正在被移出 Git 管理
- 这一步完成后，后续重构将主要体现为源码变更，而不是 `class`、`jar`、`surefire-reports` 等噪音文件变更

#### 这一步为什么重要

- 现在这个项目之前把大量 `target/` 目录内容纳入了版本管理
- 这会导致每次构建后 Git 状态非常混乱
- 对重构来说，这种噪音会显著增加审查成本和误操作风险
- 所以在正式替换 `Eureka`、`Zuul`、`Hystrix` 之前，先做仓库治理是必要动作

#### 下一步计划

下一步准备进入“重构底座阶段”，优先顺序如下：

1. 确认 `target/` 取消跟踪动作无遗漏
2. 统一根工程的依赖管理策略
3. 开始基础设施迁移第一刀：
   - `Eureka -> Nacos`
   - `Config Server -> Nacos Config`
4. 然后再处理：
   - `Zuul -> Spring Cloud Gateway`

## 可直接用于简历/项目描述的阶段性表述

可以这样描述这一步工作：

- 启动遗留 Spring Cloud 微服务项目现代化重构，先完成仓库治理与迁移基线建设，清理历史编译产物跟踪问题，建立面向后续架构升级的可维护代码基线。
- 在重构初期沉淀原始架构、模块边界、迁移目标和阶段性执行清单，为后续技术升级和项目复盘提供完整依据。

### 2026-03-17 - 阶段 2：Nacos 基础设施方案入库

#### 本阶段目标

- 开始基础设施替换第一刀
- 先把 `Nacos` 的本地运行方案纳入仓库
- 为后续替换 `Eureka` 和 `Config Server` 提供统一入口

#### 已完成事项

1. 新增基础设施目录
   - 创建了 `infra/docker-compose/nacos`

2. 新增 `Nacos` 单机版运行文件
   - 编写了 `docker-compose.yml`
   - 采用单机模式
   - 映射了 `8848` 和 `9848` 端口

3. 新增中文运行说明
   - 说明了这个目录的用途
   - 说明了如何启动
   - 说明了当前替代关系：
     - `Eureka -> Nacos`
     - `Config Server -> Nacos`

4. 确认了当前环境限制
   - 当前开发机未安装 Docker
   - 因此本阶段先完成“基础设施定义入库”
   - 还未实际启动 Nacos 容器

#### 当前结果

现在仓库已经具备了后续基础设施迁移的第一个明确入口：

- 原来的基础设施还在代码中
- 但新的 `Nacos` 本地运行方案已经进入版本库
- 后续只需要在具备 Docker 环境的机器上执行启动命令，就可以进入注册中心与配置中心迁移

#### 这一步为什么重要

- 重构不能只停留在文档层面，必须先把新的基础设施入口真正落到仓库
- 这样后续每个服务迁移到 `Nacos` 时，都有统一依托
- 也方便你在简历里说明：不仅设计了迁移方案，还把新的基础设施运行方式落成了工程资产

#### 下一步计划

下一步准备进入服务接入层面的改造，优先顺序如下：

1. 先在某个基础设施模块上试点接入 `Nacos`
2. 再梳理 `Config Server -> Nacos Config`
3. 然后开始准备 `gateway-service`

### 2026-03-17 - 阶段 3：Nacos Config 模板入库

#### 本阶段目标

- 为 `Config Server -> Nacos Config` 迁移铺路
- 不直接冒险改运行时依赖，而是先把配置模板整理出来
- 让后续每个服务都有清晰的配置落点

#### 已完成事项

1. 新增 `infra/nacos-config` 目录
   - 用于集中管理未来迁移到 Nacos 的配置模板

2. 新增中文说明文档
   - 说明了目录目的
   - 说明了推荐的 Data ID 命名方式
   - 说明了为什么当前阶段先做模板入库，而不是直接切运行时依赖

3. 新增服务配置模板
   - `index-codes-service-dev.yaml`
   - `index-data-service-dev.yaml`
   - `trend-trading-backtest-service-dev.yaml`
   - `trend-trading-backtest-view-dev.yaml`

4. 明确了当前迁移策略
   - 先整理配置模板
   - 再继续升级版本底座
   - 最后再把服务正式切到 `Nacos Config`

#### 当前结果

当前仓库已经不再只是“知道要迁到 Nacos Config”，而是已经开始把未来配置中心里的内容结构化沉淀到仓库中。

这意味着后续迁移时：

- 不需要临时从各服务配置里东拼西凑
- 可以按照模板逐个导入 Nacos
- 每个服务的配置迁移会更可控

#### 这一步为什么重要

- 配置中心迁移最怕“边迁边找配置”
- 先把模板沉淀下来，可以显著降低后续切换风险
- 对简历来说，这一步也能体现你不是只会换依赖，而是能做完整的迁移规划与配置治理

#### 下一步计划

下一步准备开始“服务接入试点”，优先顺序如下：

1. 先从 `eureka-server` 和 `index-config-server` 的退场策略入手
2. 再开始梳理第一个试点服务的 `Nacos Discovery` 接入
3. 然后进入 `gateway-service` 的新模块建设

### 2026-03-17 - 阶段 4：gateway-service 试点模块落库

#### 本阶段目标

- 开始 `Zuul -> Spring Cloud Gateway` 的实际替换动作
- 先新增新网关模块，而不是立刻删除旧网关
- 通过“新旧并存”的方式降低迁移风险

#### 已完成事项

1. 在父工程中注册了新模块
   - 新增 `gateway-service`

2. 新建了网关试点模块
   - 新增模块 `gateway-service`
   - 新增启动类
   - 新增配置文件
   - 新增模块说明文档

3. 平移了原 Zuul 的核心路由
   - `/api-codes/**`
   - `/api-backtest/**`
   - `/api-view/**`

4. 做了并行迁移设计
   - 新网关先使用 `8032` 端口
   - 旧 `index-zuul-service` 继续保留
   - 避免一上来直接替换导致入口不可用

5. 补充了基础网关能力
   - 增加了全局跨域配置
   - 增加了 Actuator 暴露配置
   - 保留了 Zipkin 链路追踪入口

#### 当前结果

现在项目中已经不只有旧的 `Zuul` 网关，还具备了新的 `Spring Cloud Gateway` 试点模块。

这意味着后续可以分阶段推进：

- 先补齐新网关的配置
- 再逐步验证路由
- 最后再让新网关替换旧网关

#### 这一步为什么重要

- 网关是系统入口，不能粗暴替换
- 先把新网关模块落地，再和旧网关并行一段时间，是更稳妥的重构路径
- 对简历来说，这一步能体现你具备“不中断系统入口的渐进式迁移能力”

#### 下一步计划

下一步准备继续推进基础设施退场与接管，优先顺序如下：

1. 明确 `eureka-server` 的退场方案
2. 明确 `index-config-server` 的退场方案
3. 再选择一个服务试点 `Nacos Discovery` 接入

### 2026-03-17 - 阶段 5：老基础设施退场策略落库

#### 本阶段目标

- 把“老基础设施如何退场”从口头方案变成项目内的正式文档
- 明确旧模块与新方案之间的接管关系
- 为后续删模块和切换入口提供清晰依据

#### 已完成事项

1. 新增老基础设施退场方案文档
   - 编写了 `infra/transition/LEGACY_INFRA_RETIREMENT_PLAN.md`
   - 明确了：
     - `eureka-server`
     - `index-config-server`
     - `index-zuul-service`
     - `index-hystrix-dashboard`
     - `index-turbine`
     的退场前提与执行顺序

2. 新增服务迁移矩阵
   - 编写了 `infra/transition/SERVICE_TRANSITION_MATRIX.md`
   - 明确了每个模块未来是：
     - 保留
     - 合并
     - 删除
     - 待定

3. 固化了当前迁移原则
   - 先有替代者，再让旧模块退场
   - 新旧基础设施并行一段时间
   - 一次只替换一种能力

#### 当前结果

现在项目已经具备了“从文档到执行”的退场依据：

- 不是简单地知道要删哪些模块
- 而是已经明确了它们为什么暂时不能删
- 以及未来在什么条件下可以退场

#### 这一步为什么重要

- 遗留系统重构里，删模块往往比新建模块更危险
- 先把退场条件写清楚，能显著降低误删和误切换风险
- 对简历来说，这一步也体现了你具备系统性迁移治理能力，而不是只会写代码

#### 下一步计划

下一步准备正式开始“服务接入试点”，优先顺序如下：

1. 选一个非核心服务做 `Nacos Discovery` 接入试点
2. 继续保持老体系可用，避免一次性替换
3. 试点成功后再逐步扩展到业务服务

### 2026-03-17 - 阶段 6：index-codes-service 接入 Nacos Discovery 试点

#### 本阶段目标

- 在不破坏旧体系的前提下，完成第一个服务级别的 `Nacos Discovery` 接入试点
- 选择影响面较小的 `index-codes-service` 作为实验对象
- 验证“老 Eureka 路径保留，新 Nacos 路径可切换”的并行迁移方式

#### 已完成事项

1. 调整了 `index-codes-service` 的依赖
   - 引入兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Discovery` 依赖
   - 保留原有 `Eureka` 依赖，确保旧路径不立即失效

2. 增加了独立的 Nacos 配置文件
   - 新增 `application-nacos.yml`
   - 把 Nacos 注册地址配置为 `127.0.0.1:8848`
   - 在 Nacos profile 下关闭 Eureka client

3. 调整了启动类
   - 增加 `@EnableDiscoveryClient`
   - 增加 `nacos` profile 启动识别逻辑
   - 在 `nacos` 模式下，启动前检查从 `Eureka` 端口切换为 `Nacos` 端口

#### 当前结果

当前 `index-codes-service` 已具备两种运行思路：

- 默认仍走原有 Eureka 路径
- 通过 `nacos` profile 可切换到新的 Nacos 注册路径

这意味着服务级试点已经开始从“文档规划”进入“代码层接入”阶段。

另外，本阶段已经完成了第一次真实编译验证：

- 使用本机 Maven 对 `index-codes-service` 执行了 `compile`
- 当前结果为 `BUILD SUCCESS`
- 说明这条试点迁移路径在当前代码层面是可编译的

#### 这一步为什么重要

- 这是第一次真正让旧服务开始具备新注册中心能力
- 它验证了我们采用的“并行迁移而不是强行替换”的路线是可执行的
- 对简历来说，这一步能体现你不仅做了新基础设施设计，还实际推动了遗留服务逐步接入新体系

#### 当前注意事项

- 当前项目底座仍较老，这一步属于兼容性试点，不代表已经完成全面迁移
- 真正大规模切换前，仍需要继续验证依赖兼容性和启动行为
- 当前开发机未安装 Maven，因此本阶段尚未完成本地编译验证

#### 阶段内修正

- 在首次编译验证时，发现 `Nacos Discovery` 试点依赖坐标选择有误
- 已根据实际可解析的老版本坐标修正 `groupId`
- 修正后继续进行本地编译验证

#### 下一步计划

下一步准备继续做两件事：

1. 继续选一个服务做同类接入试点
2. 开始考虑让 `gateway-service` 后续接入新注册中心

### 2026-03-17 - 阶段 7：index-data-service 接入 Nacos Discovery 试点

#### 本阶段目标

- 复用 `index-codes-service` 已验证过的并行迁移方式
- 让 `index-data-service` 在保留旧 `Eureka` 路径的同时，具备 `Nacos Discovery` 试点能力
- 继续沿着市场数据链路推进第二个低风险试点服务

#### 已完成事项

1. 调整了 `index-data-service` 的依赖
   - 引入兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Discovery` 依赖
   - 保留原有 `Eureka` 依赖，确保默认运行方式不受影响

2. 增加了独立的 Nacos 配置文件
   - 新增 `application-nacos.yml`
   - 配置 `127.0.0.1:8848` 作为 `Nacos` 注册地址
   - 在 `nacos` profile 下关闭 `Eureka client`
   - 暴露基础 `Actuator` 端点，便于后续联调

3. 调整了启动类
   - 增加 `@EnableDiscoveryClient`
   - 增加 `nacos` profile 启动识别逻辑
   - 在 `nacos` 模式下，启动前改为检查 `Nacos` 端口
   - 默认模式下仍保持原有 `Eureka` 启动校验

4. 完成了本地编译验证
   - 当前环境最初缺少 `Maven` 命令
   - 已临时下载本地 `Maven 3.9.9` 工具用于验证
   - 使用本地 Maven 对 `index-data-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在市场数据链路上已经有两个服务具备并行注册能力：

- `index-codes-service` 可通过 `nacos` profile 接入 `Nacos Discovery`
- `index-data-service` 可通过 `nacos` profile 接入 `Nacos Discovery`

这意味着后续不再只是单点试验，而是开始形成一条可扩展的迁移样板。

#### 这一步为什么重要

- `index-data-service` 与 `index-codes-service` 同属市场数据服务，迁移方式相近
- 先把这两个服务接入模式统一下来，后续扩展到 `gateway-service` 或其他业务服务会更顺畅
- 这也进一步验证了“保留旧体系可运行、按 profile 渐进切换”的策略是可复制的

#### 下一步计划

下一步优先考虑以下动作：

1. 让 `gateway-service` 具备 `Nacos Discovery` 试点能力
2. 开始准备第一个服务的 `Nacos Config` 读取试点
3. 验证新网关是否能通过新注册中心发现已试点服务

### 2026-03-17 - 阶段 8：gateway-service 接入 Nacos Discovery 试点

#### 本阶段目标

- 让新的 `gateway-service` 在保留原有 `Eureka` 默认模式下，具备通过 `nacos` profile 接入新注册中心的能力
- 把前面两个市场数据服务的注册试点，继续延伸到调用入口层
- 为后续验证“新网关 + 新注册中心”的服务发现链路做准备

#### 已完成事项

1. 调整了 `gateway-service` 的依赖
   - 引入兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Discovery` 依赖
   - 保留原有 `Eureka` 依赖，继续兼容旧注册中心模式

2. 增加了独立的 Nacos 配置文件
   - 新增 `application-nacos.yml`
   - 保留原网关路由与跨域配置
   - 在 `nacos` profile 下配置 `127.0.0.1:8848`
   - 在 `nacos` profile 下关闭 `Eureka client`

3. 调整了启动类
   - 保留 `@EnableDiscoveryClient`
   - 增加 `nacos` profile 启动识别逻辑
   - 默认模式下仍检查 `Eureka` 端口
   - `nacos` 模式下改为检查 `Nacos` 端口
   - 继续保持网关固定使用 `8032` 端口运行，避免影响旧 `Zuul` 入口

4. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `gateway-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在新体系里已经不只是业务服务能试点注册到 `Nacos`，连新的网关入口也具备了切换到 `Nacos Discovery` 的能力。

这意味着当前迁移已经从“单个服务试点”推进到了“入口层也开始具备新注册中心接入能力”的阶段。

#### 这一步为什么重要

- 只有服务注册还不够，真正的基础设施替换还需要网关入口也能发现这些服务
- 先让 `gateway-service` 支持双路径运行，可以显著降低后续联调风险
- 这一步是 `eureka-server` 未来退场前的必要过渡动作

#### 下一步计划

下一步优先考虑以下动作：

1. 实际验证 `gateway-service` 在 `nacos` 模式下能发现已试点服务
2. 为一个服务启动 `Nacos Config` 配置读取试点
3. 开始梳理 `index-config-server` 的首批可迁移配置项

### 2026-03-17 - 阶段 9：index-codes-service 接入 Nacos Config 试点入口

#### 本阶段目标

- 在不切断旧配置路径的前提下，为 `index-codes-service` 增加首个 `Nacos Config` 试点入口
- 继续沿用“先试点、可切换、保留旧体系”的迁移方式
- 同时把 `infra/nacos-config` 的模板使用方式和实际目录结构对齐

#### 已完成事项

1. 调整了 `index-codes-service` 的依赖
   - 增加兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Config` 依赖
   - 保留原有依赖结构，不影响默认运行模式

2. 增加了 Nacos Config 引导文件
   - 新增 `bootstrap-nacos.yml`
   - 在 `nacos` profile 下配置 `Nacos Config` 地址
   - 指定读取 `index-codes-service-dev.yaml` 作为首个试点 Data ID

3. 校正了配置模板的职责边界
   - 调整 `infra/nacos-config/templates/index-codes-service-dev.yaml`
   - 去掉不应放在 Data ID 内容里的 `Nacos` 连接地址
   - 保留服务自身运行配置，并在模板中显式关闭 `Eureka client`

4. 更新了配置模板说明
   - 明确模板目录位于 `infra/nacos-config/templates`
   - 明确 `bootstrap-*.yml` 负责连接 Nacos
   - 明确模板文件负责承载未来导入到 Nacos 的服务运行配置

#### 当前结果

现在 `index-codes-service` 不仅具备 `Nacos Discovery` 试点能力，也已经具备了 `Nacos Config` 的最小引导入口。

这意味着配置中心迁移已经从“只有模板”推进到了“首个服务开始具备真实接入点”的阶段。

#### 这一步为什么重要

- `index-config-server` 的退场前提之一，就是至少一个服务可以从 `Nacos` 读取配置
- 先把试点入口补齐，再去做联调和导入验证，会比一上来大规模切换更稳
- 这一步也让后续复制到 `index-data-service` 或 `gateway-service` 时有了统一样板

#### 下一步计划

下一步优先考虑以下动作：

1. 让 `index-data-service` 按同样方式补齐 `Nacos Config` 试点入口
2. 联动验证 `index-codes-service` 的 `nacos` profile 配置加载行为
3. 继续梳理 `index-config-server` 中可迁移的首批配置项

### 2026-03-17 - 阶段 10：index-data-service 接入 Nacos Config 试点入口

#### 本阶段目标

- 复用 `index-codes-service` 已验证过的配置中心试点模式
- 让 `index-data-service` 在保留旧配置路径的同时，具备 `Nacos Config` 试点入口
- 让市场数据链路的两个服务在注册中心与配置中心两条迁移线上保持一致

#### 已完成事项

1. 调整了 `index-data-service` 的依赖
   - 增加兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Config` 依赖
   - 保留原有依赖结构，不影响默认运行方式

2. 增加了 Nacos Config 引导文件
   - 新增 `bootstrap-nacos.yml`
   - 在 `nacos` profile 下配置 `Nacos Config` 地址
   - 指定读取 `index-data-service-dev.yaml` 作为试点 Data ID

3. 调整了配置模板
   - 修改 `infra/nacos-config/templates/index-data-service-dev.yaml`
   - 去掉不应放在 Data ID 内容里的 `Nacos` 连接地址
   - 保留服务自身运行配置，并在模板中显式关闭 `Eureka client`

4. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `index-data-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在市场数据链路里的两个核心查询服务都已经具备：

- `Nacos Discovery` 试点能力
- `Nacos Config` 试点入口

这意味着后续再扩展到其他服务时，可以直接沿用同一套“application + bootstrap + template”的迁移样板。

#### 这一步为什么重要

- 配置中心迁移如果只停留在单个服务，样板价值有限
- 现在 `index-codes-service` 和 `index-data-service` 已经形成成对样板，更适合后续横向复制
- 这也让 `index-config-server` 的退场前提进一步接近可验证状态

#### 下一步计划

下一步优先考虑以下动作：

1. 联动验证 `index-codes-service` 与 `index-data-service` 的 `nacos` profile 配置加载行为
2. 让 `gateway-service` 继续补齐 `Nacos Config` 试点入口
3. 开始梳理 `index-config-server` 中首批可迁移的配置项

### 2026-03-17 - 阶段 11：gateway-service 接入 Nacos Config 试点入口

#### 本阶段目标

- 让 `gateway-service` 也具备 `Nacos Config` 试点入口
- 让网关与市场数据服务在 `Nacos Discovery + Nacos Config` 两条线上保持一致的迁移结构
- 为后续验证“新网关读取新配置并发现新服务”做好准备

#### 已完成事项

1. 调整了 `gateway-service` 的依赖
   - 增加兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Config` 依赖
   - 保留原有 `Gateway`、`Eureka`、`Nacos Discovery` 依赖，不影响默认运行模式

2. 增加了 Nacos Config 引导文件
   - 新增 `bootstrap-nacos.yml`
   - 在 `nacos` profile 下配置 `Nacos Config` 地址
   - 指定读取 `gateway-service-dev.yaml` 作为试点 Data ID

3. 新增了网关配置模板
   - 新增 `infra/nacos-config/templates/gateway-service-dev.yaml`
   - 把当前网关路由、跨域、Zipkin 与 Actuator 配置沉淀为未来可导入 `Nacos` 的模板
   - 在模板中显式关闭 `Eureka client`

4. 更新了配置模板说明
   - 在 `infra/nacos-config/README.md` 中补充 `gateway-service` 模板入口
   - 让当前模板清单与实际目录保持一致

5. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `gateway-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `gateway-service` 也已经具备了 `Nacos Config` 试点入口。

这意味着当前试点范围已经覆盖：

- 市场数据服务
- 新网关入口

并且这些试点模块都在沿用同一套 `application + application-nacos + bootstrap-nacos + templates/*.yaml` 的迁移样板。

#### 这一步为什么重要

- 网关是后续新调用链的入口，不能只做注册中心试点，不做配置中心试点
- 现在入口层与下游服务的迁移结构已经统一，后续联调和扩展都会更顺
- 这一步也让 `index-config-server` 的退场准备更完整

#### 下一步计划

下一步优先考虑以下动作：

1. 联动验证 `gateway-service`、`index-codes-service`、`index-data-service` 的 `nacos` profile 配置加载与注册行为
2. 梳理 `index-config-server` 当前仍承载的配置项，准备首批迁移清单
3. 再选择一个业务服务继续复制这套试点样板

### 2026-03-17 - 阶段 12：trend-trading-backtest-view 接入 Nacos Config 试点入口

#### 本阶段目标

- 把配置中心迁移从市场数据链路继续推进到真正仍在依赖 `index-config-server` 的消费方
- 让 `trend-trading-backtest-view` 在保留旧 `Config Server + Bus + RabbitMQ` 路径的同时，具备 `Nacos Config` 试点入口
- 同时梳理 `index-config-server` 的首批可迁移配置依赖

#### 已完成事项

1. 调整了 `trend-trading-backtest-view` 的依赖
   - 增加兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Config` 依赖
   - 保留原有 `Config Client` 与 `Bus AMQP` 依赖，确保默认模式不受影响

2. 增加了 Nacos Config 引导文件
   - 新增 `bootstrap-nacos.yml`
   - 在 `nacos` profile 下关闭旧 `Config Client` 发现逻辑
   - 在 `nacos` profile 下关闭旧 `Bus` 链路
   - 指定读取 `trend-trading-backtest-view-dev.yaml` 作为试点 Data ID

3. 调整了启动前置检查逻辑
   - 默认模式下仍要求 `Config Server` 与 `RabbitMQ` 可用
   - `nacos` 模式下改为检查 `Nacos` 端口
   - 保留对 `Eureka` 的依赖检查，避免把“配置中心迁移”和“注册中心迁移”混在同一步里

4. 调整了视图服务配置模板
   - 修改 `infra/nacos-config/templates/trend-trading-backtest-view-dev.yaml`
   - 去掉不应放在 Data ID 内容里的 `Nacos` 连接配置
   - 把 `version` 配置调整为当前控制器直接消费的顶层属性
   - 保留旧 `Eureka` 配置，继续兼容当前注册中心路径

5. 补充了 `index-config-server` 首批迁移清单
   - 在退场方案文档中明确：
     - 已具备 `Nacos Config` 试点入口的模块
     - 当前仍依赖 `Config Server + Bus + RabbitMQ` 的 `trend-trading-backtest-view`
     - 远程 Git 配置仓仍需继续盘点与转写

6. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `trend-trading-backtest-view` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在配置中心迁移已经不再只停留在市场数据链路，而是开始真正触碰旧 `Config Server` 的消费方。

这意味着后续可以更实际地验证：

- `nacos` profile 是否能替代旧配置来源
- 旧 `Bus + RabbitMQ` 链路是否可以逐步退出

#### 这一步为什么重要

- `trend-trading-backtest-view` 是目前仓库里已确认还在直接消费 `index-config-server` 的服务
- 先把它做成可切换试点，比继续只改模板更接近真实退场条件
- 同时把依赖盘点写进正式文档，也能避免后面误判哪些服务还没迁完

#### 下一步计划

下一步优先考虑以下动作：

1. 联动验证 `trend-trading-backtest-view` 在 `nacos` profile 下的配置加载行为
2. 继续梳理远程 Git 配置仓中仍被 `index-config-server` 承载的关键配置项
3. 再决定是否继续让该模块进入 `Nacos Discovery` 试点

### 2026-03-17 - 阶段 13：远程 Git 配置仓盘点并回填视图服务模板

#### 本阶段目标

- 核实 `index-config-server` 当前到底还在承载哪些远程 Git 配置
- 避免后续配置中心迁移基于猜测推进
- 把已确认的关键配置项回填到仓库内的 `Nacos Config` 模板

#### 已完成事项

1. 拉取并盘点了远程 Git 配置仓
   - 根据 `index-config-server` 的配置地址，实际拉取了 `https://github.com/how2j/trendConfig/`
   - 确认其配置目录为 `respo/`

2. 确认了当前远程配置范围
   - 当前仅发现 `trend-trading-backtest-view-dev.properties`
   - 其中当前已确认的配置项只有：
     - `version = how2j trend trading backtest view version 1.5`

3. 回填了本仓库模板
   - 将 `infra/nacos-config/templates/trend-trading-backtest-view-dev.yaml` 中的 `version`
     从占位值更新为远程配置仓中的真实值

4. 更新了退场方案文档
   - 在 `infra/transition/LEGACY_INFRA_RETIREMENT_PLAN.md` 中补充远程 Git 配置仓的实际盘点结果
   - 明确当前首批需要迁移到 `Nacos Config` 的关键项范围很小

#### 当前结果

现在关于 `index-config-server` 的迁移不再只是“知道它还没退场”，而是已经进一步确认：

- 它当前远程 Git 配置来源的范围很小
- 已确认的关键业务配置项已经能映射到仓库内模板

这会显著降低后续真正切换配置来源时的不确定性。

#### 这一步为什么重要

- 配置中心退场的难点常常不在依赖，而在“不知道旧配置到底承载了什么”
- 现在先把远程配置内容盘点清楚，后面做联调和切换会更稳
- 这一步也说明 `index-config-server` 的退场阻力可能比预期更小

#### 下一步计划

下一步优先考虑以下动作：

1. 联动验证 `trend-trading-backtest-view` 在 `nacos` profile 下是否能读取回填后的 `version` 配置
2. 再决定是否继续为 `trend-trading-backtest-view` 补齐 `Nacos Discovery` 试点能力
3. 评估是否可以开始弱化 `index-config-server` 与 `RabbitMQ` 的本地依赖地位

### 2026-03-17 - 阶段 14：trend-trading-backtest-view 接入 Nacos Discovery 试点

#### 本阶段目标

- 在视图服务已经具备 `Nacos Config` 入口的基础上，继续补齐它的 `Nacos Discovery` 试点能力
- 让 `trend-trading-backtest-view` 在 `nacos` profile 下逐步摆脱对旧 `Eureka + Config Server + Bus` 组合的强依赖
- 在当前本地环境仍缺少联调基础设施的情况下，先把代码层迁移路径铺平

#### 已完成事项

1. 调整了 `trend-trading-backtest-view` 的依赖
   - 增加兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Discovery` 依赖
   - 保留原有 `Eureka`、`Config Client` 与 `Bus` 依赖，确保默认运行模式不受影响

2. 增加了独立的 Nacos 运行配置
   - 新增 `application-nacos.yml`
   - 在 `nacos` profile 下配置 `Nacos Discovery` 地址
   - 在 `nacos` profile 下关闭 `Eureka client`
   - 保留 `Thymeleaf`、`Zipkin` 与 `Actuator` 基础配置

3. 调整了启动类
   - 增加 `@EnableDiscoveryClient`
   - 在 `nacos` profile 下，启动前不再强依赖 `Eureka` 端口
   - 继续保留默认模式下的旧启动前置检查

4. 明确了当前联调阻塞项
   - 当前本机未检测到 `Nacos(8848)`、`Eureka(8761)`、`Redis(6379)` 监听
   - 当前本机也没有可直接使用的 `Docker` 与 `redis-server`
   - 因此本阶段先完成代码层试点接入，真实联调仍需等待基础设施环境具备

5. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `trend-trading-backtest-view` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `trend-trading-backtest-view` 已经同时具备：

- `Nacos Config` 试点入口
- `Nacos Discovery` 试点能力

这意味着旧配置中心当前已确认的消费方，也开始具备向新注册中心和新配置中心双向迁移的代码基础。

#### 这一步为什么重要

- 如果视图服务只迁配置不迁注册，后续通过新网关或新注册中心联调时仍会受限
- 先把代码层路径补齐，可以在环境恢复后更快进入真实验证
- 这一步也把“当前卡点是环境而不是代码”明确写进了项目记录

#### 下一步计划

下一步优先考虑以下动作：

1. 在具备 `Nacos + Redis` 环境后，联动验证 `trend-trading-backtest-view` 的 `nacos` profile 启动与配置读取行为
2. 继续评估 `index-config-server` 与 `RabbitMQ` 在本地联调中的降级策略
3. 再决定是否继续向 `trend-trading-backtest-service` 扩展同类试点

### 2026-03-17 - 阶段 15：清理本地联调噪音并固化环境阻塞信息

#### 本阶段目标

- 清理当前仓库中因本地验证产生的临时目录噪音
- 避免后续每一步都被相同的本地工具目录干扰 `git status`
- 把当前真实联调阻塞点继续固化为可复用上下文

#### 已完成事项

1. 更新了 `.gitignore`
   - 新增忽略 `.tools/`
   - 新增忽略 `.temp-config-repo/`
   - 让本地下载的 Maven 工具与远程配置仓临时副本不再污染仓库状态

2. 固化了当前环境现状
   - 当前本机未检测到 `Nacos(8848)`、`Eureka(8761)`、`Redis(6379)` 监听
   - 当前本机不可直接使用 `Docker`
   - 当前本机也未发现 `redis-server`

#### 当前结果

现在仓库状态可以重新回到“只反映真实源码与文档变化”的状态。

这意味着后续继续推进时：

- 不会再被临时工具目录干扰
- 当前环境缺口也已经被连续记录，不需要后续重复排查同样的问题

#### 这一步为什么重要

- 重构过程中最容易反复浪费时间的部分之一，就是环境噪音和重复定位同一类阻塞
- 先把这些本地临时痕迹收敛掉，后面每一步会更干净、更可追踪

#### 下一步计划

下一步优先考虑以下动作：

1. 在具备 `Nacos + Redis` 环境后，优先完成 `trend-trading-backtest-view` 的真实联调验证
2. 如果暂时无法补齐环境，则继续把 `trend-trading-backtest-service` 纳入同类试点样板
3. 继续弱化 `index-config-server` 与 `RabbitMQ` 在本地联调中的必要性

### 2026-03-17 - 阶段 16：trend-trading-backtest-service 接入 Nacos 双试点入口

#### 本阶段目标

- 在当前环境仍无法完成真实联调的情况下，继续把核心业务服务纳入同类迁移样板
- 让 `trend-trading-backtest-service` 同时具备 `Nacos Discovery` 与 `Nacos Config` 试点入口
- 保持本阶段只处理注册中心与配置中心，不把 `Feign/Hystrix` 迁移混进来

#### 已完成事项

1. 调整了 `trend-trading-backtest-service` 的依赖
   - 增加兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Discovery` 依赖
   - 增加兼容当前 `Spring Boot 2.0.x / Finchley` 的 `Nacos Config` 依赖
   - 保留原有 `Feign`、`Hystrix`、`Eureka` 依赖，确保默认模式不受影响

2. 增加了 Nacos 运行配置
   - 新增 `application-nacos.yml`
   - 在 `nacos` profile 下配置 `Nacos Discovery` 地址
   - 在 `nacos` profile 下关闭 `Eureka client`
   - 保留当前 `Feign + Hystrix` 开关与 Actuator 配置

3. 增加了 Nacos Config 引导文件
   - 新增 `bootstrap-nacos.yml`
   - 指定读取 `trend-trading-backtest-service-dev.yaml` 作为试点 Data ID

4. 调整了启动类
   - 增加 `nacos` profile 启动识别逻辑
   - 默认模式下仍检查 `Eureka` 端口
   - `nacos` 模式下改为检查 `Nacos` 端口

5. 调整了服务模板
   - 修改 `infra/nacos-config/templates/trend-trading-backtest-service-dev.yaml`
   - 去掉不应放在 Data ID 内容里的 `Nacos` 连接配置
   - 在模板中显式关闭 `Eureka client`

6. 更新了迁移记录
   - 在退场方案文档中补充 `trend-trading-backtest-service` 的当前试点状态
   - 明确它后续仍需单独处理 `Feign -> HTTP Service Clients` 与 `Hystrix -> Resilience4j`

7. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `trend-trading-backtest-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在除了市场数据链路和视图服务之外，核心回测服务也已经具备了面向 `Nacos` 的双试点入口。

这意味着当前仓库中的主要业务路径，已经基本都有了“默认旧体系可运行、`nacos` profile 可切换”的代码基础。

#### 这一步为什么重要

- `trend-trading-backtest-service` 是核心业务服务，不能等到最后才开始补迁移入口
- 先把注册中心和配置中心入口补齐，后面再处理远程调用和容错替换会更清晰
- 这也让后续真实联调时，不需要再回头补这个服务的基础接入能力

#### 下一步计划

下一步优先考虑以下动作：

1. 在具备 `Nacos + Redis` 环境后，优先验证关键链路服务的 `nacos` profile 启动行为
2. 继续评估 `trend-trading-backtest-service` 的 `Feign + Hystrix` 替换切入点
3. 开始考虑 `index-config-server` 与 `eureka-server` 的“弱依赖运行”策略

### 2026-03-17 - 阶段 17：修正回测服务误用 Hystrix fallback 的问题

#### 本阶段目标

- 修正 `trend-trading-backtest-service` 中一个会影响真实调用行为的隐藏问题
- 避免后续联调时，回测服务始终使用兜底假数据而不走真实远程调用
- 为后续 `Feign -> HTTP Service Clients` 与 `Hystrix -> Resilience4j` 迁移打下正确的当前行为基线

#### 已完成事项

1. 调整了 `BackTestService` 的客户端注入方式
   - 去掉了对 `indexDataClientFeignHystrix` fallback Bean 的显式 `@Qualifier`
   - 改为通过构造器注入 `IndexDataClient`
   - 让服务默认依赖真正的客户端代理，而不是直接依赖兜底实现

2. 更新了迁移记录
   - 在退场方案文档中补充该问题说明
   - 明确这是一个当前行为修正，而不只是未来重构准备

3. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `trend-trading-backtest-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务的远程市场数据读取路径，已经不再默认绕过真实客户端。

这意味着后续无论是做旧体系联调，还是做 `Nacos` 试点联调，观察到的调用行为都会更接近真实生产路径。

#### 这一步为什么重要

- 如果一直注入 fallback Bean，很多问题会被“假成功”掩盖掉
- 先把当前行为修正到合理状态，后续替换通信组件时才有可信的比较基线
- 这也是比继续堆新入口更优先的真实缺陷修复

#### 下一步计划

下一步优先考虑以下动作：

1. 在具备 `Nacos + Redis` 环境后，优先验证回测链路的真实远程调用行为
2. 继续评估 `trend-trading-backtest-service` 的 `Feign + Hystrix` 替换切入点
3. 开始考虑如何让旧 `Feign` 客户端与未来新客户端并行存在

### 2026-03-17 - 阶段 18：为回测服务提炼远程数据访问接缝

#### 本阶段目标

- 为后续 `Feign -> Spring HTTP Service Clients` 替换先做结构层准备
- 在不改动回测计算逻辑的前提下，把远程市场数据访问从业务服务里抽出来
- 继续保持“先铺替换接缝，再切底层实现”的渐进式迁移方式

#### 已完成事项

1. 提炼了远程数据访问抽象
   - 新增 `IndexDataGateway` 接口
   - 用于承载回测服务读取指数数据的统一入口

2. 增加了当前 Feign 实现适配器
   - 新增 `FeignIndexDataGateway`
   - 由它负责调用现有 `IndexDataClient`
   - 保持当前运行行为不变

3. 调整了 `BackTestService`
   - 不再直接依赖 `Feign` 客户端接口
   - 改为依赖 `IndexDataGateway`
   - 让回测业务逻辑与底层远程调用实现解耦

4. 更新了迁移记录
   - 在退场方案文档中补充这一替换接缝
   - 明确后续替换 `Feign` 时不需要再改核心回测计算逻辑

5. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `trend-trading-backtest-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务已经不再把远程调用方式直接写死在业务服务里。

这意味着后续如果要：

- 并行保留旧 `Feign` 实现
- 新增基于 HTTP Service Clients 的实现
- 或替换为别的远程访问方式

都可以先在接缝层完成，而不用直接改动回测计算逻辑。

#### 这一步为什么重要

- 真正难切的往往不是依赖本身，而是依赖与业务逻辑缠得太紧
- 先把接缝提炼出来，可以显著降低后续替换风险
- 这一步也让我们从“只补新入口”开始进入“为现代化替换做结构准备”的阶段

#### 下一步计划

下一步优先考虑以下动作：

1. 继续为 `Hystrix` fallback 提炼接缝，准备替换为更独立的降级策略
2. 在具备环境后，验证回测链路的真实远程调用行为
3. 评估是否要为新客户端实现预留并行 Bean 结构

### 2026-03-17 - 阶段 19：为回测服务提炼独立的降级策略接缝

#### 本阶段目标

- 继续降低 `Hystrix` 与业务兜底逻辑之间的耦合
- 让当前回测服务的降级返回值不再直接写死在 Feign fallback 适配类里
- 为后续 `Hystrix -> Resilience4j` 替换保留可复用的降级策略实现

#### 已完成事项

1. 提炼了独立的降级网关
   - 新增 `IndexDataFallbackGateway`
   - 由它统一负责构造兜底的指数数据返回值

2. 调整了当前 Feign fallback 适配类
   - `IndexDataClientFeignHystrix` 不再自己拼装兜底数据
   - 改为委托给 `IndexDataFallbackGateway`
   - 让当前 Feign/Hystrix 适配层只承接旧框架接入责任

3. 更新了迁移记录
   - 在退场方案文档中补充这一层降级接缝
   - 明确后续替换 `Hystrix` 时可以复用现有兜底策略

4. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `trend-trading-backtest-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务里已经同时具备：

- 远程调用接缝
- 独立的降级策略接缝

这意味着后续替换 `Feign` 或 `Hystrix` 时，都不需要再直接改动回测业务逻辑和兜底返回细节。

#### 这一步为什么重要

- 框架替换最容易拖慢进度的点，是远程调用、降级逻辑和业务代码混在一起
- 现在把降级策略也拆出来，后续每一步替换都会更小、更稳
- 这一步是为真正进入 `Hystrix` 替换做最后一层结构铺垫

#### 下一步计划

下一步优先考虑以下动作：

1. 评估如何在不删除旧 `Feign` 客户端的前提下，并行引入新的 HTTP 调用实现
2. 在具备环境后，验证回测链路的真实远程调用与兜底行为
3. 开始准备 `Hystrix` 退出后的最小兼容路径

### 2026-03-17 - 阶段 20：为回测服务预留并行 HTTP 调用实现

#### 本阶段目标

- 在不删除旧 `Feign` 客户端的前提下，为回测服务预留新的 HTTP 调用实现入口
- 继续采用“默认行为不变、替换路径先并行存在”的方式推进通信层迁移
- 为后续 `Feign -> Spring HTTP Service Clients` 真正落地前，先把切换结构准备好

#### 已完成事项

1. 增加了可切换的 HTTP 调用实现
   - 新增 `HttpIndexDataGateway`
   - 通过 `RestTemplateBuilder` 按配置调用 `/data/{code}` 接口
   - 当前仅在 `backtest.remote.index-data.mode=http` 时启用

2. 调整了现有 Feign 适配器
   - 为 `FeignIndexDataGateway` 增加条件装配
   - 默认仍在 `feign` 模式下启用
   - 保持当前运行路径不变

3. 增加了并行切换配置
   - 在 `application.yml`、`application-nacos.yml` 和
     `infra/nacos-config/templates/trend-trading-backtest-service-dev.yaml`
     中新增：
     - `backtest.remote.index-data.mode`
     - `backtest.remote.index-data.http.base-url`
   - 当前默认值为 `feign`

4. 更新了迁移记录
   - 在退场方案文档中补充“已预留并行 HTTP 调用实现入口”的状态
   - 明确后续可以在不删除旧 Feign 的情况下做对照切换

5. 完成了本地编译验证
   - 使用本地临时 Maven 工具对 `trend-trading-backtest-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务已经具备三层替换准备：

- 远程调用接缝
- 独立降级策略接缝
- 并行 HTTP 实现入口

这意味着后续真正替换 `Feign` 时，不再需要“一步到位硬切”，而可以先通过配置切换对照两条实现路径。

#### 这一步为什么重要

- 直接删除旧 Feign 再换新实现，风险高且不利于回归比较
- 先让新旧实现并行存在，后面更容易验证行为差异
- 这一步也是最接近“现代化调用链替换”但又不破坏当前老体系的做法

#### 下一步计划

下一步优先考虑以下动作：

1. 继续准备 `Hystrix` 退出后的最小兼容路径
2. 在环境具备后，验证 `feign` 与 `http` 两种模式的行为差异
3. 再决定是否继续把同类接缝推广到其他调用方

### 2026-03-18 - 阶段 21：为回测服务补齐 Hystrix 退场兼容门面

#### 本阶段目标

- 继续沿着 `trend-trading-backtest-service` 的通信层迁移推进
- 不直接删除 `Hystrix`，先把“失败后如何兜底”从 `Feign` 专属 fallback 再向外提一层
- 让当前已经存在的 `feign/http` 双实现都能复用同一套降级策略，为后续退出 `Hystrix` 减少耦合

#### 已完成事项

1. 调整了 `Feign` 客户端定义
   - 去掉了 `IndexDataClient` 上对 `IndexDataClientFeignHystrix` 的绑定
   - 不再把兜底逻辑固定在 `Feign + Hystrix` 适配层中

2. 提炼了独立的传输层接口
   - 新增 `IndexDataTransportGateway`
   - 让 `Feign` 与并行 `HTTP` 实现都只负责“如何发起远程调用”

3. 增加了统一的降级门面
   - 新增 `ResilientIndexDataGateway`
   - 由它统一包裹当前启用中的传输实现
   - 远程调用异常时，统一委托给现有 `IndexDataFallbackGateway`
   - 当前默认开启 `backtest.remote.index-data.fallback.enabled=true`

4. 同步了配置模板
   - 更新 `application.yml`
   - 更新 `application-nacos.yml`
   - 更新 `infra/nacos-config/templates/trend-trading-backtest-service-dev.yaml`
   - 让默认运行模式与未来 `Nacos Config` 模板保持一致

5. 完成了本地编译验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务的远程调用链已经进一步演进为三层职责：

- 传输实现负责调用远端
- 统一门面负责异常兜底
- 独立降级网关负责构造 fallback 返回值

这意味着后续无论继续保留 `Feign`，还是切到并行 `HTTP` 实现，甚至逐步退出 `Hystrix`，都不需要再把兜底逻辑绑回某一个具体通信框架。

#### 这一步为什么重要

- 如果兜底能力继续绑在 `Feign` 的 `fallback` 上，后面去掉 `Hystrix` 时仍然会牵一发而动全身
- 先把降级门面抽成调用方式无关的结构，后续替换 `Feign` 或 `Hystrix` 都可以分步进行
- 这一步属于“保留旧体系可运行，同时继续削弱旧框架耦合”的典型渐进式迁移动作

#### 下一步计划

下一步优先考虑以下动作：

1. 为回测服务继续准备 `Hystrix` 退出后的最小开关路径，例如让旧熔断能力可按配置逐步弱化
2. 在环境具备后，对比验证 `feign/http` 两种模式在统一降级门面下的行为
3. 再决定是否把同类调用门面推广到其他存在远程依赖的服务

### 2026-03-18 - 阶段 22：为回测服务增加 Hystrix 渐退开关

#### 本阶段目标

- 把回测服务当前仍保留的 `Feign + Hystrix` 调用链继续收口
- 不直接移除 `Hystrix` 依赖，而是先补一个可配置的渐退开关
- 让后续关闭旧熔断能力时，仍能复用前一步已经提炼出的统一降级门面

#### 已完成事项

1. 收口了 `Hystrix` 开关配置
   - 将 `feign.hystrix.enabled` 改为引用
     `backtest.remote.index-data.feign.hystrix-enabled`
   - 默认值仍保持为 `true`
   - 确保当前默认运行行为不变

2. 同步了多套运行配置
   - 更新 `application.yml`
   - 更新 `application-nacos.yml`
   - 更新 `infra/nacos-config/templates/trend-trading-backtest-service-dev.yaml`
   - 让本地默认模式、`nacos` 模式和未来导入 `Nacos Config` 的模板保持一致

3. 固化了迁移意图
   - 在退场方案文档中补充说明
   - 明确后续可以先按配置关闭 `Feign` 的 `Hystrix` 包装，再继续推进容错框架替换

4. 完成了本地编译验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务已经具备了更清晰的两层渐进式开关：

- `backtest.remote.index-data.mode`
  控制远程调用实现走 `feign` 还是 `http`
- `backtest.remote.index-data.feign.hystrix-enabled`
  控制旧 `Feign` 路径是否继续启用 `Hystrix` 包装

这意味着后续即使先关闭 `Hystrix`，回测服务仍然可以通过统一降级门面维持当前兜底策略，而不需要立刻同步切换到新的容错框架。

#### 这一步为什么重要

- 渐进式迁移里，最稳的方式不是“一次删掉旧能力”，而是先把旧能力变成可控开关
- 现在先把 `Hystrix` 是否启用收口到服务自身配置里，后面做对照验证会更容易
- 这一步也把“通信实现切换”和“熔断能力切换”拆成了两个独立维度，减少后续联调耦合

#### 下一步计划

下一步优先考虑以下动作：

1. 为回测服务补一组最小回归测试，验证统一降级门面在异常场景下的行为
2. 在具备环境后，对比验证 `Hystrix` 开关打开/关闭时的运行差异
3. 再决定是否开始引入 `Resilience4j` 的最小试点入口

### 2026-03-18 - 阶段 23：为回测服务补齐统一降级门面回归测试

#### 本阶段目标

- 为刚刚完成的回测服务通信层解耦补一组最小回归保护
- 重点验证统一降级门面在“正常返回 / 远程异常后兜底 / 关闭兜底后抛错”三种场景下的行为
- 顺手解决当前测试执行链路里的环境兼容问题，避免后续每次都重复排查

#### 已完成事项

1. 替换了占位测试
   - 删除了原有的 `AppTest`
   - 新增 `ResilientIndexDataGatewayTest`
   - 让测试目标从“占位真值断言”切换为当前实际迁移中的关键门面行为

2. 补充了统一降级门面的三条最小回归测试
   - 验证远程调用成功时直接返回传输层结果
   - 验证远程调用抛出异常且开启兜底时返回 fallback 数据
   - 验证关闭兜底时继续向上抛出异常

3. 修复了当前测试执行环境问题
   - 本机使用 `JDK 17`
   - 当前老项目使用的 `maven-surefire-plugin` 在 fork 子 JVM 时出现兼容性问题
   - 已在 `trend-trading-backtest-service/pom.xml` 中将 `Surefire` 配置为 `forkCount=0`
   - 避免后续重复依赖命令行参数绕过该问题

4. 完成了本地测试验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 执行了 `test`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务除了已经具备：

- 远程调用接缝
- 独立降级策略接缝
- 并行 HTTP 实现入口
- Hystrix 渐退开关

也终于有了第一组直接覆盖“统一降级门面”的自动化回归测试。

这意味着后续继续关闭 `Hystrix`、切换 `feign/http` 模式，或者引入新的容错实现时，都有最小行为基线可以对照。

#### 这一步为什么重要

- 没有测试保护时，渐进式迁移很容易在下一步把前一步刚拆好的接缝重新弄坏
- 这组测试虽然小，但刚好锁住了当前最关键的迁移资产：统一降级门面
- 同时把 `Surefire + JDK 17` 的兼容问题一次性固化修掉，也能减少后续机械性环境排查

#### 下一步计划

下一步优先考虑以下动作：

1. 继续为回测服务评估 `Resilience4j` 的最小试点接入点
2. 在统一降级门面保持不变的前提下，尝试为 `http` 模式补充更贴近真实调用的测试
3. 再决定是否开始弱化模块中的 `Hystrix` 注解与依赖存在感

### 2026-03-18 - 阶段 24：为回测服务预留 Resilience4j 试点入口

#### 本阶段目标

- 在不移除现有 `Hystrix` 依赖的前提下，为回测服务增加新的容错试点入口
- 保持当前默认运行行为不变，只把 `Resilience4j` 作为可按配置启用的并行保护层
- 继续遵循“先并行、再对照、最后替换”的渐进式迁移方式

#### 已完成事项

1. 增加了最小 `Resilience4j` 依赖
   - 在 `trend-trading-backtest-service/pom.xml` 中引入 `resilience4j-circuitbreaker`
   - 当前只使用其核心库，不引入更大范围的框架替换

2. 提炼了远程调用保护层接口
   - 新增 `IndexDataCallGuard`
   - 让回测服务里的调用保护方式与具体远程调用实现进一步解耦

3. 增加了两种可切换的保护层实现
   - 新增 `DirectIndexDataCallGuard`
   - 新增 `Resilience4jIndexDataCallGuard`
   - 默认仍走直接调用保护层
   - 仅在 `backtest.remote.index-data.resilience4j.enabled=true` 时启用 `Resilience4j`

4. 调整了统一降级门面
   - `ResilientIndexDataGateway` 不再直接调用传输层
   - 改为先经过新的调用保护层，再进入现有统一兜底逻辑
   - 让后续切换容错方式时不需要再改业务使用入口

5. 同步了配置与测试
   - 更新 `application.yml`
   - 更新 `application-nacos.yml`
   - 更新 `infra/nacos-config/templates/trend-trading-backtest-service-dev.yaml`
   - 调整现有统一降级门面测试以适配新的保护层接口

6. 完成了本地验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 执行了 `test`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务已经具备了更完整的并行迁移结构：

- 远程调用实现可在 `feign/http` 之间切换
- 旧 `Hystrix` 开关可按配置渐退
- 新 `Resilience4j` 保护层可按配置试点启用
- 统一降级门面继续承接异常后的 fallback 逻辑

这意味着后续如果要比较旧容错路径和新容错路径，不需要再直接重写业务调用入口，只需要围绕保护层配置与行为做对照验证。

#### 这一步为什么重要

- `Hystrix -> Resilience4j` 最怕一步硬切，因为那会同时影响调用包装、异常行为和兜底路径
- 先让 `Resilience4j` 以最小方式并行接入，可以把风险压缩在一个更小的边界内
- 这一步也让“远程调用实现切换”和“容错机制切换”继续保持独立推进

#### 下一步计划

下一步优先考虑以下动作：

1. 为 `Resilience4j` 保护层补一组最小行为测试，验证开关开启时的异常包装行为
2. 继续弱化回测服务中对 `Hystrix` 注解和依赖的存在感
3. 再决定是否开始为监控侧补 Prometheus 指标暴露入口

### 2026-03-18 - 阶段 25：补齐 Resilience4j 保护层测试并弱化 Hystrix 痕迹

#### 本阶段目标

- 为刚接入的 `Resilience4j` 调用保护层补最小自动化验证
- 继续清理回测服务中仍然可见的 `Hystrix` 时代显式痕迹
- 保持旧调用链仍可运行，但让框架替换继续朝“默认入口统一、旧框架逐步退到配置层”推进

#### 已完成事项

1. 补充了 `Resilience4j` 保护层测试
   - 在 `ResilientIndexDataGatewayTest` 中新增保护层相关用例
   - 验证开启 `Resilience4j` 后，首次远程异常仍会进入统一兜底
   - 验证达到失败阈值后，后续调用会被短路，体现新保护层已经真实生效

2. 弱化了启动类中的旧框架痕迹
   - 从 `TrendTradingBackTestServiceApplication` 中移除了 `@EnableCircuitBreaker`
   - 当前模块内已不再保留显式的 `Hystrix` 注解型入口

3. 更新了迁移记录
   - 在退场方案文档中补充“已移除启动类显式熔断注解”的状态
   - 明确当前 `Hystrix` 的保留范围进一步收缩到依赖与配置层

4. 完成了本地测试验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 执行了 `test`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务在容错替换这条线上已经进一步收敛为：

- 新的 `Resilience4j` 保护层有了最小行为测试
- 旧的 `Hystrix` 入口已不再通过启动类注解显式暴露
- 统一降级门面继续稳定承接异常后的兜底逻辑

这意味着后续继续清理 `Hystrix` 依赖时，风险会更集中在旧 `Feign` 路径本身，而不是还散落在启动配置和门面入口上。

#### 这一步为什么重要

- 新能力只进代码不进测试，后面很难放心继续收缩旧框架
- 启动类里的显式 `Hystrix` 注解虽然不大，但会持续强化旧架构心智
- 先把测试和入口痕迹都收掉一部分，后面真正删依赖会更稳

#### 下一步计划

下一步优先考虑以下动作：

1. 继续评估 `spring-cloud-starter-netflix-hystrix` 在回测服务中的剩余必要性
2. 在保持 `Feign` 可用的前提下，尝试进一步弱化 `feign.hystrix.enabled` 的默认地位
3. 再决定是否开始补 Prometheus 指标暴露入口，为 `Hystrix Dashboard/Turbine` 退场铺路

### 2026-03-18 - 阶段 26：弱化回测服务中 Hystrix 的默认地位

#### 本阶段目标

- 在不直接删除 `Hystrix` 依赖的前提下，继续降低它在回测服务中的默认存在感
- 让旧 `Feign + Hystrix` 包装从“默认开启”退到“显式配置才开启”
- 保持统一降级门面仍然可用，避免因为默认值调整而失去兜底能力

#### 已完成事项

1. 调整了默认开关方向
   - 将 `feign.hystrix.enabled` 的默认值从引用 `true` 调整为引用 `false`
   - 将 `backtest.remote.index-data.feign.hystrix-enabled` 在默认配置中改为 `false`
   - 让回测服务默认更接近“无 Hystrix 包装”的新路径

2. 同步了多套运行配置
   - 更新 `application.yml`
   - 更新 `application-nacos.yml`
   - 更新 `infra/nacos-config/templates/trend-trading-backtest-service-dev.yaml`
   - 确保本地默认模式、`nacos` 模式和未来 `Nacos Config` 模板保持一致

3. 更新了迁移记录
   - 在退场方案文档中补充“Hystrix 默认值已下调为关闭”的状态
   - 明确旧熔断包装现在只在显式配置时启用

4. 完成了本地验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 执行了 `test`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务在容错迁移这条线上已经进一步变成：

- 默认情况下不再优先依赖 `Hystrix` 包装
- 需要时仍可通过配置显式打开旧路径
- 新的统一降级门面与 `Resilience4j` 保护层继续保留

这意味着后续如果要真正评估移除 `spring-cloud-starter-netflix-hystrix`，风险已经被进一步压缩到更小范围。

#### 这一步为什么重要

- 渐进式迁移里，默认值往往比代码注解更能体现真实架构方向
- 先把默认开关从旧框架移开，能更早暴露潜在兼容问题，也能减少后续误用旧路径
- 这一步比直接删依赖更稳，因为它保留了回退空间

#### 下一步计划

下一步优先考虑以下动作：

1. 继续评估 `spring-cloud-starter-netflix-hystrix` 在回测服务中的剩余必要性
2. 尝试验证移除该依赖后，当前 `Feign` 与统一降级门面是否仍可成立
3. 再决定是否开始为 Prometheus 指标暴露补基础接入

### 2026-03-18 - 阶段 27：移除回测服务中的 Hystrix 模块依赖

#### 本阶段目标

- 验证回测服务是否已经真正具备脱离 `Hystrix` 模块依赖独立运行的条件
- 把上一阶段已经收缩到“默认关闭”的旧配置路径彻底删掉
- 用实际编译和测试结果确认当前 `Feign + 统一降级门面 + Resilience4j` 结构已经足以承接回测服务

#### 已完成事项

1. 移除了模块级 `Hystrix` 依赖
   - 从 `trend-trading-backtest-service/pom.xml` 中删除 `spring-cloud-starter-netflix-hystrix`
   - 让回测服务不再显式依赖 `Hystrix` starter

2. 删除了旧配置开关
   - 从 `application.yml` 中移除 `feign.hystrix.enabled`
   - 从 `application-nacos.yml` 中移除 `feign.hystrix.enabled`
   - 从两份配置与 `Nacos Config` 模板中移除 `backtest.remote.index-data.feign.hystrix-enabled`
   - 避免后续继续误以为该模块仍存在可切换的旧熔断包装路径

3. 更新了迁移记录
   - 在退场方案文档中补充“回测服务已移除 Hystrix 模块依赖”的状态
   - 明确当前重点已经转向继续完善 `Resilience4j` 与后续监控替代路径

4. 完成了本地验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 执行了 `test`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务在通信与容错迁移这条线上已经进一步演进为：

- 远程调用实现仍可在 `feign/http` 之间切换
- 统一降级门面继续负责异常后的兜底逻辑
- `Resilience4j` 保护层可按配置启用
- 模块本身已经不再显式依赖 `Hystrix` starter

这意味着 `trend-trading-backtest-service` 已经成为当前仓库里第一个在代码层和模块依赖层都明显退出 `Hystrix` 的业务服务试点。

#### 这一步为什么重要

- 只有把旧依赖真正从模块里删掉，才能证明前面提炼的接缝不是“看起来可替换”
- 这一步让后续 `index-hystrix-dashboard` / `index-turbine` 的退场判断更有依据
- 同时它也为后面补 Prometheus 指标暴露提供了更清晰的替代目标

#### 下一步计划

下一步优先考虑以下动作：

1. 开始为回测服务补最小 Prometheus 指标暴露入口
2. 继续评估 `index-hystrix-dashboard` 与 `index-turbine` 的退场前提是否已满足一部分
3. 再决定是否把同类 `Hystrix` 退出路径推广到其他仍有远程依赖的模块

### 2026-03-18 - 阶段 28：为回测服务补最小 Prometheus 指标入口

#### 本阶段目标

- 为已经退出 `Hystrix` 模块依赖的回测服务补最小观测替代入口
- 不一次性搭完整监控体系，先让服务具备被 `Prometheus` 抓取的基础能力
- 为后续 `index-hystrix-dashboard` 与 `index-turbine` 的退场准备第一个业务服务试点

#### 已完成事项

1. 增加了 `Prometheus` registry 依赖
   - 在 `trend-trading-backtest-service/pom.xml` 中引入 `micrometer-registry-prometheus`

2. 调整了服务监控配置
   - 更新 `application.yml`
   - 更新 `application-nacos.yml`
   - 更新 `infra/nacos-config/templates/trend-trading-backtest-service-dev.yaml`
   - 开启 `management.endpoint.prometheus.enabled=true`
   - 将暴露端点收口为 `health,info,prometheus`
   - 增加统一的 `application` 指标标签

3. 更新了退场方案文档
   - 在基础设施退场文档中补充回测服务已具备最小 `Prometheus` 暴露能力
   - 明确它可以作为后续替代 `Hystrix Dashboard / Turbine` 的观测试点

4. 完成了本地验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 执行了 `test`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务除了已经完成通信层与容错层的渐进迁移之外，也开始具备新的观测出口：

- `/actuator/health`
- `/actuator/info`
- `/actuator/prometheus`

这意味着后续如果继续推进 `Prometheus / Grafana`，已经有了一个可以先接入抓取配置的真实业务服务样板。

#### 这一步为什么重要

- 只退出旧熔断体系但不补新的观测出口，会让后续退场缺少替代抓手
- 先从一个核心业务服务补最小指标入口，比直接改整套监控基础设施更稳
- 这一步把“容错替换”和“观测替换”正式接上了

#### 下一步计划

下一步优先考虑以下动作：

1. 为仓库补一个最小 `Prometheus` 抓取配置样板
2. 继续评估 `index-hystrix-dashboard` 与 `index-turbine` 的退场条件
3. 再决定是否把同类指标暴露入口推广到 `gateway-service` 或其他业务服务

### 2026-03-18 - 阶段 29：为 Prometheus 补最小抓取配置样板

#### 本阶段目标

- 把“服务已暴露 `/actuator/prometheus`”推进到“仓库里已有可运行的抓取样板”
- 继续沿用现有 `infra/docker-compose` 目录结构，不额外引入新的文档层噪音
- 为后续替代 `index-hystrix-dashboard` 与 `index-turbine` 提供第一个可落地的本地监控入口

#### 已完成事项

1. 新增了本地 `Prometheus` 运行目录
   - 创建 `infra/docker-compose/prometheus`

2. 增加了最小 `docker-compose` 文件
   - 新增 `infra/docker-compose/prometheus/docker-compose.yml`
   - 使用 `prom/prometheus` 官方镜像
   - 暴露 `9090` 端口
   - 挂载本地抓取配置与数据目录

3. 增加了最小抓取配置
   - 新增 `infra/docker-compose/prometheus/prometheus.yml`
   - 当前先抓取 `trend-trading-backtest-service`
   - 使用 `host.docker.internal:8051`
   - 抓取路径为 `/actuator/prometheus`

4. 更新了迁移记录
   - 在退场方案文档中补充当前 `Prometheus` 本地抓取样板已入库
   - 明确后续可以在同一配置里继续扩展更多服务抓取目标

#### 当前结果

现在仓库里关于监控替代路径已经从“只有服务端暴露指标”进一步推进到：

- 服务具备 `/actuator/prometheus`
- 本地 `Prometheus` 运行入口已入库
- 最小抓取配置已能覆盖回测服务试点

这意味着后续只要本机具备 Docker 环境，就可以直接验证从业务服务到 `Prometheus` 的指标抓取链路。

#### 这一步为什么重要

- 如果只有应用暴露指标而没有抓取配置，监控替代方案仍然停留在半成品阶段
- 先把最小抓取样板落库，后续扩展其他服务就会更快
- 这一步也让 `Hystrix Dashboard / Turbine` 的退场条件开始从“概念替代”转向“已有运行入口”

#### 下一步计划

下一步优先考虑以下动作：

1. 为 `gateway-service` 或另一个已迁移服务补最小 `Prometheus` 指标暴露入口
2. 继续评估是否要同步补 `Grafana` 本地样板
3. 再决定何时开始正式弱化 `index-hystrix-dashboard` 与 `index-turbine` 的存在感

### 2026-03-18 - 阶段 30：为 gateway-service 补最小 Prometheus 指标入口

#### 本阶段目标

- 把监控替代样板从单个业务服务扩展到入口层
- 让 `gateway-service` 具备与回测服务一致的最小 `Prometheus` 暴露能力
- 同步把本地抓取配置扩展到网关入口，增强 `Hystrix Dashboard / Turbine` 退场前的替代可见性

#### 已完成事项

1. 增加了 `Prometheus` registry 依赖
   - 在 `gateway-service/pom.xml` 中引入 `micrometer-registry-prometheus`

2. 调整了网关监控配置
   - 更新 `gateway-service/src/main/resources/application.yml`
   - 更新 `gateway-service/src/main/resources/application-nacos.yml`
   - 更新 `infra/nacos-config/templates/gateway-service-dev.yaml`
   - 开启 `management.endpoint.prometheus.enabled=true`
   - 将暴露端点收口为 `health,info,prometheus`
   - 增加统一的 `application` 指标标签

3. 扩展了本地抓取样板
   - 更新 `infra/docker-compose/prometheus/prometheus.yml`
   - 新增对 `gateway-service` 的抓取目标 `host.docker.internal:8032`
   - 让本地 `Prometheus` 样板同时覆盖入口层和核心业务服务

4. 更新了迁移记录
   - 在退场方案文档中补充 `gateway-service` 已具备最小 `Prometheus` 暴露能力
   - 明确它已经成为入口层的监控替代试点

5. 完成了本地验证
   - 使用本机 Maven 对 `gateway-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在仓库里的监控替代路径已经不再只覆盖单个业务服务，而是开始形成一条最小观察链：

- `gateway-service` 暴露 `/actuator/prometheus`
- `trend-trading-backtest-service` 暴露 `/actuator/prometheus`
- `infra/docker-compose/prometheus/prometheus.yml` 已同时具备两个抓取目标

这意味着后续如果继续推进 `Prometheus / Grafana`，已经可以从“入口层 + 核心业务服务”两个关键位置开始做联动观察。

#### 这一步为什么重要

- 只抓业务服务还不够，入口层指标同样是网关替代和熔断体系退场的重要观测面
- 先把 `gateway-service` 补齐，后续扩展其他服务时能更有样板价值
- 这一步也让 `gateway-service` 从“新网关试点”进一步走向“新网关 + 新观测入口试点”

#### 下一步计划

下一步优先考虑以下动作：

1. 继续评估是否要补最小 `Grafana` 本地样板
2. 开始整理 `index-hystrix-dashboard` 与 `index-turbine` 的阶段性退场条件
3. 再决定是否把同类指标暴露入口推广到 `index-data-service` 或 `index-codes-service`

### 2026-03-18 - 阶段 31：为 Grafana 补最小本地运行样板

#### 本阶段目标

- 在已有 `Prometheus` 抓取样板的基础上，补最小 `Grafana` 可视化入口
- 继续复用 `infra/docker-compose` 目录结构，不新增独立说明文档
- 让 `Hystrix Dashboard / Turbine` 的替代路径从“有指标、有抓取”进一步推进到“有基础可视化入口”

#### 已完成事项

1. 新增了本地 `Grafana` 运行目录
   - 创建 `infra/docker-compose/grafana`

2. 增加了最小 `docker-compose` 文件
   - 新增 `infra/docker-compose/grafana/docker-compose.yml`
   - 使用 `grafana/grafana` 官方镜像
   - 暴露 `3000` 端口
   - 预置默认账号密码 `admin/admin`
   - 挂载本地数据目录与 provisioning 配置

3. 增加了预置数据源配置
   - 新增 `infra/docker-compose/grafana/provisioning/datasources/prometheus.yml`
   - 默认预置 `Trend Prometheus` 数据源
   - 指向 `http://host.docker.internal:9090`
   - 让本地 `Grafana` 启动后能直接对接仓库内的 `Prometheus` 样板

4. 更新了迁移记录
   - 在退场方案文档中补充 `Grafana` 最小运行样板已入库
   - 明确当前阶段先提供可视化入口，后续再补 dashboard 模板

#### 当前结果

现在仓库里的监控替代路径已经具备三层最小样板：

- 应用暴露 `/actuator/prometheus`
- `Prometheus` 可抓取业务服务与入口层指标
- `Grafana` 有可直接连接 `Prometheus` 的本地运行入口

这意味着后续如果具备 Docker 环境，就可以直接从服务指标一路联通到基础可视化面板，而不需要再从零搭监控入口。

#### 这一步为什么重要

- 只有数据源而没有可视化入口，`Hystrix Dashboard` 的替代叙事还不完整
- 先把最小 `Grafana` 样板落库，后续再补 dashboard 模板会更顺
- 这一步也让监控替代路径第一次形成了“暴露-抓取-展示”的完整最小闭环

#### 下一步计划

下一步优先考虑以下动作：

1. 开始整理 `index-hystrix-dashboard` 与 `index-turbine` 的阶段性退场条件
2. 评估是否要为 `Grafana` 补一份最小 dashboard provisioning 样板
3. 再决定是否把同类指标暴露入口推广到 `index-data-service` 或 `index-codes-service`

### 2026-03-18 - 阶段 32：将旧监控模块移出主构建

#### 本阶段目标

- 在监控替代路径已经具备最小样板后，推进 `index-hystrix-dashboard` 与 `index-turbine` 的阶段性退场
- 先把这两个旧监控模块从父工程主构建中摘除
- 不急着物理删除目录，先保留源码参考和回退空间

#### 已完成事项

1. 调整了父工程模块列表
   - 从根 `pom.xml` 中移除了 `index-hystrix-dashboard`
   - 从根 `pom.xml` 中移除了 `index-turbine`
   - 让这两个旧监控模块不再参与当前主构建

2. 更新了服务迁移矩阵
   - 将 `index-hystrix-dashboard` 标记为“已停止纳入主构建”
   - 将 `index-turbine` 标记为“已停止纳入主构建”
   - 明确当前退场动作已经从“仅有规划”进入“主构建层收缩”阶段

3. 更新了退场方案文档
   - 补充当前已具备的替代条件：
     - 回测服务已退出 `Hystrix` 模块依赖
     - 回测服务与网关已暴露最小 `Prometheus` 指标入口
     - `Prometheus` 与 `Grafana` 最小运行样板已入库
   - 明确本轮已执行“先摘出主构建、后视情况删目录”的阶段性退场动作

4. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在项目里的旧监控体系已经进一步收缩为：

- `index-hystrix-dashboard` 与 `index-turbine` 不再参与主构建
- 新的监控替代样板已覆盖：
  - `trend-trading-backtest-service`
  - `gateway-service`
  - `Prometheus`
  - `Grafana`

这意味着后续继续推进时，主工程已经不再默认依赖旧监控模块，新的替代路径也不再只是文档方案。

#### 这一步为什么重要

- 把旧模块留在父工程里，会持续增加构建噪音，也会让退场动作一直停留在“计划中”
- 先从主构建摘掉，是比直接删目录更稳、但又足够实质的一步
- 这一步也符合当前你的要求：原服务并没有真正启用，优先保证替代链路不出错即可

#### 下一步计划

下一步优先考虑以下动作：

1. 继续把同类最小 `Prometheus` 指标入口推广到 `index-data-service` 或 `index-codes-service`
2. 评估是否要为 `Grafana` 补一份最小 dashboard provisioning 样板
3. 再决定何时物理删除 `index-hystrix-dashboard` 与 `index-turbine` 目录

### 2026-03-18 - 阶段 33：删除旧监控模块源码目录

#### 本阶段目标

- 在两个旧监控模块已经移出主构建后，完成物理退场
- 删除 `index-hystrix-dashboard` 与 `index-turbine` 的源码与模块文件
- 让仓库状态与当前真实替代方案保持一致，避免继续保留失效模块造成误导

#### 已完成事项

1. 删除了 `index-hystrix-dashboard` 模块文件
   - 删除模块 `pom.xml`
   - 删除启动类与工具类
   - 删除 `application.yml`
   - 删除测试占位文件
   - 删除模块级 `.iml`

2. 删除了 `index-turbine` 模块文件
   - 删除模块 `pom.xml`
   - 删除启动类与工具类
   - 删除 `application.yml`
   - 删除测试占位文件
   - 删除模块级 `.iml`

3. 更新了迁移矩阵与退场方案
   - 将 `index-hystrix-dashboard` 标记为“已退场”
   - 将 `index-turbine` 标记为“已退场”
   - 明确当前已经完成“从主构建移除 + 源码目录删除”的阶段性退场

4. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在旧监控体系在仓库中的状态已经进一步收口为：

- `index-hystrix-dashboard` 已退场
- `index-turbine` 已退场
- 替代路径已由：
  - `trend-trading-backtest-service`
  - `gateway-service`
  - `Prometheus`
  - `Grafana`
  组成最小替代样板

这意味着后续围绕监控替代继续推进时，仓库主线已经不再被旧监控模块干扰。

#### 这一步为什么重要

- 既然旧模块已经不进主构建，再继续保留源码目录只会增加认知噪音
- 直接删掉失效模块，能让当前架构状态更清晰
- 这一步也和你这轮的要求一致：原服务没有启用，优先保证替代不出错即可

#### 下一步计划

下一步优先考虑以下动作：

1. 继续把最小 `Prometheus` 指标入口推广到 `index-data-service` 或 `index-codes-service`
2. 评估是否要为 `Grafana` 补最小 dashboard provisioning 样板
3. 再决定是否继续推进其他旧基础设施模块的主构建退场

### 2026-03-18 - 阶段 34：为 index-data-service 补最小 Prometheus 指标入口

#### 本阶段目标

- 把监控替代样板从入口层和回测核心继续扩展到市场数据链路
- 让 `index-data-service` 具备与 `gateway-service`、`trend-trading-backtest-service` 一致的最小 `Prometheus` 暴露能力
- 同步扩展本地 `Prometheus` 抓取样板，让市场数据服务进入最小观察范围

#### 已完成事项

1. 增加了 `Prometheus` registry 依赖
   - 在 `index-data-service/pom.xml` 中引入 `micrometer-registry-prometheus`

2. 调整了服务监控配置
   - 更新 `index-data-service/src/main/resources/application.yml`
   - 更新 `index-data-service/src/main/resources/application-nacos.yml`
   - 更新 `infra/nacos-config/templates/index-data-service-dev.yaml`
   - 开启 `management.endpoint.prometheus.enabled=true`
   - 将暴露端点收口为 `health,info,prometheus`
   - 增加统一的 `application` 指标标签

3. 扩展了本地抓取样板
   - 更新 `infra/docker-compose/prometheus/prometheus.yml`
   - 新增对 `index-data-service` 的抓取目标 `host.docker.internal:8021`
   - 让市场数据链路也进入当前最小抓取范围

4. 更新了迁移记录
   - 在退场方案文档中补充 `index-data-service` 已具备最小 `Prometheus` 暴露能力
   - 明确它已成为市场数据链路的监控替代试点

5. 完成了本地验证
   - 使用本机 Maven 对 `index-data-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在仓库里的监控替代样板已经覆盖三类关键位置：

- 入口层：`gateway-service`
- 核心业务：`trend-trading-backtest-service`
- 市场数据链路：`index-data-service`

这意味着后续不论是继续扩展指标、补 `Grafana` dashboard，还是进一步推动旧监控体系退场，都已经不再只依赖单点试验。

#### 这一步为什么重要

- 市场数据服务是回测链路的上游，只观察网关和回测服务还不够完整
- 先把 `index-data-service` 纳入最小监控样板，能更贴近真实调用链
- 这一步也让“替代不出错”的验证从核心服务扩展到了数据服务

#### 下一步计划

下一步优先考虑以下动作：

1. 继续把同类最小 `Prometheus` 指标入口推广到 `index-codes-service`
2. 评估是否要为 `Grafana` 补最小 dashboard provisioning 样板
3. 再决定是否继续推进其他旧基础设施模块的退场动作

### 2026-03-18 - 阶段 35：为 index-codes-service 补最小 Prometheus 指标入口

#### 本阶段目标

- 继续把监控替代样板推广到市场数据链路中的另一条基础服务
- 让 `index-codes-service` 具备与 `index-data-service` 一致的最小 `Prometheus` 暴露能力
- 让当前本地 `Prometheus` 抓取样板覆盖完整的市场数据查询链路

#### 已完成事项

1. 增加了 `Prometheus` registry 依赖
   - 在 `index-codes-service/pom.xml` 中引入 `micrometer-registry-prometheus`

2. 调整了服务监控配置
   - 更新 `index-codes-service/src/main/resources/application.yml`
   - 更新 `index-codes-service/src/main/resources/application-nacos.yml`
   - 更新 `infra/nacos-config/templates/index-codes-service-dev.yaml`
   - 开启 `management.endpoint.prometheus.enabled=true`
   - 将暴露端点收口为 `health,info,prometheus`
   - 增加统一的 `application` 指标标签

3. 扩展了本地抓取样板
   - 更新 `infra/docker-compose/prometheus/prometheus.yml`
   - 新增对 `index-codes-service` 的抓取目标 `host.docker.internal:8011`
   - 让市场元数据服务也进入当前最小抓取范围

4. 更新了迁移记录
   - 在退场方案文档中补充 `index-codes-service` 已具备最小 `Prometheus` 暴露能力
   - 明确它已成为市场元数据链路的监控替代试点

5. 完成了本地验证
   - 使用本机 Maven 对 `index-codes-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在仓库里的最小监控替代样板已经覆盖：

- 入口层：`gateway-service`
- 核心业务：`trend-trading-backtest-service`
- 市场数据链路：`index-data-service`
- 市场元数据链路：`index-codes-service`

这意味着当前最小替代观察面已经不再是单点，而是覆盖了主要查询链路上的关键服务。

#### 这一步为什么重要

- 市场数据链路通常成对出现，只有 `index-data-service` 进入观察范围还不够完整
- 把 `index-codes-service` 也补齐后，后续无论做 `Grafana` dashboard 还是继续删旧基础设施，依据都会更充分
- 这一步也让当前“替代不出错”的验证进一步从回测链路扩展到上游服务

#### 下一步计划

下一步优先考虑以下动作：

1. 评估是否要为 `Grafana` 补最小 dashboard provisioning 样板
2. 继续推进其他旧基础设施模块的阶段性退场判断
3. 再决定是否把最小指标入口继续推广到 `index-gather-store-service`

### 2026-03-18 - 阶段 36：为 Grafana 补最小 Dashboard 样板

#### 本阶段目标

- 在已有 `Grafana` 运行样板基础上，补齐最小 dashboard provisioning 配置
- 让当前已经接入 `Prometheus` 的关键服务有一个可直接打开的总览面板
- 把监控替代路径从“有数据源”推进到“有基础看板”

#### 已完成事项

1. 增加了 dashboard provider 配置
   - 新增 `infra/docker-compose/grafana/provisioning/dashboards/dashboard-provider.yml`
   - 将 `Trend Invest` 文件夹下的 dashboard 文件纳入自动加载

2. 增加了最小总览面板
   - 新增 `infra/docker-compose/grafana/provisioning/dashboards/json/trend-overview.json`
   - 当前先用 `stat` 面板展示基础存活状态
   - 覆盖服务包括：
     - `gateway-service`
     - `trend-trading-backtest-service`
     - `index-data-service`
     - `index-codes-service`

3. 更新了退场方案文档
   - 补充当前 `Grafana` 已具备 dashboard provisioning 能力
   - 明确最小可视化面板已能覆盖入口层、核心业务和市场数据链路

#### 当前结果

现在仓库里的监控替代样板已经形成更完整的最小闭环：

- 应用暴露 `/actuator/prometheus`
- `Prometheus` 抓取关键服务指标
- `Grafana` 自动加载 `Prometheus` 数据源
- `Grafana` 自动加载服务总览 dashboard

这意味着后续一旦具备 Docker 环境，就可以直接从服务指标进入一个基础可视化总览，而不需要手工创建数据源和面板。

#### 这一步为什么重要

- 只有数据源没有面板，`Grafana` 样板仍然不够“可直接使用”
- 先把最小 dashboard 也预置好，后续继续加更细的业务图表会更顺
- 这一步让旧监控体系的替代路径第一次具备了“开箱即看”的雏形

#### 下一步计划

下一步优先考虑以下动作：

1. 继续评估是否把最小指标入口推广到 `index-gather-store-service`
2. 开始整理 `eureka-server` 与 `index-config-server` 的阶段性主构建退场条件
3. 再决定是否继续推进其他旧基础设施模块的源码退场

### 2026-03-18 - 阶段 37：为 index-gather-store-service 补最小 Prometheus 指标入口

#### 本阶段目标

- 继续把最小监控替代样板推广到市场数据采集链路
- 让 `index-gather-store-service` 具备最基础的 `Prometheus` 暴露能力
- 在不改动它当前老式同步与容错实现的前提下，先补齐新的观测出口

#### 已完成事项

1. 增加了 `Prometheus` registry 依赖
   - 在 `index-gather-store-service/pom.xml` 中引入 `micrometer-registry-prometheus`

2. 调整了服务监控配置
   - 更新 `index-gather-store-service/src/main/resources/application.yml`
   - 开启 `management.endpoint.prometheus.enabled=true`
   - 将暴露端点收口为 `health,info,prometheus`
   - 增加统一的 `application` 指标标签

3. 扩展了本地抓取样板
   - 更新 `infra/docker-compose/prometheus/prometheus.yml`
   - 新增对 `index-gather-store-service` 的抓取目标 `host.docker.internal:8001`
   - 让市场数据采集服务进入当前最小抓取范围

4. 更新了迁移记录
   - 在退场方案文档中补充 `index-gather-store-service` 已具备最小 `Prometheus` 暴露能力
   - 明确它已成为市场数据采集链路的监控替代试点

5. 完成了本地验证
   - 使用本机 Maven 对 `index-gather-store-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在当前仓库内最小监控替代样板已经覆盖：

- 入口层：`gateway-service`
- 核心业务：`trend-trading-backtest-service`
- 市场数据查询：`index-data-service`
- 市场元数据查询：`index-codes-service`
- 市场数据采集：`index-gather-store-service`

这意味着从入口、核心业务到数据采集/查询链路，已经基本都有了新的最小观察出口。

#### 这一步为什么重要

- 只观察查询服务而不观察采集服务，市场数据链路还是不完整
- 先补最小指标入口，不会干扰它当前还比较旧的实现方式
- 这一步也让后续继续退场旧基础设施时，替代样板更有说服力

#### 下一步计划

下一步优先考虑以下动作：

1. 开始整理 `eureka-server` 与 `index-config-server` 的阶段性主构建退场条件
2. 评估是否要继续把最小指标入口推广到 `trend-trading-backtest-view`
3. 再决定是否继续推进其他旧基础设施模块的源码退场

### 2026-03-18 - 阶段 38：将 eureka-server 移出主构建

#### 本阶段目标

- 开始推进核心旧基础设施模块的阶段性退场
- 先选择替代条件相对更成熟的 `eureka-server` 做主构建收缩
- 不急着物理删目录，先让当前主工程默认不再把它纳入构建主线

#### 已完成事项

1. 调整了父工程模块列表
   - 从根 `pom.xml` 中移除了 `eureka-server`
   - 让它不再参与当前主构建

2. 更新了迁移矩阵
   - 将 `eureka-server` 标记为“已停止纳入主构建”
   - 明确当前动作属于“核心旧基础设施开始退出主构建”的阶段

3. 更新了退场方案文档
   - 补充当前可支撑该动作的前提：
     - `index-codes-service`
     - `index-data-service`
     - `gateway-service`
     - `trend-trading-backtest-service`
     - `trend-trading-backtest-view`
     已具备不同程度的 `Nacos Discovery` 试点路径
   - 明确本轮先执行“移出主构建、保留源码目录”的轻量退场动作

4. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在核心旧基础设施退场已经从监控模块扩展到了注册中心模块：

- `index-hystrix-dashboard` 已退场
- `index-turbine` 已退场
- `eureka-server` 已停止纳入主构建

这意味着当前主工程的默认构建主线，已经进一步向新注册中心和新入口样板倾斜。

#### 这一步为什么重要

- 如果一直把 `eureka-server` 保留在主构建里，迁移会长期停留在“新旧都在，但默认仍偏旧体系”
- 先把它摘出主构建，比直接删目录更稳，也能减少当前主线的构建噪音
- 这一步也为后续处理 `index-config-server` 和 `index-zuul-service` 提供了类似操作样板

#### 下一步计划

下一步优先考虑以下动作：

1. 继续整理 `index-config-server` 的主构建退场条件
2. 评估 `trend-trading-backtest-view` 对旧配置中心的剩余依赖是否还能再压缩一步
3. 再决定何时把 `eureka-server` 物理删除

### 2026-03-18 - 阶段 39：将 index-config-server 移出主构建

#### 本阶段目标

- 继续推进核心旧基础设施模块的阶段性退场
- 在保留旧配置体系源码目录的前提下，先把 `index-config-server` 从当前主构建中摘除
- 让主工程默认构建主线进一步向 `Nacos Config` 试点路径收敛

#### 已完成事项

1. 调整了父工程模块列表
   - 从根 `pom.xml` 中移除了 `index-config-server`
   - 让它不再参与当前主构建

2. 更新了迁移矩阵
   - 将 `index-config-server` 标记为“已停止纳入主构建”
   - 明确当前动作属于“旧配置中心开始退出主构建”的阶段

3. 更新了退场方案文档
   - 补充当前可支撑该动作的前提：
     - `index-codes-service`
     - `index-data-service`
     - `gateway-service`
     - `trend-trading-backtest-service`
     已具备不同程度的 `Nacos Config` 试点入口
   - 明确 `trend-trading-backtest-view` 仍保留旧配置链路，但不影响当前先执行主构建收缩
   - 明确本轮先执行“移出主构建、保留源码目录”的轻量退场动作

4. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在核心旧基础设施退场已经继续推进到了配置中心模块：

- `index-hystrix-dashboard` 已退场
- `index-turbine` 已退场
- `eureka-server` 已停止纳入主构建
- `index-config-server` 已停止纳入主构建

这意味着当前主工程的默认构建主线，已经进一步减少了对旧注册中心和旧配置中心模块的直接依赖。

#### 这一步为什么重要

- 如果 `index-config-server` 长期留在主构建里，迁移会持续停留在“新旧并存但默认仍偏旧配置中心”的状态
- 先把它摘出主构建，不会影响保留源码目录作为兜底参考，也能显著减少当前主线的构建噪音
- 这一步也为后续继续处理 `trend-trading-backtest-view` 的旧配置链路提供了更明确的收敛方向

#### 下一步计划

下一步优先考虑以下动作：

1. 继续压缩 `trend-trading-backtest-view` 对 `Config Server + Bus + RabbitMQ` 的剩余依赖
2. 开始评估 `index-zuul-service` 的主构建退场条件
3. 再决定何时物理删除 `index-config-server` 目录

### 2026-03-18 - 阶段 40：压缩 trend-trading-backtest-view 的旧配置链路

#### 本阶段目标

- 继续削弱 `trend-trading-backtest-view` 对 `Config Server + Bus + RabbitMQ` 的直接依赖
- 让该模块默认启动路径进一步收敛到 `Nacos Config`
- 在不影响页面基础功能编译通过的前提下，先完成依赖层和默认配置层减法

#### 已完成事项

1. 收缩了模块依赖
   - 从 `trend-trading-backtest-view/pom.xml` 中移除了 `spring-cloud-starter-config`
   - 从 `trend-trading-backtest-view/pom.xml` 中移除了 `spring-cloud-starter-bus-amqp`
   - 保留 `Nacos Config` 与 `Nacos Discovery` 试点依赖

2. 调整了默认配置入口
   - 更新 `trend-trading-backtest-view/src/main/resources/application.yml`
   - 将默认激活 profile 调整为 `nacos`
   - 增加本地 `version` 默认值，避免因为远程配置未注入导致页面启动失败

3. 弱化了旧配置链路
   - 更新 `trend-trading-backtest-view/src/main/resources/bootstrap.yml`
   - 关闭默认 `Config Client` 和 `Bus` 开关
   - 移除旧的本地 `RabbitMQ` 连接配置
   - 更新启动类中的 profile 识别逻辑，让无显式 profile 时默认按 `nacos` 路径做前置检查

4. 处理了旧刷新入口
   - 更新 `trend-trading-backtest-view/src/main/java/bupt/util/FreshConfigUtil.java`
   - 不再调用 `/actuator/bus-refresh`
   - 改为明确提示旧刷新入口已退役，当前应通过 `Nacos Config` 管理配置

5. 完成了本地验证
   - 使用本机 Maven 对 `trend-trading-backtest-view` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `trend-trading-backtest-view` 已经不再把旧配置中心依赖放在默认路径上：

- 默认启动优先走 `nacos`
- `Config Client + Bus AMQP` 已从模块依赖中移除
- 旧的 `bus-refresh` 工具入口已退役

这意味着后续继续推进 `index-config-server` 物理退场时，当前主线阻力已经进一步降低。

#### 这一步为什么重要

- 如果 `view` 模块继续默认依赖 `Config Server + Bus + RabbitMQ`，旧配置中心即使退出主构建，也仍然会在运行习惯上拖住迁移
- 先把默认入口切到 `nacos`，再逐步删除剩余兼容代码，比一次性硬删更稳
- 这一步也让旧配置中心的退场从“主构建收缩”进一步进入“消费方默认路径收缩”

#### 下一步计划

下一步优先考虑以下动作：

1. 开始评估 `index-zuul-service` 的主构建退场条件
2. 继续检查 `trend-trading-backtest-view` 是否还残留旧配置中心专属代码
3. 再决定何时物理删除 `index-config-server` 目录

### 2026-03-18 - 阶段 41：删除旧 Zuul 网关模块

#### 本阶段目标

- 直接推进旧网关入口的实质退场
- 在确认 `gateway-service` 已承接核心路由和新迁移主线能力后，移除 `index-zuul-service`
- 让当前仓库主线彻底摆脱 `Zuul` 模块噪音

#### 已完成事项

1. 调整了父工程模块列表
   - 从根 `pom.xml` 中移除了 `index-zuul-service`
   - 让它不再参与当前主构建

2. 删除了旧网关模块源码目录
   - 删除 `index-zuul-service/pom.xml`
   - 删除 `index-zuul-service/src/main/java/bupt/IndexZuulServiceApplication.java`
   - 删除 `index-zuul-service/src/main/resources/application.yml`
   - 删除 `index-zuul-service/src/test/java/bupt/AppTest.java`

3. 更新了迁移矩阵与退场方案
   - 将 `index-zuul-service` 标记为“已退场”
   - 明确当前旧网关入口已完成“从主构建移除 + 源码目录删除”的阶段性退场
   - 明确当前由 `gateway-service` 承接核心路由与新迁移主线能力

4. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在旧基础设施退场已经进一步推进到网关模块：

- `index-hystrix-dashboard` 已退场
- `index-turbine` 已退场
- `index-zuul-service` 已退场
- `eureka-server` 已停止纳入主构建
- `index-config-server` 已停止纳入主构建

这意味着当前仓库主线里的旧基础设施噪音已经继续减少，入口层默认方向已经完全收敛到 `gateway-service`。

#### 这一步为什么重要

- 既然旧网关已经不再使用，继续保留 `Zuul` 模块只会让主线状态和真实架构脱节
- 直接删除旧网关，比继续保留一个不会再启用的空壳更符合当前迁移进度
- 这一步也把 `Zuul -> Gateway` 从“新旧并行试点”真正推进到了“旧入口退场”

#### 下一步计划

下一步优先考虑以下动作：

1. 评估是否物理删除 `index-config-server` 与 `eureka-server` 目录
2. 继续检查 `trend-trading-backtest-view` 是否还残留旧配置中心兼容代码
3. 开始转入前端 `Vue 3 + Vite + TS` 或市场数据服务进一步现代化

### 2026-03-18 - 阶段 42：删除旧注册中心与配置中心模块

#### 本阶段目标

- 直接完成 `eureka-server` 与 `index-config-server` 的物理退场
- 让当前仓库主线不再保留未使用的旧基础设施源码空壳
- 把旧基础设施退场状态从“移出主构建”推进到“源码目录删除”

#### 已完成事项

1. 删除了 `eureka-server` 模块源码目录
   - 删除 `eureka-server/pom.xml`
   - 删除 `eureka-server/src/main/java/bupt/EurekaServerApplication.java`
   - 删除 `eureka-server/src/main/resources/application.yml`
   - 删除 `eureka-server/src/test/java/bupt/AppTest.java`

2. 删除了 `index-config-server` 模块源码目录
   - 删除 `index-config-server/pom.xml`
   - 删除 `index-config-server/src/main/java/bupt/IndexConfigServerApplication.java`
   - 删除 `index-config-server/src/main/resources/application.yml`
   - 删除 `index-config-server/src/test/java/bupt/AppTest.java`

3. 更新了迁移矩阵与退场方案
   - 将 `eureka-server` 标记为“已退场”
   - 将 `index-config-server` 标记为“已退场”
   - 明确这两个旧基础设施模块已完成“从主构建移除 + 源码目录删除”的阶段性退场

4. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在旧基础设施退场已经进一步推进到注册中心和配置中心模块：

- `index-hystrix-dashboard` 已退场
- `index-turbine` 已退场
- `index-zuul-service` 已退场
- `eureka-server` 已退场
- `index-config-server` 已退场

这意味着当前仓库主线已经基本移除了旧基础设施模块本身，后续可以更专注地推进业务现代化和前端迁移。

#### 这一步为什么重要

- 既然旧注册中心和旧配置中心都已经不再参与主构建，继续保留源码目录只会增加判断成本
- 直接删掉空壳模块，能让仓库状态与当前真实迁移进度保持一致
- 这一步也让基础设施退场从“缩主构建”正式进入“清旧模块”的后期阶段

#### 下一步计划

下一步优先考虑以下动作：

1. 继续清理 `trend-trading-backtest-view` 残留的旧配置中心兼容代码
2. 开始转入前端 `Vue 3 + Vite + TS` 迁移主线
3. 或继续推进市场数据服务的现代化收敛

### 2026-03-18 - 阶段 43：创建 trend-web 前端试点工程

#### 本阶段目标

- 把前端迁移从文档计划推进到真实工程落库
- 创建独立的 `Vue 3 + Vite + TypeScript` 前端项目 `trend-web`
- 先完成首个回测工作台页面骨架，并继续复用当前网关与后端接口

#### 已完成事项

1. 新建了 `trend-web` 工程
   - 新增 `package.json`
   - 新增 `tsconfig.json`
   - 新增 `vite.config.ts`
   - 配置了 `Vue 3 + Vite + TypeScript`
   - 配置了 `Pinia`、`Vue Router`、`Axios`、`ECharts`

2. 落地了首个回测工作台页面
   - 新增 `src/views/BacktestWorkbench.vue`
   - 新增参数面板、指标卡片、收益曲线图、年度收益图、交易明细表等组件
   - 用原有 `/api-codes/**` 与 `/api-backtest/**` 路径继续对接现有后端
   - 用原生日期输入替代旧的 jQuery datepicker 依赖方式

3. 补齐了前端基础组织结构
   - 新增 `src/router`
   - 新增 `src/stores`
   - 新增 `src/services`
   - 新增 `src/types`
   - 明确新前端将通过 `store + service + component` 结构继续演进

4. 调整了仓库忽略规则与迁移矩阵
   - 在 `.gitignore` 中补充了 `node_modules/` 与 `dist/`
   - 在迁移矩阵中登记 `trend-web` 为新增前端试点

5. 完成了本地前端验证
   - 使用本机 `npm install`
   - 使用本机 `npm run build`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在前端迁移主线已经不再只是“未来要做”：

- `trend-web` 工程已经真实存在
- 新前端已经有可继续演进的回测工作台页面
- 旧的 `trend-trading-backtest-view` 可以开始逐步退到对照和兼容角色

这意味着后续可以正式从“旧页面收缩”进入“新前端逐步接管”的阶段。

#### 这一步为什么重要

- 如果一直只保留 Thymeleaf 页面，前端迁移就会长期停留在口头目标
- 先把新前端工程和首个业务页面建起来，后续拆组件、接状态管理、补交互才有真正落点
- 这一步也让 `Thymeleaf + Vue2 + jQuery -> Vue 3 + Vite + TS` 首次从规划变成工程资产

#### 下一步计划

下一步优先考虑以下动作：

1. 继续把 `trend-trading-backtest-view` 压缩为纯兼容壳层
2. 给 `trend-web` 补接口错误态、加载态和更多交互细节
3. 开始考虑让新前端通过独立部署或静态托管接入当前网关体系

### 2026-03-18 - 阶段 44：让新前端接入当前入口链路

#### 本阶段目标

- 不再让 `trend-web` 只是一个能单独构建的试点工程
- 让 `gateway-service` 能转发新前端入口
- 把 `trend-trading-backtest-view` 收缩成默认跳转新前端、保留 `/legacy` 的兼容壳层

#### 已完成事项

1. 扩展了网关入口
   - 更新 `gateway-service/src/main/resources/application.yml`
   - 更新 `gateway-service/src/main/resources/application-nacos.yml`
   - 更新 `infra/nacos-config/templates/gateway-service-dev.yaml`
   - 新增 `/trend-web/**` 路由，默认转发到 `http://127.0.0.1:5173`

2. 收缩了旧视图服务入口
   - 更新 `trend-trading-backtest-view/src/main/resources/application.yml`
   - 新增 `trend.web.entry-url` 配置，默认指向 `http://127.0.0.1:8032/trend-web/`
   - 更新 `ViewController`
   - 让 `/` 默认重定向到新前端
   - 保留 `/legacy` 继续承载旧 Thymeleaf 页面

3. 调整了新前端基础路径
   - 更新 `trend-web/vite.config.ts`
   - 更新 `trend-web/src/router/index.ts`
   - 让 `trend-web` 以 `/trend-web/` 作为基础访问路径，便于继续挂在 Gateway 下

4. 完成了本地验证
   - 使用本机 Maven 对 `gateway-service` 与 `trend-trading-backtest-view` 执行了 `compile`
   - 使用本机 `npm run build` 验证 `trend-web`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在新前端已经开始真正接入当前入口链路：

- `gateway-service` 可承接 `trend-web` 路径
- `trend-trading-backtest-view` 默认跳转新前端
- 旧页面被收缩到 `/legacy` 兼容入口

这意味着前端迁移已经从“新工程试点”推进到了“新入口开始接管”的阶段。

#### 这一步为什么重要

- 只有前端工程但不接现有入口，迁移仍然停留在并排摆放代码的阶段
- 先让入口链路和兼容壳层到位，后续替换页面内容时风险会明显更低
- 这一步也让 `trend-trading-backtest-view` 的角色更清晰：逐步退场，而不是继续承载默认入口

#### 下一步计划

下一步优先考虑以下动作：

1. 继续给 `trend-web` 补错误态、空态和接口调用体验
2. 继续压缩 `trend-trading-backtest-view` 内部的旧页面依赖
3. 评估是否开始推进市场数据服务合并或 Java 版本升级主线

### 2026-03-18 - 阶段 45：补强 trend-web 可用性并优化前端拆包

#### 本阶段目标

- 继续把新前端从“能访问”推进到“更适合接管”
- 补齐 `trend-web` 的错误态、空态、加载态与重试体验
- 处理前端首版构建时出现的大 chunk 风险，降低后续继续扩展时的打包压力

#### 已完成事项

1. 补强了前端状态体验
   - 更新 `trend-web/src/views/BacktestWorkbench.vue`
   - 增加错误态、空态、加载态展示
   - 增加重试回测、关闭提示、跳转旧页面等操作入口
   - 仅在存在有效结果时才展示指标卡、图表和表格

2. 收敛了状态管理逻辑
   - 更新 `trend-web/src/stores/backtest.ts`
   - 增加 `hasResults`、`hasIndexes`、`lastUpdatedAt`
   - 增加日期范围校验
   - 增加 `resetDateRange` 与 `clearError`
   - 在初始化失败时也保留可继续重试的前端状态

3. 增强了参数面板可操作性
   - 更新 `trend-web/src/components/ParameterPanel.vue`
   - 新增“重新回测”和“重置日期”按钮
   - 让参数修改之外也有显式触发入口

4. 优化了前端打包拆分
   - 更新 `trend-web/vite.config.ts`
   - 将 `vue`、`echarts`、`axios` 做了手动拆包
   - 降低首版单包过大的风险，便于后续继续扩展

5. 完成了本地验证
   - 使用本机 `npm run build`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `trend-web` 已经比前一轮更适合作为默认入口：

- 接口异常时有明确反馈和重试路径
- 没有结果时不会直接渲染空图表和空表格
- 页面展示与交互状态更完整
- 前端构建也不再完全依赖单一超大包

这意味着后续继续把旧页面收缩时，新前端的可接管程度会更高。

#### 这一步为什么重要

- 只有基本页面和图表还不够，真正替代旧入口前必须先把失败路径和空状态补齐
- 先把构建拆包收一轮，能减少后面继续加页面和组件时的打包风险
- 这一步是在保证“新的内容可用”的前提下继续把新前端往生产化方向推

#### 下一步计划

下一步优先考虑以下动作：

1. 继续压缩 `trend-trading-backtest-view` 内部遗留资源和旧页面依赖
2. 继续扩展 `trend-web` 的页面结构，逐步形成真正的独立前端
3. 评估是否开始推进市场数据服务合并或 Java 版本升级主线

### 2026-03-18 - 阶段 46：将旧视图服务收缩为纯跳转壳层

#### 本阶段目标

- 继续压缩 `trend-trading-backtest-view` 的遗留负担
- 在确认新前端已能接管入口后，移除旧 Thymeleaf 页面和静态资源
- 让视图服务只保留最小跳转职责，不再继续承载旧页面实现

#### 已完成事项

1. 收缩了视图服务依赖
   - 从 `trend-trading-backtest-view/pom.xml` 中移除了 `spring-boot-starter-thymeleaf`
   - 让该模块不再需要服务端模板渲染能力

2. 精简了入口控制器
   - 更新 `trend-trading-backtest-view/src/main/java/bupt/web/ViewController.java`
   - 让 `/` 与 `/legacy` 都统一重定向到 `trend-web`
   - 移除了旧页面渲染相关的 `Model`、`version` 和 `RefreshScope` 依赖

3. 清理了旧视图配置与资源
   - 更新 `trend-trading-backtest-view/src/main/resources/application.yml`
   - 更新 `trend-trading-backtest-view/src/main/resources/application-nacos.yml`
   - 移除了 Thymeleaf 和旧页面版本展示相关配置
   - 删除了 `templates/` 和 `static/` 下的旧页面模板、Vue2/jQuery/Bootstrap/Chart.js/Datepicker 静态资源

4. 更新了迁移记录
   - 在迁移矩阵中把 `trend-trading-backtest-view` 更新为“纯跳转壳层”

5. 完成了本地验证
   - 使用本机 Maven 对 `trend-trading-backtest-view` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `trend-trading-backtest-view` 已经不再保留旧页面实现：

- 旧模板和静态资源已移除
- 服务端模板依赖已移除
- 模块只负责把入口跳转到 `trend-web`

这意味着前端迁移主线已经从“新前端接管入口”进一步推进到了“旧页面实现退场”。

#### 这一步为什么重要

- 如果旧页面模板和静态资源继续长期保留，前端迁移仍然会被两套实现并存拖慢
- 先把视图服务压成纯跳转壳层，仓库状态会和当前真实入口保持一致
- 这一步也为后续彻底删除 `trend-trading-backtest-view` 模块打下了基础

#### 下一步计划

下一步优先考虑以下动作：

1. 继续扩展 `trend-web` 的页面结构，逐步形成真正独立的前端应用
2. 评估是否开始推进市场数据服务合并主线
3. 评估 `trend-trading-backtest-view` 是否已接近整体退场条件

### 2026-03-18 - 阶段 47：将 trend-web 扩展为多页面前端应用壳层

#### 本阶段目标

- 继续把 `trend-web` 从单页工作台推进为真正独立的前端应用
- 落地基础路由、导航和页面组织结构
- 为后续继续拆业务页面和扩展交互提供稳定壳层

#### 已完成事项

1. 搭建了前端应用壳层
   - 新增 `trend-web/src/layouts/AppShell.vue`
   - 增加左侧导航、品牌区和兼容入口说明
   - 让新前端具备稳定的应用级布局

2. 扩展了前端路由结构
   - 更新 `trend-web/src/router/index.ts`
   - 从单一路由扩展为：
     - `overview`
     - `trades`
     - `status`
   - 使用懒加载方式加载页面，继续优化首屏打包结构

3. 拆分了页面职责
   - 新增 `OverviewView.vue`
   - 新增 `TradesView.vue`
   - 新增 `StatusView.vue`
   - 新增 `ViewStatePanel.vue`
   - 新增 `useBacktestWorkspace` 复用初始化逻辑
   - 删除旧的 `BacktestWorkbench.vue` 单页聚合入口

4. 调整了样式与迁移记录
   - 更新 `trend-web/src/styles.css`
   - 为应用壳层、导航、状态页和多页面布局补齐样式
   - 在迁移矩阵中将 `trend-web` 更新为“已形成多页面应用壳层”

5. 完成了本地验证
   - 使用本机 `npm run build`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `trend-web` 已经不再只是“一页回测工作台”：

- 有了应用壳层
- 有了多页面导航
- 有了独立的状态页与交易页
- 有了更适合继续扩展的前端结构

这意味着前端迁移已经开始从“先做可用页”推进到“形成真正的独立应用形态”。

#### 这一步为什么重要

- 如果一直停留在单页工作台，后续继续拆业务能力时前端结构会越来越挤
- 先把应用壳层和路由结构立起来，后续新增页面和功能时成本会明显更低
- 这一步也让 `trend-web` 更接近最终替代 `trend-trading-backtest-view` 的目标形态

#### 下一步计划

下一步优先考虑以下动作：

1. 继续扩展 `trend-web` 的业务页面与交互细节
2. 评估是否开始推进市场数据服务合并主线
3. 判断 `trend-trading-backtest-view` 是否可以整体退场

### 2026-03-18 - 阶段 48：落地 market-data-service 收敛试点模块

#### 本阶段目标

- 直接开始业务服务收敛主线，而不是继续只停留在“未来要合并”的文档状态
- 先把 `index-codes-service` 和 `index-data-service` 的读能力合成为一个可编译、可挂路由、可纳入监控样板的新模块
- 用新增模块验证后续“市场数据服务收敛”这条迁移路线是能真实落地的

#### 已完成事项

1. 新增了 `market-data-service` 试点模块
   - 在父工程 `pom.xml` 中注册了 `market-data-service`
   - 新建模块 `market-data-service`
   - 补齐了启动类、缓存配置、端口探测与最小 `Zipkin / Prometheus / Nacos` 依赖

2. 合并了市场数据读能力的最小入口
   - 新增 `/codes` 读取入口
   - 新增 `/data/{code}` 读取入口
   - 先复用 `index-codes-service` 与 `index-data-service` 当前占位型缓存读取逻辑
   - 让新模块先具备“接口兼容 + 编译可用”的收敛试点形态

3. 补齐了新模块的接入样板
   - 新增 `application-nacos.yml`
   - 新增 `bootstrap-nacos.yml`
   - 新增 `infra/nacos-config/templates/market-data-service-dev.yaml`
   - 在 `infra/nacos-config/README.md` 中登记新模板

4. 扩展了入口层和监控样板
   - 在 `gateway-service` 中新增 `/api-market/**` 路由，转发到 `lb://MARKET-DATA-SERVICE`
   - 在 `infra/docker-compose/prometheus/prometheus.yml` 中新增 `market-data-service` 抓取目标 `host.docker.internal:8061`

5. 更新了迁移矩阵
   - 在 `SERVICE_TRANSITION_MATRIX.md` 中登记 `market-data-service` 为“市场数据读能力收敛试点模块”

6. 完成了本地验证
   - 使用本机 Maven 对 `market-data-service` 与 `gateway-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在市场数据服务收敛已经不再只是目标架构里的文字描述：

- `market-data-service` 已真实落库
- 新模块已具备最小读能力接口
- 新网关入口和 Prometheus 抓取样板也已同步接上

这意味着后续可以沿着这条新模块继续把 `index-gather-store-service` 的同步能力逐步并入，而不是继续维持三个高度耦合的小服务长期并存。

#### 这一步为什么重要

- 旧市场数据模块边界本来就偏细碎，继续只做文档规划不会让架构真正收敛
- 先把读能力合成一个试点模块，可以最快验证“服务合并”这条主线是否顺畅
- 这一步也把当前重构从“旧基础设施退场 + 新前端接管”推进到了“业务服务边界开始收缩”的下一阶段

#### 下一步计划

下一步优先考虑以下动作：

1. 让 `trend-web` 优先读取 `/api-market/**`，开始消费新的市场数据试点服务
2. 评估把 `index-gather-store-service` 的同步能力继续并入 `market-data-service`
3. 在验证新链路可用后，再考虑把旧 `index-codes-service` 和 `index-data-service` 从入口链路中降级

### 2026-03-18 - 阶段 49：让 trend-web 优先消费 market-data-service

#### 本阶段目标

- 不让 `market-data-service` 只停留在“新模块已创建”的状态
- 让 `trend-web` 开始真实消费新的市场数据收敛试点入口
- 同时保留一个最小兼容回退，确保当前前端链路可用

#### 已完成事项

1. 调整了前端市场数据读取入口
   - 新增 `trend-web/src/services/market-data.ts`
   - 当前前端会优先调用 `/api-market/codes`
   - 如果新试点服务暂时不可用，再自动回退到 `/api-codes/codes`

2. 同步更新了前端状态管理
   - 更新 `trend-web/src/stores/backtest.ts`
   - 增加 `indexSource` 状态
   - 让页面可以明确展示当前实际命中的市场数据来源
   - 同时把初始化失败提示收敛到新的市场数据链路表述

3. 更新了新前端状态页与总览页
   - 更新 `trend-web/src/views/OverviewView.vue`
   - 更新 `trend-web/src/views/StatusView.vue`
   - 当前可以直接看到：
     - 新前端优先读取 `/api-market/**`
     - 当前实际数据来源是 `market-data-service` 还是兼容回退链路

4. 完成了本地验证
   - 使用本机 `npm run build`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `market-data-service` 不再只是仓库里新增的一个试点模块：

- `trend-web` 已开始优先走新的市场数据收敛入口
- 如果新链路临时不可用，当前前端还能自动回退到旧 `index-codes-service`
- 页面上也能直接看到当前命中的数据来源

这意味着业务服务收敛主线已经从“模块落库”推进到了“新前端开始真实消费新模块”的阶段。

#### 这一步为什么重要

- 只有新模块没有消费者，收敛主线就还没有形成真实闭环
- 先让新前端把市场数据入口切过去，后面继续降级旧服务入口时会更顺
- 这一步也给后续继续并入 `index-gather-store-service` 提供了更明确的落点

#### 下一步计划

下一步优先考虑以下动作：

1. 评估把 `index-gather-store-service` 的同步能力继续并入 `market-data-service`
2. 开始弱化 `trend-web` 对旧 `/api-codes/**` 回退链路的依赖
3. 继续让状态页和入口文案反映新的业务服务收敛进度

### 2026-03-18 - 阶段 50：将市场数据同步能力并入 market-data-service

#### 本阶段目标

- 继续把 `market-data-service` 从“只合并查询接口”推进到“开始承接同步能力”
- 不再沿用旧的 `Quartz + Hystrix` 组合，而是在新模块里先落最小可用的 `@Scheduled` 同步路径
- 为后续真正弱化 `index-gather-store-service` 创造条件

#### 已完成事项

1. 扩展了 `market-data-service` 的同步能力
   - 新增 `ThirdPartIndexClient`
   - 新增 `MarketDataSyncService`
   - 新增 `MarketDataSyncScheduler`
   - 新增 `MarketDataSyncController`
   - 当前新模块已经可以：
     - 定时刷新指数代码
     - 定时刷新所有指数历史数据
     - 手动触发 `/sync/codes`
     - 手动触发 `/sync/all`
     - 手动触发 `/sync/data/{code}`

2. 把查询服务从占位读取升级为“可同步 + 可缓存”
   - 更新 `MarketIndexCodeService`
   - 更新 `MarketIndexDataService`
   - 当前已具备从第三方数据服务拉取、刷新 Redis 缓存、再对外读取的最小闭环

3. 调整了新模块启动与配置
   - 更新 `MarketDataApplication`
   - 增加 `@EnableScheduling`
   - 增加 `RestTemplate` Bean
   - 在 `application.yml`、`application-nacos.yml` 和 `market-data-service-dev.yaml` 中补充：
     - 第三方数据源地址
     - 同步周期
     - 首次延迟时间

4. 完成了本地验证
   - 使用本机 Maven 对 `market-data-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `market-data-service` 已经不再只是一个读接口聚合模块：

- 读能力已存在
- 同步能力也已经开始并入
- 新模块内部已经形成“拉取第三方数据 -> 写缓存 -> 对外提供查询”的最小闭环

这意味着市场数据服务合并主线已经从“接口层收敛”推进到了“数据同步能力也开始迁移”的阶段。

#### 这一步为什么重要

- 如果只合并查询接口，不合并同步入口，`market-data-service` 仍然只是半个服务
- 先把同步最小闭环补进来，后面继续压缩 `index-gather-store-service` 就会更顺
- 这一步也顺手把旧同步模块里最重的 `Quartz + Hystrix` 依赖路径，从新模块默认实现里绕开了

#### 下一步计划

下一步优先考虑以下动作：

1. 开始弱化 `index-gather-store-service` 的主构建地位
2. 让 `trend-web` 和文档进一步明确当前市场数据主线已经转向 `market-data-service`
3. 在确认新同步链路稳定后，再决定何时删除旧同步模块

### 2026-03-18 - 阶段 51：将旧同步模块移出主构建

#### 本阶段目标

- 在 `market-data-service` 已开始承接同步职责后，继续压缩旧市场数据同步模块的存在感
- 先把 `index-gather-store-service` 从主构建中摘掉，而不是继续让它跟当前主线一起编译
- 让仓库主线明确转向新的市场数据收敛模块

#### 已完成事项

1. 调整了父工程模块列表
   - 从根 `pom.xml` 中移除了 `index-gather-store-service`
   - 让旧同步模块不再参与主构建

2. 更新了服务迁移矩阵
   - 将 `index-gather-store-service` 的当前状态更新为“已移出主构建”
   - 明确其同步职责已经开始并入 `market-data-service`

3. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在市场数据收敛主线已经进一步向前推进：

- `market-data-service` 已承接读能力
- `market-data-service` 已开始承接同步能力
- `index-gather-store-service` 已不再参与当前主构建

这意味着旧市场数据同步模块已经从“仍在主线里”进入“阶段性退场”的状态。

#### 这一步为什么重要

- 如果旧同步模块继续留在主构建里，后续每次构建都会继续把旧实现当成当前主线的一部分
- 先把它移出主构建，能让仓库状态更准确反映当前实际迁移方向
- 这一步也为后续直接物理删除旧同步模块铺平了路径

#### 下一步计划

下一步优先考虑以下动作：

1. 评估并直接物理删除 `index-gather-store-service` 模块目录
2. 开始继续收缩 `index-codes-service` 和 `index-data-service` 的默认入口地位
3. 让文档进一步反映当前市场数据主线已经完成“新模块承接 + 旧同步模块退到主构建外”

### 2026-03-18 - 阶段 52：删除旧同步模块源码目录

#### 本阶段目标

- 在旧同步模块已经移出主构建后，继续完成真正的物理退场
- 删除 `index-gather-store-service` 源码目录，避免旧实现继续滞留在仓库主线里
- 让当前市场数据主线彻底收敛到 `market-data-service`

#### 已完成事项

1. 删除了旧同步模块源码文件
   - 删除 `index-gather-store-service/pom.xml`
   - 删除启动类、Quartz 配置、Redis 配置、同步任务类
   - 删除控制器、服务类、实体类、工具类和测试占位文件

2. 清理了旧模块目录
   - 删除 `index-gather-store-service` 目录下遗留的 `.iml` 和本地构建产物目录
   - 让仓库主线不再保留这个旧模块的源码空壳

3. 更新了迁移矩阵
   - 将 `index-gather-store-service` 状态更新为“已退场”
   - 明确当前旧同步职责已经由 `market-data-service` 开始承接

4. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在市场数据相关旧模块的退场已经继续向前推进：

- `market-data-service` 已承接查询主线
- `market-data-service` 已开始承接同步主线
- `index-gather-store-service` 已完成“移出主构建 + 源码目录删除”的阶段性退场

这意味着后续就可以继续把注意力放到 `index-codes-service`、`index-data-service` 的默认入口压缩上。

#### 这一步为什么重要

- 如果只是移出主构建但继续留目录，仓库状态仍然和真实架构不完全一致
- 直接删除旧同步模块，能让当前主线更清楚地表达“市场数据收敛已经开始”
- 这一步也减少了旧 Quartz/Hystrix 同步实现继续干扰后续判断的成本

#### 下一步计划

下一步优先考虑以下动作：

1. 开始弱化 `index-codes-service` 和 `index-data-service` 的主构建地位
2. 让网关和文档进一步明确市场数据默认主线已经转向 `market-data-service`
3. 在确认兼容入口不再需要后，再决定何时删除旧查询模块

### 2026-03-18 - 阶段 53：将市场数据默认消费链路切到 market-data-service

#### 本阶段目标

- 不再只让 `market-data-service` 承担“新增试点模块”角色
- 把新前端、网关和回测服务的默认市场数据链路统一切到 `market-data-service`
- 为后续真正删除 `index-codes-service` 和 `index-data-service` 做准备

#### 已完成事项

1. 调整了回测服务的默认市场数据服务名
   - 更新 `trend-trading-backtest-service/src/main/java/bupt/client/IndexDataClient.java`
   - 将 Feign 调用目标从 `INDEX-DATA-SERVICE` 切换为 `MARKET-DATA-SERVICE`
   - 同时将 HTTP 模式下的默认地址从 `http://localhost:8021` 更新为 `http://localhost:8061`
   - 同步更新了 `application.yml`、`application-nacos.yml` 和 `trend-trading-backtest-service-dev.yaml`

2. 收缩了网关中的旧查询入口
   - 更新 `gateway-service/src/main/resources/application.yml`
   - 更新 `gateway-service/src/main/resources/application-nacos.yml`
   - 更新 `infra/nacos-config/templates/gateway-service-dev.yaml`
   - 移除了旧 `/api-codes/**` 默认路由
   - 保留并继续使用 `/api-market/**` 作为市场数据默认入口

3. 收缩了新前端中的旧回退路径
   - 更新 `trend-web/src/services/market-data.ts`
   - 移除了对 `/api-codes/codes` 的自动回退
   - 更新 `trend-web/src/stores/backtest.ts` 和 `trend-web/src/views/StatusView.vue`
   - 让页面状态文案明确当前市场数据默认链路已经切到 `market-data-service`
   - 更新 `trend-web/vite.config.ts`，将开发代理从 `/api-codes` 调整为 `/api-market`

4. 更新了迁移矩阵和网关说明
   - 更新 `SERVICE_TRANSITION_MATRIX.md`
   - 更新 `gateway-service/README.md`
   - 明确 `index-codes-service` 与 `index-data-service` 已进入“默认地位弱化”状态

5. 完成了本地验证
   - 使用本机 Maven 对 `gateway-service` 与 `trend-trading-backtest-service` 执行了 `compile`
   - 使用本机 `npm run build` 验证 `trend-web`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在市场数据主线已经进一步收敛：

- 新前端默认读取 `market-data-service`
- 回测服务默认读取 `market-data-service`
- 网关默认暴露的市场数据入口也已经转向 `/api-market/**`

这意味着旧的 `index-codes-service` 与 `index-data-service` 已经不再承载默认主线职责。

#### 这一步为什么重要

- 只有把真正的消费方切过去，旧查询模块才具备被摘出主构建甚至删除的条件
- 先统一默认消费链路，后面删旧模块时就不会还被隐性依赖拖住
- 这一步也让当前仓库状态更准确地反映“市场数据已经开始收敛为一个服务”

#### 下一步计划

下一步优先考虑以下动作：

1. 评估并将 `index-codes-service` 与 `index-data-service` 从主构建中摘除
2. 在确认没有残余默认入口后，再直接删除旧查询模块源码目录
3. 继续把文档和监控样板中的旧查询模块痕迹压缩掉

### 2026-03-18 - 阶段 54：将旧查询模块移出主构建

#### 本阶段目标

- 在市场数据默认消费链路已经切到 `market-data-service` 后，继续压缩旧查询模块的存在感
- 先把 `index-codes-service` 和 `index-data-service` 从主构建中摘掉
- 让仓库主线明确只围绕新的市场数据聚合服务继续演进

#### 已完成事项

1. 调整了父工程模块列表
   - 从根 `pom.xml` 中移除了 `index-codes-service`
   - 从根 `pom.xml` 中移除了 `index-data-service`
   - 让这两个旧查询模块不再参与当前主构建

2. 更新了服务迁移矩阵
   - 将 `index-codes-service` 状态更新为“已移出主构建”
   - 将 `index-data-service` 状态更新为“已移出主构建”
   - 明确当前默认查询主线已收敛到 `market-data-service`

3. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在市场数据相关旧模块的收缩又向前推进了一步：

- `market-data-service` 已承接默认查询主线
- `market-data-service` 已开始承接默认同步主线
- `index-codes-service` 和 `index-data-service` 已不再参与当前主构建

这意味着后续已经可以直接进入“物理删除旧查询模块源码目录”的阶段。

#### 这一步为什么重要

- 如果旧查询模块继续留在主构建里，它们仍然会被当作当前主线的一部分维护
- 先把它们移出主构建，能让仓库状态和当前真实消费链路保持一致
- 这一步也为下一轮直接删除旧查询模块目录铺平了路径

#### 下一步计划

下一步优先考虑以下动作：

1. 直接物理删除 `index-codes-service` 和 `index-data-service` 模块目录
2. 继续收缩监控样板与文档中旧查询模块的残余痕迹
3. 再决定 `third-part-index-data-project` 是保留为 fixture 还是继续压缩

### 2026-03-18 - 阶段 55：删除旧查询模块源码目录

#### 本阶段目标

- 在旧查询模块已经移出主构建后，继续完成真正的物理退场
- 删除 `index-codes-service` 和 `index-data-service` 源码目录
- 让当前市场数据主线彻底收敛到 `market-data-service`

#### 已完成事项

1. 删除了 `index-codes-service` 模块源码文件
   - 删除 `pom.xml`
   - 删除启动类、配置类、控制器、服务类、实体类
   - 删除 `application.yml`、`application-nacos.yml`、`bootstrap-nacos.yml`
   - 删除测试占位文件

2. 删除了 `index-data-service` 模块源码文件
   - 删除 `pom.xml`
   - 删除启动类、配置类、控制器、服务类、实体类
   - 删除 `application.yml`、`application-nacos.yml`、`bootstrap-nacos.yml`
   - 删除测试占位文件

3. 清理了旧模块目录
   - 删除两个模块目录下遗留的 `.iml` 与本地构建产物目录
   - 让仓库主线不再保留这两个旧查询模块的源码空壳

4. 更新了迁移矩阵
   - 将 `index-codes-service` 状态更新为“已退场”
   - 将 `index-data-service` 状态更新为“已退场”
   - 明确当前默认查询主线已由 `market-data-service` 承接

5. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在市场数据服务收敛已经进入更彻底的阶段：

- `market-data-service` 已承接默认查询主线
- `market-data-service` 已开始承接默认同步主线
- `index-gather-store-service`、`index-codes-service`、`index-data-service` 都已完成源码退场

这意味着当前仓库里与市场数据相关的主线已经基本压缩为一个聚合服务。

#### 这一步为什么重要

- 如果旧查询模块继续留目录，仓库状态仍然和真实默认架构不一致
- 直接删除这两个模块，能让当前主线更清楚地表达“市场数据已经完成一轮服务收敛”
- 这一步也为后续继续压缩监控样板和文档中的旧痕迹铺平了路径

#### 下一步计划

下一步优先考虑以下动作：

1. 继续收缩监控样板与文档中旧市场数据模块的残余痕迹
2. 评估 `third-part-index-data-project` 是否收敛为 fixture / mock-provider
3. 再决定是否继续推进更深层的版本现代化主线

### 2026-03-18 - 阶段 56：将第三方数据模块收口为本地 mock-provider

#### 本阶段目标

- 继续压缩市场数据链路中的旧微服务痕迹
- 保留 `third-part-index-data-project` 的静态 JSON 数据能力，但不再让它继续承担旧微服务注册职责
- 让这个模块明确回到“本地 fixture / mock-provider”角色

#### 已完成事项

1. 收缩了模块依赖
   - 更新 `third-part-index-data-project/pom.xml`
   - 移除了 `spring-cloud-starter-netflix-eureka-client`

2. 精简了启动逻辑
   - 更新 `third-part-index-data-project/src/main/java/bupt/ThirdPartIndexDataApplication.java`
   - 移除了 `@EnableEurekaClient`
   - 移除了启动前必须检查 `Eureka` 的逻辑
   - 当前模块只保留本地 `8090` 端口静态数据服务能力

3. 收敛了模块配置与迁移文档
   - 更新 `third-part-index-data-project/src/main/resources/application.yml`
   - 明确固定使用 `8090`
   - 更新 `SERVICE_TRANSITION_MATRIX.md`
   - 更新 `MIGRATION_CHECKLIST.md`
   - 明确当前该模块已经收口为本地 `mock-provider`

4. 完成了本地验证
   - 使用本机 Maven 对 `third-part-index-data-project` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `third-part-index-data-project` 已经不再属于旧式基础设施链路的一部分：

- 不再依赖 `Eureka`
- 仍保留本地静态样例数据能力
- 明确作为 `market-data-service` 的本地 fixture / mock-provider 使用

这意味着市场数据主线已经从“多个旧查询/同步微服务”继续收口到了“一个聚合服务 + 一个本地样例数据提供器”。

#### 这一步为什么重要

- 如果这个模块继续保留旧微服务注册逻辑，就会和当前已经收敛后的市场数据主线脱节
- 先把角色收口清楚，后面无论保留它做演示数据，还是再进一步迁到 `fixtures/`，都会更顺
- 这一步也让文档和当前实际架构更加一致

#### 下一步计划

下一步优先考虑以下动作：

1. 继续收缩监控样板与文档中已经退场的旧市场数据模块痕迹
2. 评估是否把 `third-part-index-data-project` 再进一步迁为更纯粹的 `fixtures/` 目录
3. 再决定是否开始推进更深层的版本现代化主线

### 2026-03-18 - 阶段 57：清理监控样板与文档中的旧市场数据痕迹

#### 本阶段目标

- 让监控样板和中文文档继续与当前实际架构对齐
- 移除已经退场的 `index-gather-store-service`、`index-codes-service`、`index-data-service` 监控目标
- 让仓库里的说明文档明确围绕 `market-data-service` 这一条市场数据主线展开

#### 已完成事项

1. 收缩了 Prometheus 抓取样板
   - 更新 `infra/docker-compose/prometheus/prometheus.yml`
   - 删除：
     - `index-gather-store-service`
     - `index-codes-service`
     - `index-data-service`
     的抓取目标
   - 保留 `market-data-service` 作为当前市场数据主线监控目标

2. 收缩了 Grafana 总览面板
   - 更新 `infra/docker-compose/grafana/provisioning/dashboards/json/trend-overview.json`
   - 将旧的 `Index Data Service` 卡片切换为 `Market Data Service`
   - 删除了 `Index Codes Service` 卡片

3. 更新了迁移与配置文档
   - 更新 `infra/transition/LEGACY_INFRA_RETIREMENT_PLAN.md`
   - 更新 `infra/nacos-config/README.md`
   - 更新根 `README.md`
   - 明确当前默认市场数据主线已经收敛到：
     - `market-data-service`
     - `third-part-index-data-project` 本地样例数据提供器

4. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在仓库里的监控样板和主要文档已经更接近当前实际架构：

- 市场数据默认主线只保留 `market-data-service`
- 旧查询/同步模块不再继续出现在默认监控总览里
- 项目说明文档也开始围绕新的运行链路展开

这意味着当前市场数据收敛不只体现在代码上，也开始体现在观测面和文档层。

#### 这一步为什么重要

- 如果监控样板和文档还长期保留旧模块名称，会让仓库状态和实际架构脱节
- 先把这些外围痕迹收干净，后面继续推进更深层的现代化时判断成本会更低
- 这一步也让当前仓库对外表达更加统一，不再同时描述两套市场数据主线

#### 下一步计划

下一步优先考虑以下动作：

1. 评估是否把 `third-part-index-data-project` 进一步迁为更纯粹的 `fixtures/` 目录
2. 或开始推进更深层的版本现代化主线，例如父 POM、Java 版本和 Spring 版本升级
3. 再决定是否需要补更多围绕 `market-data-service` 的测试与监控样板

### 2026-03-18 - 阶段 58：移除主线模块中的 Eureka 依赖与默认配置

#### 本阶段目标

- 在旧注册中心已经退场后，继续清理主线模块里残留的 `Eureka` 依赖、注解和默认配置
- 让 `gateway-service`、`market-data-service`、`trend-trading-backtest-service`、`trend-trading-backtest-view` 默认按 `nacos` 路径运行
- 为后续更深层的版本现代化减少旧栈包袱

#### 已完成事项

1. 移除了主线模块中的 `Eureka Client` 依赖
   - 更新 `gateway-service/pom.xml`
   - 更新 `market-data-service/pom.xml`
   - 更新 `trend-trading-backtest-service/pom.xml`
   - 更新 `trend-trading-backtest-view/pom.xml`
   - 删除 `spring-cloud-starter-netflix-eureka-client`

2. 清理了启动类中的 `Eureka` 注解与检查逻辑
   - 更新 `GatewayServiceApplication`
   - 更新 `MarketDataApplication`
   - 更新 `TrendTradingBackTestServiceApplication`
   - 更新 `TrendTradingBackTestViewApplication`
   - 删除 `@EnableEurekaClient`
   - 删除启动前对 `8761` 端口的依赖检查
   - 让无显式 profile 时默认按 `nacos` 路径判断启动前置条件

3. 调整了默认配置
   - 更新 `gateway-service/src/main/resources/application.yml`
   - 更新 `market-data-service/src/main/resources/application.yml`
   - 更新 `trend-trading-backtest-service/src/main/resources/application.yml`
   - 增加 `spring.profiles.active=${SPRING_PROFILES_ACTIVE:nacos}`
   - 删除默认 `eureka.client.service-url.defaultZone`
   - 更新 `trend-trading-backtest-view/src/main/resources/bootstrap.yml`
   - 删除其中残留的 `eureka` 配置

4. 更新了迁移记录
   - 更新 `LEGACY_INFRA_RETIREMENT_PLAN.md`
   - 更新 `REFACTOR_PROGRESS.md`
   - 明确当前主线模块已不再默认依赖 `Eureka`

5. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在主线模块已经进一步去掉了旧注册中心痕迹：

- 默认启动路径已切到 `nacos`
- 主线模块已不再携带 `Eureka Client` 依赖
- 启动类和默认配置也不再假定 `8761` 必须存在

这意味着当前仓库主线已经更接近真正的 “Nacos-only” 运行形态。

#### 这一步为什么重要

- 如果依赖和默认配置里还长期残留 `Eureka`，后续版本升级时会继续拖着旧栈包袱
- 先把主线模块的默认运行方式收成 `nacos`，能显著降低后续现代化升级的判断成本
- 这一步也让仓库状态和我们已经完成的基础设施退场结果保持一致

#### 下一步计划

下一步优先考虑以下动作：

1. 开始推进更深层的版本现代化主线，例如父 POM、Java 版本和 Spring 版本升级准备
2. 或补一轮围绕 `market-data-service` 与 `backtest-service` 的关键回归测试
3. 再决定是否把 `third-part-index-data-project` 进一步迁为纯 `fixtures/` 目录

### 2026-03-18 - 阶段 59：收口父工程中的主线依赖版本

#### 本阶段目标

- 在正式推进更深层版本升级前，先把主线模块里分散的版本号收回父工程
- 减少各模块各自维护 `Nacos` 和 `Resilience4j` 版本的重复成本
- 为后续统一升级 `Spring / Java / 依赖栈` 降低改动面

#### 已完成事项

1. 调整了父工程版本属性
   - 更新根 `pom.xml`
   - 新增 `spring-cloud-alibaba.version`
   - 新增 `resilience4j.version`

2. 调整了父工程依赖管理
   - 在根 `pom.xml` 的 `dependencyManagement` 中新增：
     - `spring-cloud-starter-alibaba-nacos-discovery`
     - `spring-cloud-starter-alibaba-nacos-config`
     - `resilience4j-circuitbreaker`
   - 让主线模块统一继承这些版本

3. 精简了主线模块 POM
   - 更新 `gateway-service/pom.xml`
   - 更新 `market-data-service/pom.xml`
   - 更新 `trend-trading-backtest-service/pom.xml`
   - 更新 `trend-trading-backtest-view/pom.xml`
   - 删除各模块中重复定义的 `spring-cloud-alibaba.version`、`resilience4j.version`
   - 删除各模块里对应依赖上的显式 `version`

4. 完成了本地验证
   - 使用本机 Maven 对主线模块执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在主线模块里的关键依赖版本已经开始向父工程收口：

- `Nacos` 相关版本不再散落在多个子模块里
- `Resilience4j` 版本也不再由单个业务模块单独维护
- 后续继续推进版本现代化时，改动入口会更集中

#### 这一步为什么重要

- 如果版本号继续散落在子模块里，后面做 Boot/Java 升级时会出现很多重复改动
- 先把父工程变成真正的版本收口入口，能明显降低下一阶段升级成本
- 这一步也让当前主线更接近现代化父工程的组织方式

#### 下一步计划

下一步优先考虑以下动作：

1. 开始推进父 POM 的更深层现代化准备，例如 Java 21 / Boot 3.5.x / Cloud 2024.x 的升级分层计划
2. 或补一轮围绕 `market-data-service` 与 `trend-trading-backtest-service` 的关键回归测试
3. 再决定是否把 `third-part-index-data-project` 进一步迁为纯 `fixtures/` 目录

### 2026-03-18 - 阶段 60：在父 POM 中补齐版本升级预备入口

#### 本阶段目标

- 在不直接触发大版本切换的前提下，为 `Boot 3 / Java 21 / Cloud 2024` 升级准备父工程入口
- 先把根工程里的目标版本标记和公共插件管理补齐
- 让后续升级动作尽量集中在父 POM，而不是再回到多个模块分散修改

#### 已完成事项

1. 补充了父工程的升级目标标记
   - 更新根 `pom.xml`
   - 新增：
     - `target.spring-boot.version`
     - `target.spring-cloud.version`
     - `target.spring-cloud-alibaba.version`
   - 让后续升级目标在父工程里更明确

2. 收口了父工程的公共插件管理
   - 在根 `pom.xml` 中补充 `maven-resources-plugin`
   - 在根 `pom.xml` 中补充 `maven-surefire-plugin`
   - 给 `maven-compiler-plugin` 增加 `parameters`
   - 让主线模块后续升级时不再各自分散处理这些基础插件配置

3. 增加了 Java 21 预备 profile
   - 在根 `pom.xml` 中新增 `modernization-java21-preview`
   - 当前该 profile 默认不启用
   - 但已经作为后续 Java 21 升级的统一入口落在父工程中

4. 更新了迁移清单与执行记录
   - 更新 `MIGRATION_CHECKLIST.md`
   - 更新 `REFACTOR_PROGRESS.md`
   - 明确当前已经进入“版本现代化准备”阶段，而不是只停留在业务与基础设施收敛

5. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在父工程已经不只是“记录目标版本”，而是开始具备真正的升级入口：

- 有了更明确的 Boot / Cloud / Java 目标标记
- 有了更完整的公共插件管理
- 有了非默认启用的 Java 21 预备 profile

这意味着后续继续推进版本现代化时，改动会更集中，也更容易分阶段推进。

#### 这一步为什么重要

- 如果直接开始改大版本而没有先整理父工程入口，升级过程很容易重新散回多个模块
- 先把父 POM 的升级入口补齐，能明显降低后续正式升级时的重复修改量
- 这一步也让当前重构从“基础设施替换 + 服务收敛”自然过渡到了“版本现代化准备”

#### 下一步计划

下一步优先考虑以下动作：

1. 开始梳理 `Boot 3 / Java 21` 升级时的主要兼容阻塞点
2. 或补一轮围绕 `market-data-service` 与 `trend-trading-backtest-service` 的关键回归测试
3. 再决定是否把 `third-part-index-data-project` 进一步迁为纯 `fixtures/` 目录

### 2026-03-18 - 阶段 61：梳理 Boot 3 与 Java 21 升级阻塞点

#### 本阶段目标

- 在正式切换 `Boot 3 / Java 21` 之前，先把主线模块里的主要兼容风险点显式整理出来
- 避免后续升级时一边改版本、一边临时排雷
- 为下一步选择“先拆哪一个兼容点”提供明确依据

#### 已完成事项

1. 重新扫描了主线模块中的兼容风险
   - 检查了 `gateway-service`
   - 检查了 `market-data-service`
   - 检查了 `trend-trading-backtest-service`
   - 检查了 `trend-trading-backtest-view`
   - 检查了 `third-part-index-data-project`

2. 确认了当前最主要的升级阻塞点
   - `market-data-service` 仍使用 `ObjectMapper.enableDefaultTyping`
   - `trend-trading-backtest-service` 仍保留 `OpenFeign`
   - `market-data-service` 与部分回测链路仍使用 `RestTemplate`
   - `trend-trading-backtest-view` 仍保留 `bootstrap.yml / bootstrap-nacos.yml`
   - 主线模块仍残留 `@EnableDiscoveryClient`

3. 把阻塞点回填到了迁移清单
   - 更新 `MIGRATION_CHECKLIST.md`
   - 明确这些问题属于 `Boot 3 / Java 21` 之前需要优先拆掉的兼容项

4. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在版本现代化主线已经不再只是“知道要升到哪里”，而是开始明确“先拆哪些兼容点”：

- 升级风险已经收口到少数几个明确位置
- 下一步可以直接从 `Redis DefaultTyping` 这类高风险点开始动手
- 后续每一步会更接近真正可执行的 `Boot 3 / Java 21` 切换

#### 这一步为什么重要

- 如果不先把阻塞点写清楚，后面升级时很容易在多个模块来回试错
- 先做一次集中排雷，能让后面的版本升级改动更聚焦
- 这一步也让当前文档从“目标描述”进一步变成“升级执行清单”

#### 下一步计划

下一步优先考虑以下动作：

1. 先改掉 `market-data-service` 的 Redis `DefaultTyping` 序列化配置
2. 再继续收缩 `RestTemplate` 与 `OpenFeign`
3. 最后处理 `bootstrap` 残留与更深层的 Boot 3 配置兼容问题

### 2026-03-18 - 阶段 62：移除 market-data-service 的 Redis DefaultTyping 配置

#### 本阶段目标

- 优先拆掉 `Boot 3 / Java 21` 升级前最明显的 Jackson 兼容阻塞点
- 让 `market-data-service` 的 Redis 缓存序列化不再依赖已过时的 `enableDefaultTyping`
- 在不扩大改动面的前提下，先把高风险旧 API 从主线模块中移除

#### 已完成事项

1. 调整了 Redis 缓存值序列化实现
   - 更新 `market-data-service/src/main/java/bupt/config/RedisCacheConfig.java`
   - 删除 `Jackson2JsonRedisSerializer + ObjectMapper.enableDefaultTyping`
   - 改为使用 `GenericJackson2JsonRedisSerializer`

2. 保持了现有缓存管理结构
   - 保留 `CacheManager` 入口不变
   - 保留当前 key/value 序列化策略边界不变
   - 避免这一步顺手扩散到业务读写逻辑

3. 更新了迁移清单
   - 更新 `MIGRATION_CHECKLIST.md`
   - 将 Redis `DefaultTyping` 兼容阻塞点标记为已完成

4. 完成了本地验证
   - 使用本机 Maven 对 `market-data-service` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `market-data-service` 已经先拆掉了一个最直接的版本升级障碍：

- Redis 缓存序列化不再依赖 `enableDefaultTyping`
- `Boot 3 / Java 21` 前置兼容问题又少了一项
- 后续可以继续把注意力集中到 `RestTemplate`、`OpenFeign` 和 `bootstrap` 残留上

#### 这一步为什么重要

- `enableDefaultTyping` 是 Jackson 升级里最容易出问题的一类旧 API
- 先把这个点拿掉，能明显降低后续版本升级时的失败概率
- 这一步也符合当前“大步推进，但优先保证正确”的节奏

#### 下一步计划

下一步优先考虑以下动作：

1. 继续处理 `market-data-service` 的 `RestTemplate`
2. 或直接开始拆 `trend-trading-backtest-service` 中的 `OpenFeign`
3. 再处理 `trend-trading-backtest-view` 的 `bootstrap` 残留

### 2026-03-18 - 阶段 63：移除回测服务中的 OpenFeign 主线

#### 本阶段目标

- 直接拆掉 `trend-trading-backtest-service` 中残留的 `OpenFeign`
- 让回测服务只保留已经验证过的 HTTP 传输门面
- 为后续继续向 `Spring HTTP Service Clients` 靠拢先清掉旧依赖和旧注解

#### 已完成事项

1. 移除了 `OpenFeign` 依赖与启用入口
   - 更新 `trend-trading-backtest-service/pom.xml`
   - 删除 `spring-cloud-starter-openfeign`
   - 更新 `TrendTradingBackTestServiceApplication`
   - 删除 `@EnableFeignClients`

2. 删除了旧 Feign 调用实现
   - 删除 `IndexDataClient`
   - 删除 `FeignIndexDataGateway`
   - 让回测服务只保留 HTTP 传输实现

3. 收口了回测服务远程调用配置
   - 更新 `application.yml`
   - 更新 `application-nacos.yml`
   - 更新 `infra/nacos-config/templates/trend-trading-backtest-service-dev.yaml`
   - 删除 `backtest.remote.index-data.mode`
   - 让当前默认链路直接收口到 `http.base-url`

4. 完成了本地验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 执行了 `test`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在回测服务已经进一步摆脱旧式服务调用栈：

- 不再依赖 `OpenFeign`
- 不再保留 Feign 模式切换分支
- 当前远程调用主线已经收口为 HTTP 传输门面 + Resilience4j + 统一降级

#### 这一步为什么重要

- `OpenFeign` 是当前主线里最明显的旧调用栈残留之一
- 先把它彻底拿掉，后续再收口到 `Spring HTTP Service Clients` 会更顺
- 这一步也让回测服务的运行路径更单一，后面排查和升级都会更轻

#### 下一步计划

下一步优先考虑以下动作：

1. 继续处理 `market-data-service` 中的 `RestTemplate`
2. 处理 `trend-trading-backtest-view` 的 `bootstrap` 残留
3. 再统一清理主线模块中的 `@EnableDiscoveryClient`

### 2026-03-18 - 阶段 64：移除视图壳层中的 bootstrap 与旧配置链路残留

#### 本阶段目标

- 让 `trend-trading-backtest-view` 更接近纯壳层应用
- 删除 `bootstrap` 时代遗留的配置入口
- 清掉已经没有实际价值的 `Config Server / Bus / Nacos Config` 残留

#### 已完成事项

1. 收缩了视图壳层依赖
   - 更新 `trend-trading-backtest-view/pom.xml`
   - 删除 `spring-cloud-starter-alibaba-nacos-config`
   - 让壳层不再承担外部配置中心职责

2. 精简了启动前置检查
   - 更新 `TrendTradingBackTestViewApplication`
   - 删除对 `Config Server` 和 `RabbitMQ` 的旧端口检查
   - 让当前壳层只保留 `nacos` 与自身端口的最小启动判断

3. 删除了旧配置入口与无效模板
   - 删除 `trend-trading-backtest-view/src/main/resources/bootstrap.yml`
   - 删除 `trend-trading-backtest-view/src/main/resources/bootstrap-nacos.yml`
   - 删除 `infra/nacos-config/templates/trend-trading-backtest-view-dev.yaml`

4. 更新了迁移清单
   - 更新 `MIGRATION_CHECKLIST.md`
   - 将 `bootstrap` 残留阻塞点标记为已完成

5. 完成了本地验证
   - 使用本机 Maven 对 `trend-trading-backtest-view` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在 `trend-trading-backtest-view` 已经进一步收成轻壳层：

- 不再依赖 `bootstrap` 配置加载方式
- 不再依赖 `Nacos Config`
- 不再保留 `Config Server + Bus` 时代的启动前置检查

#### 这一步为什么重要

- `bootstrap` 残留会持续拖慢后续 Boot 3 配置兼容收口
- 先把这个壳层收干净，后面要不要彻底退掉它都会更容易
- 这一步也让当前主线更一致，不再混着两代配置加载方式

#### 下一步计划

下一步优先考虑以下动作：

1. 继续处理 `market-data-service` 中的 `RestTemplate`
2. 统一清理主线模块中的 `@EnableDiscoveryClient`
3. 再评估是否需要顺手收缩主线应用中的交互式端口输入逻辑

### 2026-03-18 - 阶段 65：将主线 HTTP 调用从 RestTemplate 收口到 WebClient

#### 本阶段目标

- 继续拆掉 `Boot 3 / Java 21` 升级前的旧 HTTP 客户端阻塞点
- 让 `market-data-service` 和回测服务的主线 HTTP 调用都不再依赖 `RestTemplate`
- 为后续进一步靠近现代调用栈打下更统一的基础

#### 已完成事项

1. 为主线模块补充了 `WebClient` 依赖
   - 更新 `market-data-service/pom.xml`
   - 更新 `trend-trading-backtest-service/pom.xml`
   - 新增 `spring-boot-starter-webflux`

2. 收口了市场数据服务的上游调用实现
   - 更新 `MarketDataApplication`
   - 删除 `RestTemplate` Bean
   - 更新 `ThirdPartIndexClient`
   - 将第三方数据读取改为基于 `WebClient` 的实现

3. 收口了回测服务的 HTTP 调用实现
   - 更新 `HttpIndexDataGateway`
   - 将读取 `market-data-service` 的实现从 `RestTemplateBuilder` 改为 `WebClient`

4. 更新了迁移清单
   - 更新 `MIGRATION_CHECKLIST.md`
   - 将 `RestTemplate` 阻塞点标记为已完成

5. 完成了本地验证
   - 使用本机 Maven 对 `market-data-service` 与 `trend-trading-backtest-service` 执行了 `test`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在主线 HTTP 调用又进一步现代化了一步：

- `market-data-service` 不再依赖 `RestTemplate`
- 回测服务的 HTTP 调用门面也不再依赖 `RestTemplate`
- 当前主线调用栈已经更接近后续 Boot 3 时代的实现方式

#### 这一步为什么重要

- `RestTemplate` 是这条升级主线里最典型的旧客户端痕迹之一
- 一次性把两个主线模块都收掉，后面继续升级时会轻很多
- 这一步也让当前主线调用方式更统一，后续继续演进到 `RestClient` 或更强约束的客户端会更顺

#### 下一步计划

下一步优先考虑以下动作：

1. 统一清理主线模块中的 `@EnableDiscoveryClient`
2. 收缩主线应用里的交互式端口输入逻辑
3. 再决定是否把当前迁移阶段标记为基本完成

### 2026-03-18 - 阶段 66：清理主线模块中的显式 Discovery 注解

#### 本阶段目标

- 收掉主线模块里最后一批明显的旧时代显式发现注解
- 让服务发现能力完全回到当前依赖栈自带的自动配置
- 为这一轮迁移主线画上一个更干净的收尾

#### 已完成事项

1. 清理了主线模块启动类中的显式注解
   - 更新 `gateway-service/src/main/java/bupt/GatewayServiceApplication.java`
   - 更新 `market-data-service/src/main/java/bupt/MarketDataApplication.java`
   - 更新 `trend-trading-backtest-service/src/main/java/bupt/TrendTradingBackTestServiceApplication.java`
   - 更新 `trend-trading-backtest-view/src/main/java/bupt/TrendTradingBackTestViewApplication.java`
   - 删除 `@EnableDiscoveryClient`

2. 更新了迁移清单
   - 更新 `MIGRATION_CHECKLIST.md`
   - 将显式 Discovery 注解阻塞点标记为已完成

3. 完成了本地验证
   - 使用本机 Maven 在根目录执行了 `validate`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在这一轮主线迁移里显式列出的兼容阻塞点已经全部清掉：

- `Redis DefaultTyping` 已移除
- `OpenFeign` 已移除
- `RestTemplate` 已收口到 `WebClient`
- `bootstrap` 残留已移除
- `@EnableDiscoveryClient` 已清理

#### 这一步为什么重要

- 这一步本身改动不大，但它让当前主线终于不再混着旧时代显式发现写法
- 也意味着本轮“迁移主线收口”已经从大方向完成，进入可选择的收尾优化阶段

#### 下一步计划

下一步优先考虑以下动作：

1. 如需继续深挖，可收缩主线应用里的交互式端口输入逻辑
2. 或开始真正的 `Spring Boot 3 / Java 21` 版本切换试跑
3. 当前仓库层面的这一轮迁移主线可视为完成

### 2026-03-18 - 阶段 67：移除主线应用中的交互式端口输入

#### 本阶段目标

- 收掉主线应用里不适合现代部署方式的交互式端口输入逻辑
- 让回测服务与视图壳层更适合本地脚本、容器和网关统一接入
- 为当前迁移主线做最后一轮可部署性收尾

#### 已完成事项

1. 精简了回测服务启动逻辑
   - 更新 `trend-trading-backtest-service/src/main/java/bupt/TrendTradingBackTestServiceApplication.java`
   - 删除控制台 `Scanner` 输入与超时等待逻辑
   - 改为按 `port=`、`server.port=`、系统属性、`SERVER_PORT` 环境变量和默认端口顺序解析

2. 精简了视图壳层启动逻辑
   - 更新 `trend-trading-backtest-view/src/main/java/bupt/TrendTradingBackTestViewApplication.java`
   - 删除控制台 `Scanner` 输入与超时等待逻辑
   - 改为按 `port=`、`server.port=`、系统属性、`SERVER_PORT` 环境变量和默认端口顺序解析

3. 完成了本地验证
   - 使用本机 Maven 对 `trend-trading-backtest-service` 与 `trend-trading-backtest-view` 执行了 `compile`
   - 当前结果为 `BUILD SUCCESS`

#### 当前结果

现在当前仓库主线已经完成了这一轮迁移收口：

- 旧基础设施主线已经退场
- 新前端已经接管入口
- 市场数据服务已经完成收敛
- 主线兼容阻塞点已经清掉
- 主线应用也不再依赖交互式启动方式

#### 这一步为什么重要

- 交互式端口输入很不利于容器化、脚本化和稳定启动
- 把这段逻辑去掉之后，当前仓库主线才更像真正可持续演进的现代化基线

#### 下一步计划

下一步优先考虑以下动作：

1. 如需继续推进，可开始真正的 `Spring Boot 3 / Java 21` 版本切换试跑
2. 当前仓库层面的这一轮迁移主线可记为完成

### 2026-03-17 - 阶段 1：父工程迁移底座整理

#### 本阶段目标

- 把根 `pom.xml` 整理成后续迁移可复用的入口
- 先统一依赖和编译管理，减少子模块各自维护配置的混乱
- 为后续 `Nacos`、`Gateway`、`Resilience4j` 等替换动作做铺垫

#### 已完成事项

1. 整理了根工程模块清单
   - 按“基础设施模块 / 业务模块”做了分组标记
   - 让后续做模块收敛和替换时更容易定位

2. 统一了父工程编译属性
   - 在根 `pom.xml` 中增加了：
     - `maven.compiler.source`
     - `maven.compiler.target`
   - 让子模块后续能逐步收敛到统一编译配置

3. 提炼了公共依赖版本
   - 把 `hutool` 版本提到父工程属性中统一管理
   - 同时补到 `dependencyManagement`，方便后续各模块继承

4. 补充了父工程插件管理
   - 增加 `maven-compiler-plugin` 的统一配置
   - 增加 `spring-boot-maven-plugin` 的统一声明
   - 为后续逐模块清理重复插件配置做准备

5. 在父工程中补充了迁移目标备注属性
   - 标记了目标 Java 版本
   - 标记了目标云原生技术栈方向
   - 这一步不是直接切换版本，而是把迁移目标显式写进工程基线

#### 当前结果

当前根工程已经从“纯粹老项目父 POM”变成了“可承载后续迁移的父工程入口”：

- 依赖管理开始集中
- 编译配置开始集中
- 模块分层开始清晰
- 后续做 Boot 升级、Nacos 接入和 Gateway 替换时，改动路径会更可控

#### 这一步为什么重要

- 目前很多子模块各自带有编译配置，甚至存在 `1.7`、`8` 混用的情况
- 如果不先整理父工程，后面做整体升级会出现大量重复修改
- 先把父工程变成统一入口，能显著降低后续迁移的成本

#### 下一步计划

下一步进入基础设施替换的第一刀，优先动作如下：

1. 准备本地 `Nacos` 运行方案
2. 新增或规划 `gateway-service`
3. 逐步从 `Eureka` 和 `Config Server` 迁移到 `Nacos`
