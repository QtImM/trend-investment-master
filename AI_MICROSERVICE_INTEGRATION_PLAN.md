# AI 微服务接入方案（面向当前 trend-investment 项目）

## 1. 文档目标

本文基于当前仓库主链路，设计一个可渐进落地的 AI 微服务方案，优先实现以下三项能力：

1. 策略解释与复盘
2. 参数助手
3. 风险预警总结

目标是：

- 不破坏现有主链路可用性
- 保持微服务边界清晰
- 第一阶段不引入 SQL，沿用当前 Redis + 计算结果输入即可
- 后续可平滑升级到更复杂的 RAG/向量检索能力

---

## 2. 当前项目现状（与 AI 接入相关）

### 2.1 现有主链路

当前核心服务链路为：

- gateway-service（统一入口）
- market-data-service（市场数据）
- trend-trading-backtest-service（回测计算）
- trend-trading-backtest-view / trend-web（前端入口）

主入口：

- `http://127.0.0.1:8032/trend-web/`

### 2.2 与 AI 能力直接相关的数据来源

回测服务已经返回 AI 分析所需的大部分结构化数据：

- 指数历史序列：`indexDatas`
- 收益曲线：`profits`
- 交易记录：`trades`
- 年度收益：`annualProfits`
- 汇总指标：`trendIncomeAnnual`、`indexIncomeAnnual`、`winCount`、`lossCount`、`avgWinRate`、`avgLossRate` 等

这意味着 AI 服务不需要先改造核心计算引擎，就可以先做“解释层”和“建议层”。

### 2.3 基础设施约束

- 已有 Nacos Config + Nacos Discovery
- 已有网关统一路由
- 已有 Prometheus 指标基础
- 市场数据当前主要为 Redis 缓存与样例数据回退
- 当前本地链路有统一脚本 `.tools/local_stack.py` 和 `.tools/verify_local_migration.py`

结论：可以新增一个 AI 微服务并挂到网关，不需要先做全局架构改造。

---

## 3. 新增服务定位

建议新增服务：`trend-ai-analysis-service`

职责边界：

- 专注“分析与建议输出”，不直接承担交易执行
- 对内聚合回测数据与市场数据，对外输出解释文本和结构化建议
- 不替代 `trend-trading-backtest-service`，而是消费它的结果

不建议在第一阶段承担的职责：

- 自动下单
- 资金账户操作
- 强事务账务处理

---

## 4. 三项能力的落地设计

## 4.1 能力 A：策略解释与复盘

### 目标

把回测结果自动生成“可读复盘报告”，用于解释：

- 策略总体表现与基准对比
- 关键收益/回撤阶段
- 胜率、盈亏比、交易频次特征
- 年度表现差异

### API 设计

- `POST /analysis/review`

请求体（建议）：

```json
{
  "code": "000300",
  "params": {
    "ma": 20,
    "buyThreshold": 1.01,
    "sellThreshold": 0.99,
    "serviceCharge": 0,
    "startDate": "2018-01-01",
    "endDate": "2020-12-31"
  },
  "backtestResult": {
    "indexDatas": [],
    "profits": [],
    "trades": [],
    "annualProfits": [],
    "indexIncomeAnnual": 0,
    "trendIncomeAnnual": 0,
    "winCount": 0,
    "lossCount": 0,
    "avgWinRate": 0,
    "avgLossRate": 0
  }
}
```

返回体（建议）：

```json
{
  "summary": "策略在样本区间跑赢基准，年度波动较大。",
  "highlights": [
    "年化收益高于指数 3.2%",
    "胜率 54%，平均盈利高于平均亏损"
  ],
  "riskNotes": [
    "2019Q4 回撤明显",
    "交易频次偏高，手续费敏感"
  ],
  "explanations": [
    {
      "title": "收益来源",
      "content": "主要收益来自上涨趋势阶段的持仓收益。"
    }
  ],
  "disclaimer": "仅作为策略研究参考，不构成投资建议。"
}
```

