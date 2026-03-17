<script setup lang="ts">
import * as echarts from 'echarts';
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';

const props = defineProps<{
  rows: Array<{
    year: string;
    indexIncome: number;
    trendIncome: number;
  }>;
  currentIndex: string;
}>();

const root = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | null = null;

function renderChart() {
  if (!root.value) {
    return;
  }
  if (!chart) {
    chart = echarts.init(root.value);
  }

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
    legend: {
      textStyle: { color: '#d4d9e2' },
    },
    grid: {
      left: 28,
      right: 18,
      top: 48,
      bottom: 28,
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: props.rows.map((item) => item.year),
      axisLabel: { color: '#8892a6' },
      axisLine: { lineStyle: { color: '#394150' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#8892a6',
        formatter: '{value}%',
      },
      splitLine: { lineStyle: { color: '#2a3140' } },
    },
    series: [
      {
        name: props.currentIndex,
        type: 'bar',
        itemStyle: { color: '#ffb86b' },
        data: props.rows.map((item) => item.indexIncome),
      },
      {
        name: '趋势投资',
        type: 'bar',
        itemStyle: { color: '#7cf0c3' },
        data: props.rows.map((item) => item.trendIncome),
      },
    ],
  });
}

function resizeChart() {
  chart?.resize();
}

onMounted(() => {
  renderChart();
  window.addEventListener('resize', resizeChart);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart);
  chart?.dispose();
  chart = null;
});

watch(() => [props.rows, props.currentIndex], renderChart, { deep: true });
</script>

<template>
  <section class="chart-card">
    <div class="card-heading">
      <span class="eyebrow">年度收益</span>
      <h3>指数与趋势收益分布</h3>
    </div>
    <div ref="root" class="chart-surface"></div>
  </section>
</template>
