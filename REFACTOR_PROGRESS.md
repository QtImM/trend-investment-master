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
