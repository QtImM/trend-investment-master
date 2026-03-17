<script setup lang="ts">
const milestones = [
  '旧基础设施模块已经完成退场或主构建收缩。',
  'trend-web 已接入当前 Gateway 入口链路。',
  'trend-trading-backtest-view 已收缩为纯跳转壳层。',
  '后续重点将转向新前端扩展和业务服务现代化。',
];
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