### 第一阶段实现建议

- 先做“规则摘要 + LLM 润色”模式：
  - 规则层算出关键指标和结论
  - LLM 负责自然语言表达
- 即使模型调用失败，也能返回规则版报告，保障可用性

---

## 4.2 能力 B：参数助手

### 目标

基于给定指数与时间区间，给出参数建议范围与试验优先级，减少手工试参。

### API 设计

- `POST /analysis/parameter-suggestion`

请求体（建议）：

```json
{
  "code": "000300",
  "dateRange": {
    "startDate": "2018-01-01",
    "endDate": "2020-12-31"
  },
  "baseParams": {
    "ma": 20,
    "buyThreshold": 1.01,
    "sellThreshold": 0.99,
    "serviceCharge": 0
  },
  "constraints": {
    "maxRuns": 30,
    "target": "trendIncomeAnnual",
    "riskPreference": "balanced"
  }
}
```

返回体（建议）：

```json
{
  "recommendedRanges": {
    "ma": [15, 35],
    "buyThreshold": [1.005, 1.02],
    "sellThreshold": [0.97, 0.995]
  },
  "topCandidates": [
    {
      "ma": 24,
      "buyThreshold": 1.012,
      "sellThreshold": 0.986,
      "expected": {
        "trendIncomeAnnual": 0.138,
        "winRatio": 0.56,
        "maxDrawdown": 0.18
      },
      "reason": "在收益与回撤之间平衡较好"
    }
  ],
  "warnings": [
    "样本区间较短，建议做滚动窗口验证",
    "避免只选择单次最优参数"
  ]
}
```

### 第一阶段实现建议

- 不做复杂自动机器学习，先做“有限网格 + 评分函数”
- 评分函数建议示例：

$$
score = w_1 \cdot trendIncomeAnnual - w_2 \cdot maxDrawdown + w_3 \cdot winRatio
$$

- LLM 负责解释“为什么推荐这些参数”

---

## 4.3 能力 C：风险预警总结

### 目标

定时生成“市场与策略风险摘要”，面向研究和演示场景，输出可读风险结论。

### API 与任务设计

接口：

- `GET /analysis/risk-digest/{code}`

定时任务：

- 每日或每小时调度，生成最新风险摘要

请求依赖：

- market-data-service 的指数时间序列
- 最近一次或多次回测结果（可由前端传入，也可由 AI 服务调用回测接口生成）

返回体（建议）：

```json
{
  "code": "000300",
  "timestamp": "2026-03-26T10:00:00+08:00",
  "overallLevel": "medium",
  "signals": [
    {
      "name": "volatility_up",
      "level": "high",
      "detail": "近 20 日波动率较前 60 日上升 35%"
    },
    {
      "name": "trend_break_risk",
      "level": "medium",
      "detail": "价格多次跌破短期均线"
    }
  ],
  "advice": [
    "控制单次仓位",
    "提高止损纪律",
    "关注连续亏损次数"
  ],
  "disclaimer": "仅作为风险提示，不构成投资建议。"
}
```

### 第一阶段实现建议

- 风险信号先规则化（波动率抬升、连续回撤、异常交易频率）
- AI 负责总结与文字表达，不直接替代风险规则判断

---

## 5. 微服务架构接入点

## 5.1 网关路由新增

在 gateway-service 增加新路由：

- `id: api-ai`
- `Path=/api-ai/**`
- `uri=lb://trend-ai-analysis-service`
- `StripPrefix=1`

这样前端统一通过网关访问 AI 接口：

- `/api-ai/analysis/review`
- `/api-ai/analysis/parameter-suggestion`
- `/api-ai/analysis/risk-digest/{code}`

## 5.2 服务间调用建议

推荐调用链：

1. `trend-web` 调用 `/api-ai/**`
2. `trend-ai-analysis-service` 调用 `trend-trading-backtest-service`（按需）
3. `trend-ai-analysis-service` 调用 `market-data-service`（按需）
4. AI 服务整合结果并返回

