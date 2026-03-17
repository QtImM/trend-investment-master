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
    />

    <p v-if="store.error" class="status-banner error">{{ store.error }}</p>
    <p v-else-if="store.loading" class="status-banner">正在同步回测结果...</p>

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
  </main>
</template>
