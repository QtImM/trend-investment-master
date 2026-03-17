<script setup lang="ts">
import AnnualIncomeChart from '../components/AnnualIncomeChart.vue';
import MetricCards from '../components/MetricCards.vue';
import ParameterPanel from '../components/ParameterPanel.vue';
import ProfitChart from '../components/ProfitChart.vue';
import ViewStatePanel from '../components/ViewStatePanel.vue';
import { useBacktestWorkspace } from '../composables/useBacktestWorkspace';

const store = useBacktestWorkspace();
</script>

<template>
  <main class="page-shell">
    <section class="hero-card">
      <div>
        <span class="eyebrow">回测总览</span>
        <h2>策略参数、收益曲线和核心指标</h2>
        <p>
          这一页保留旧页面最核心的“调参数看结果”链路，但已经完全运行在
          Vue 3 + Vite + TypeScript 的新前端里。
        </p>
      </div>
      <div class="hero-badge">
        <span>当前视图</span>
        <strong>Overview</strong>
        <small>{{ store.indexSource || '等待初始化' }}</small>
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

    <ViewStatePanel
      :loading="store.loading"
      :initialized="store.initialized"
      :has-results="store.hasResults"
      :error="store.error"
      :last-updated-at="store.lastUpdatedAt"
      @retry="store.runSimulation()"
      @reset="store.runSimulation(true)"
      @clear-error="store.clearError"
    />

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
    </template>
  </main>
</template>
