<script setup lang="ts">
import { computed } from 'vue';
import type { BacktestParams, IndexOption } from '../types/backtest';

const props = defineProps<{
  indexes: IndexOption[];
  params: BacktestParams;
  minDate: string;
  maxDate: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  patch: [payload: Partial<BacktestParams>];
  simulate: [resetDate?: boolean];
  resetDateRange: [];
}>();

const maOptions = [5, 10, 20, 60];
const buyThresholdOptions = Array.from({ length: 9 }, (_, index) => Number((1 + (index + 1) / 100).toFixed(2)));
const sellThresholdOptions = Array.from({ length: 10 }, (_, index) => Number((1 - index / 100).toFixed(2)));
const serviceChargeOptions = [
  { label: '无', value: 0 },
  { label: '0.1%', value: 0.001 },
  { label: '0.15%', value: 0.0015 },
  { label: '0.2%', value: 0.002 },
];

const canUseDateRange = computed(() => Boolean(props.minDate && props.maxDate));

function patch(payload: Partial<BacktestParams>) {
  emit('patch', payload);
}

function run(resetDate = false) {
  emit('simulate', resetDate);
}
</script>

<template>
  <section class="panel-shell">
    <div class="section-title">
      <span class="eyebrow">回测参数</span>
      <h2>策略控制台</h2>
      <p>新的前端工作台默认经 Gateway 转发接口，不再依赖旧的 Thymeleaf 页面拼装。</p>
    </div>

    <div class="form-grid">
      <label class="field">
        <span>指数</span>
        <select
          :value="params.currentIndex"
          :disabled="disabled"
          @change="patch({ currentIndex: ($event.target as HTMLSelectElement).value }); run(true);"
        >
          <option v-for="item in indexes" :key="item.code" :value="item.code">
            {{ item.name }} ({{ item.code }})
          </option>
        </select>
      </label>

      <label class="field">
        <span>均线 MA</span>
        <select
          :value="params.ma"
          :disabled="disabled"
          @change="patch({ ma: Number(($event.target as HTMLSelectElement).value) }); run();"
        >
          <option v-for="item in maOptions" :key="item" :value="item">{{ item }} 日</option>
        </select>
      </label>

      <label class="field">
        <span>购买阈值</span>
        <select
          :value="params.buyThreshold"
          :disabled="disabled"
          @change="patch({ buyThreshold: Number(($event.target as HTMLSelectElement).value) }); run();"
        >
          <option v-for="item in buyThresholdOptions" :key="item" :value="item">{{ item.toFixed(2) }}</option>
        </select>
      </label>

      <label class="field">
        <span>出售阈值</span>
        <select
          :value="params.sellThreshold"
          :disabled="disabled"
          @change="patch({ sellThreshold: Number(($event.target as HTMLSelectElement).value) }); run();"
        >
          <option v-for="item in sellThresholdOptions" :key="item" :value="item">{{ item.toFixed(2) }}</option>
        </select>
      </label>

      <label class="field">
        <span>手续费</span>
        <select
          :value="params.serviceCharge"
          :disabled="disabled"
          @change="patch({ serviceCharge: Number(($event.target as HTMLSelectElement).value) }); run();"
        >
          <option v-for="item in serviceChargeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
        </select>
      </label>

      <label class="field">
        <span>开始日期</span>
        <input
          type="date"
          :value="params.startDate ?? ''"
          :min="minDate || undefined"
          :max="maxDate || undefined"
          :disabled="disabled || !canUseDateRange"
          @change="patch({ startDate: ($event.target as HTMLInputElement).value || null }); run();"
        />
      </label>

      <label class="field">
        <span>结束日期</span>
        <input
          type="date"
          :value="params.endDate ?? ''"
          :min="minDate || undefined"
          :max="maxDate || undefined"
          :disabled="disabled || !canUseDateRange"
          @change="patch({ endDate: ($event.target as HTMLInputElement).value || null }); run();"
        />
      </label>
    </div>

    <div class="action-row">
      <button type="button" class="action-button primary" :disabled="disabled" @click="run()">
        重新回测
      </button>
      <button
        type="button"
        class="action-button ghost"
        :disabled="disabled || !canUseDateRange"
        @click="emit('resetDateRange'); run(true);"
      >
        重置日期
      </button>
    </div>
  </section>
</template>
