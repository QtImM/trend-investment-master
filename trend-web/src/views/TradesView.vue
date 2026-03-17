<script setup lang="ts">
import ParameterPanel from '../components/ParameterPanel.vue';
import TradesTable from '../components/TradesTable.vue';
import ViewStatePanel from '../components/ViewStatePanel.vue';
import { useBacktestWorkspace } from '../composables/useBacktestWorkspace';

const store = useBacktestWorkspace();
</script>

<template>
  <main class="page-shell">
    <section class="hero-card compact">
      <div>
        <span class="eyebrow">交易明细</span>
        <h2>年度收益与逐笔交易拆开看</h2>
        <p>这里把旧页面里最容易堆在一起的表格信息单独拆成一页，后续继续扩展交互会更顺。</p>
      </div>
      <div class="hero-badge">
        <span>交易次数</span>
        <strong>{{ store.totalTrades }}</strong>
        <small>按当前参数统计</small>
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

    <TradesTable v-if="store.hasResults" :annual-profits="store.annualProfits" :trades="store.trades" />
  </main>
</template>
