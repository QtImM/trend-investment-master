<script setup lang="ts">
import type { AnnualProfitRecord, TradeRecord } from '../types/backtest';

defineProps<{
  annualProfits: AnnualProfitRecord[];
  trades: TradeRecord[];
}>();

function toPercent(value: number) {
  return `${(value * 100).toFixed(2)}%`;
}

function toMoney(value: number) {
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
}
</script>

<template>
  <section class="tables-grid">
    <article class="table-card">
      <div class="card-heading">
        <span class="eyebrow">年度明细</span>
        <h3>年度收益对比表</h3>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>年份</th>
              <th>指数收益</th>
              <th>趋势收益</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in annualProfits" :key="String(item.year)">
              <td>{{ item.year }}</td>
              <td>{{ toPercent(item.indexIncome) }}</td>
              <td>{{ toPercent(item.trendIncome) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <article class="table-card span-full">
      <div class="card-heading">
        <span class="eyebrow">交易明细</span>
        <h3>趋势策略逐笔交易</h3>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>结果</th>
              <th>买入日期</th>
              <th>买入点位</th>
              <th>卖出日期</th>
              <th>卖出点位</th>
              <th>盈亏比率</th>
              <th>1000 元收益</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in trades" :key="`${item.buyDate}-${item.sellDate}`">
              <td>
                <span :class="item.sellClosePoint > item.buyClosePoint ? 'pill gain' : 'pill loss'">
                  {{ item.sellClosePoint > item.buyClosePoint ? '盈利' : '亏损' }}
                </span>
              </td>
              <td>{{ item.buyDate }}</td>
              <td>{{ item.buyClosePoint }}</td>
              <td>{{ item.sellDate }}</td>
              <td>{{ item.sellClosePoint || 'n/a' }}</td>
              <td>{{ item.sellClosePoint ? toPercent((item.sellClosePoint - item.buyClosePoint) / item.buyClosePoint) : 'n/a' }}</td>
              <td>{{ item.sellClosePoint ? toMoney(item.rate * 1000) : 'n/a' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>
  </section>
</template>
