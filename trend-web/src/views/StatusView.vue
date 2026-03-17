<script setup lang="ts">
import { computed } from 'vue';
import { useBacktestWorkspace } from '../composables/useBacktestWorkspace';

const store = useBacktestWorkspace();
const milestones = [
  '旧基础设施模块已经完成退场或主构建收缩。',
  'trend-web 已接入当前 Gateway 入口链路。',
  'trend-trading-backtest-view 已收缩为纯跳转壳层。',
  'trend-web 已开始优先读取 market-data-service。',
];

const marketSourceText = computed(() => {
  if (store.indexSource === 'market-data-service') {
    return 'market-data-service';
  }
  if (store.indexSource === 'index-codes-service') {
    return 'index-codes-service（兼容回退）';
  }
  return '等待初始化';
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
            <span>默认优先走新收敛模块，异常时自动回退旧 codes 服务</span>
          </div>
          <div class="status-item">
            <strong>/api-backtest/**</strong>
            <span>回测计算链路保持不变，继续由 backtest-service 承接</span>
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
  </main>
</template>
