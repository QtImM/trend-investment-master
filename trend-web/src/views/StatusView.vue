<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import SystemStatusPanel from '../components/SystemStatusPanel.vue';
import { fetchEndpointStatuses, type EndpointStatus } from '../services/system-status';
import { useBacktestWorkspace } from '../composables/useBacktestWorkspace';

const store = useBacktestWorkspace();
const endpointStatuses = ref<EndpointStatus[]>([]);
const checking = ref(false);
const checkedAt = ref('');
const milestones = [
  '旧基础设施模块已经完成退场或主构建收缩。',
  'trend-web 已接入当前 Gateway 入口链路。',
  'trend-trading-backtest-view 已收缩为纯跳转壳层。',
  'trend-web 已开始优先读取 market-data-service。',
  '当前状态页已经可以直接展示最近一次回测请求和核心接口检查结果。',
];

const marketSourceText = computed(() => {
  if (store.indexSource === 'market-data-service') {
    return 'market-data-service';
  }
  return '等待初始化';
});

async function refreshStatuses() {
  checking.value = true;
  try {
    endpointStatuses.value = await fetchEndpointStatuses();
    checkedAt.value = new Date().toLocaleString('zh-CN');
  } finally {
    checking.value = false;
  }
}

onMounted(async () => {
  await refreshStatuses();
});
</script>

<template>
  <main class="page-shell">
    <section class="hero-card compact">
      <div>
        <span class="eyebrow">迁移状态</span>
        <h2>当前入口、兼容壳层与下一阶段计划</h2>
        <p>这页专门承载迁移状态说明，让新前端不只是“业务页集合”，而是有自己的产品化结构。</p>
      </div>
      <div class="hero-badge">
        <span>当前阶段</span>
        <strong>接管中</strong>
        <small>前端已开始替代旧视图入口</small>
      </div>
    </section>

    <section class="status-grid">
      <article class="table-card">
        <div class="card-heading">
          <span class="eyebrow">当前入口</span>
          <h3>访问路径</h3>
        </div>
        <div class="status-list">
          <div class="status-item">
            <strong>/trend-web/</strong>
            <span>新前端默认入口，经 Gateway 转发</span>
          </div>
          <div class="status-item">
            <strong>/api-market/**</strong>
            <span>市场数据收敛试点入口，当前前端优先读取</span>
          </div>
          <div class="status-item">
            <strong>/</strong>
            <span>旧视图服务入口，当前会跳转到 trend-web</span>
          </div>
          <div class="status-item">
            <strong>/legacy</strong>
            <span>兼容入口，当前同样跳到新前端，不再承载旧页面实现</span>
          </div>
        </div>
      </article>

      <article class="table-card">
        <div class="card-heading">
          <span class="eyebrow">数据来源</span>
          <h3>当前读取链路</h3>
        </div>
        <div class="status-list">
          <div class="status-item">
            <strong>{{ marketSourceText }}</strong>
            <span>默认直接走 market-data-service，旧 codes 回退链路已从新前端默认路径移除</span>
          </div>
          <div class="status-item">
            <strong>/api-backtest/**</strong>
            <span>回测计算链路继续由 backtest-service 承接，但其市场数据读取默认也已转向 market-data-service</span>
          </div>
        </div>
      </article>

      <article class="table-card">
        <div class="card-heading">
          <span class="eyebrow">迁移里程碑</span>
          <h3>已完成事项</h3>
        </div>
        <ul class="milestone-list">
          <li v-for="item in milestones" :key="item">{{ item }}</li>
        </ul>
      </article>
    </section>

    <SystemStatusPanel
      :endpoint-statuses="endpointStatuses"
      :checking="checking"
      :checked-at="checkedAt"
      :request-path="store.lastRequestPath"
      :request-status="store.lastRequestStatus"
      :request-error="store.lastRequestError"
      :request-at="store.lastRequestAt"
      @refresh="refreshStatuses"
    />
  </main>
</template>