## 5.3 数据存储建议（第一阶段）

可不引入 SQL，先做：

- Redis 缓存 AI 结果（短 TTL，例如 5~30 分钟）
- 输入参数 + 回测摘要作为缓存 key
- 模型调用失败时降级返回规则结果

---

## 6. 推荐模块与配置变更清单

## 6.1 新增模块

在根工程新增模块：

- `trend-ai-analysis-service`

建议基础依赖：

- `spring-boot-starter-web`
- `spring-boot-starter-actuator`
- `spring-cloud-starter-loadbalancer`
- `spring-cloud-starter-alibaba-nacos-discovery`
- `spring-cloud-starter-alibaba-nacos-config`
- （可选）Spring AI / Spring AI Alibaba 对应 starter
- （可选）`spring-boot-starter-data-redis`

## 6.2 Nacos Config 新增模板

建议新增：

- `infra/nacos-config/templates/trend-ai-analysis-service-dev.yaml`

建议配置项：

- 服务端口
- 回测服务 base-url
- 市场数据服务 base-url
- 模型配置（模型名、温度、超时）
- 降级开关（规则模式开关）
- 缓存 TTL

## 6.3 本地脚本联动改造

建议同步更新：

- `.tools/local_stack.py`：将 AI 服务加入启动/停止/状态检查
- `.tools/verify_local_migration.py`：新增 AI 健康检查与一个最小 AI 接口可用性检查
- `.tools/nacos_config_sync.py`：将 AI Data ID 纳入核心同步（可选）

---

## 7. 实施里程碑（建议 3 期）

## 第 1 期：最小可用（推荐先做）

范围：

- 新建 `trend-ai-analysis-service`
- 打通 `/analysis/review`
- 接入网关 `/api-ai/**`
- 支持规则摘要 + LLM 失败降级

验收标准：

- 前端可获取一份完整复盘报告
- 模型故障时接口仍可返回结构化摘要

## 第 2 期：参数助手

范围：

- 增加 `/analysis/parameter-suggestion`
- 增加网格试参与评分函数
- 输出推荐参数与解释

验收标准：

- 可输出至少 3 组候选参数
- 推荐参数可复现（同输入可重复）

## 第 3 期：风险预警总结

范围：

- 增加 `/analysis/risk-digest/{code}`
- 增加定时任务
- 增加风险信号规则库

验收标准：

- 能按调度周期稳定产出风险摘要
- 输出包含等级、信号与建议

---

## 8. 风险与治理建议

1. 幻觉风险：所有关键数字必须来自结构化计算结果，不允许模型凭空生成。
2. 成本控制：对相同请求启用缓存，避免重复调用模型。
3. 延迟控制：设置超时与并发上限，必要时返回“规则版摘要”。
4. 安全合规：响应中固定包含“非投资建议”声明。
5. 可观测性：新增 AI 调用成功率、耗时、降级次数指标。

---

## 9. 与当前项目目标的匹配结论

你当前项目目标是“熟悉 Spring Cloud 微服务”。在这个目标下，引入 AI 微服务是加分项，原因是：

- 能复用你现有的网关、注册发现、配置中心、监控链路
- 能展示一个真实的“业务能力扩展服务”场景
- 不必立即引入 SQL，也能完成端到端可演示能力

建议优先顺序：先做策略解释与复盘，再做参数助手，最后做风险预警总结。

---

## 10. 交付建议（本仓库下一步）

按最小改动启动开发：

1. 新建模块 `trend-ai-analysis-service`（先实现 `/analysis/review`）
2. 在 `gateway-service` 新增 `/api-ai/**` 路由
3. 在 `infra/nacos-config/templates` 新增 AI 服务配置模板
4. 在 `trend-web` 增加“AI 分析面板”入口（先展示复盘报告）
5. 更新本地脚本与验收脚本，把 AI 服务纳入一键链路
