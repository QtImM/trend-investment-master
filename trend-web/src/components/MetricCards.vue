<script setup lang="ts">
const props = defineProps<{
  years: number;
  indexIncomeTotal: number;
  indexIncomeAnnual: number;
  trendIncomeTotal: number;
  trendIncomeAnnual: number;
  totalTrades: number;
  winCount: number;
  lossCount: number;
  avgWinRate: number;
  avgLossRate: number;
  winRatio: number;
}>();

function toPercent(value: number) {
  return `${(value * 100).toFixed(2)}%`;
}

function toMoney(value: number) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
  }).format(value);
}
</script>

<template>
  <section class="metrics-grid">
    <article class="metric-card">
      <span class="metric-label">指数收益</span>
      <strong>{{ toPercent(indexIncomeTotal) }}</strong>
      <p>{{ years.toFixed(2) }} 年，1000 元约为 {{ toMoney((indexIncomeTotal + 1) * 1000) }}</p>
    </article>
    <article class="metric-card accent">
      <span class="metric-label">趋势收益</span>
      <strong>{{ toPercent(trendIncomeTotal) }}</strong>
      <p>年化 {{ toPercent(trendIncomeAnnual) }}，1000 元约为 {{ toMoney((trendIncomeTotal + 1) * 1000) }}</p>
    </article>
    <article class="metric-card">
      <span class="metric-label">相对超额</span>
      <strong>{{ toPercent(trendIncomeTotal - indexIncomeTotal) }}</strong>
      <p>指数年化 {{ toPercent(indexIncomeAnnual) }}</p>
    </article>
    <article class="metric-card">
      <span class="metric-label">交易统计</span>
      <strong>{{ totalTrades }}</strong>
      <p>胜率 {{ (winRatio * 100).toFixed(2) }}%，盈利 {{ winCount }} / 亏损 {{ lossCount }}</p>
      <p>平均盈利 {{ toPercent(avgWinRate) }}，平均亏损 {{ toPercent(avgLossRate) }}</p>
    </article>
  </section>
</template>
