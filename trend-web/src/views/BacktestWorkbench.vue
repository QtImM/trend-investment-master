<script setup lang="ts">
import { onMounted } from 'vue';
import AnnualIncomeChart from '../components/AnnualIncomeChart.vue';
import MetricCards from '../components/MetricCards.vue';
import ParameterPanel from '../components/ParameterPanel.vue';
import ProfitChart from '../components/ProfitChart.vue';
import TradesTable from '../components/TradesTable.vue';
import { useBacktestStore } from '../stores/backtest';

const store = useBacktestStore();

onMounted(async () => {
  if (!store.initialized) {
    await store.bootstrap();
  }
});
</script>

<template>
  <main class="page-shell">
    <section class="hero-card">
      <div>
        <span class="eyebrow">Trend Web</span>
        <h1>Vue 3 回测工作台</h1>
        <p>
          这是一条新的前端迁移主线：用 Vue 3 + Vite + TypeScript + Pinia + ECharts
          重建旧的回测页面，同时保持现有后端接口不变。
        </p>
      </div>
      <div class="hero-badge">
        <span>当前入口</span>
        <strong>/</strong>
        <small>默认经 Gateway 转发</small>
      </div>
    </section>

    <ParameterPanel
      :indexes="store.indexes"
      :params="store.params"
      :min-date="store.indexStartDate"
      :max-date="store.indexEndDate"
      :disabled="store.loading"
      @patch="store.patchParams"
      @simulate="store.runSimulation"
      @reset-date-range="store.resetDateRange"
    />

    <section v-if="store.error" class="state-card error">
      <span class="eyebrow">异常状态</span>
      <h3>当前回测结果没有刷新成功</h3>
      <p>{{ store.error }}</p>
      <div class="state-actions">
        <button type="button" class="action-button primary" @click="store.runSimulation()">重试回测</button>
        <button type="button" class="action-button ghost" @click="store.clearError()">关闭提示</button>
        <a class="action-link" href="/legacy">查看旧页面</a>
      </div>
    </section>

    <section v-else-if="store.loading" class="state-card loading">
      <span class="eyebrow">同步中</span>
      <h3>正在拉取指数与回测结果</h3>
      <p>当前入口已经切到新前端，正在通过 Gateway 复用现有后端接口。</p>
    </section>

    <section v-else-if="store.initialized && !store.hasResults" class="state-card empty">
      <span class="eyebrow">空状态</span>
      <h3>暂时还没有可展示的回测结果</h3>
      <p>这通常意味着当前数据链路还没准备好，或者所选参数没有返回可用结果。</p>
      <div class="state-actions">
        <button type="button" class="action-button primary" @click="store.runSimulation(true)">重新初始化</button>
        <a class="action-link" href="/legacy">查看旧页面</a>
      </div>
    </section>

    <p v-if="store.hasResults && store.lastUpdatedAt" class="status-banner">
      最近更新时间：{{ store.lastUpdatedAt }}
    </p>

    <template v-if="store.hasResults">
      <MetricCards
        :years="store.years"
        :index-income-total="store.indexIncomeTotal"
        :index-income-annual="store.indexIncomeAnnual"
        :trend-income-total="store.trendIncomeTotal"
        :trend-income-annual="store.trendIncomeAnnual"
        :total-trades="store.totalTrades"
        :win-count="store.winCount"
        :loss-count="store.lossCount"
        :avg-win-rate="store.avgWinRate"
        :avg-loss-rate="store.avgLossRate"
        :win-ratio="store.winRatio"
      />

      <section class="chart-grid">
        <ProfitChart :rows="store.profitChartRows" :current-index="store.params.currentIndex" />
        <AnnualIncomeChart :rows="store.annualChartRows" :current-index="store.params.currentIndex" />
      </section>

      <TradesTable :annual-profits="store.annualProfits" :trades="store.trades" />
    </template>
  </main>
</template>
