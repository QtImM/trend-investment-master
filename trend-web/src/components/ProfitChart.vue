<script setup lang="ts">
import * as echarts from 'echarts';
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';

const props = defineProps<{
  rows: Array<{
    date: string;
    closePoint: number;
    profitValue: number;
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
      data: props.rows.map((item) => item.date),
      axisLabel: { color: '#8892a6' },
      axisLine: { lineStyle: { color: '#394150' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#8892a6' },
      splitLine: { lineStyle: { color: '#2a3140' } },
    },
    series: [
      {
        name: props.currentIndex,
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { color: '#ff7b72', width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,123,114,0.35)' },
            { offset: 1, color: 'rgba(255,123,114,0.02)' },
          ]),
        },
        data: props.rows.map((item) => item.closePoint),
      },
      {
        name: '趋势投资',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { color: '#64d2ff', width: 2 },
        data: props.rows.map((item) => item.profitValue),
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
      <span class="eyebrow">收益曲线</span>
      <h3>指数与趋势收益对比</h3>
    </div>
    <div ref="root" class="chart-surface"></div>
  </section>
</template>
