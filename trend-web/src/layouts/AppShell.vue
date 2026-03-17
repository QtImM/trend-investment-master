<script setup lang="ts">
import { RouterLink, RouterView, useRoute } from 'vue-router';

const route = useRoute();

const navigationItems = [
  { name: 'overview', label: '回测总览', description: '指标、图表与参数控制' },
  { name: 'trades', label: '交易明细', description: '年度收益与逐笔交易' },
  { name: 'status', label: '迁移状态', description: '当前入口、兼容壳层与后续计划' },
];
</script>

<template>
  <div class="app-shell">
    <aside class="side-nav">
      <div class="brand-block">
        <span class="eyebrow">Trend Web</span>
        <h1>前端接管工作台</h1>
        <p>新的前端入口已经开始接管回测体验，旧视图服务只保留跳转壳层。</p>
      </div>

      <nav class="nav-list" aria-label="主导航">
        <RouterLink
          v-for="item in navigationItems"
          :key="item.name"
          :to="{ name: item.name }"
          class="nav-item"
          :class="{ active: route.name === item.name }"
        >
          <strong>{{ item.label }}</strong>
          <span>{{ item.description }}</span>
        </RouterLink>
      </nav>

      <div class="side-note">
        <span>兼容入口</span>
        <a href="/legacy">旧视图跳转入口</a>
      </div>
    </aside>

    <section class="shell-main">
      <RouterView />
    </section>
  </div>
</template>
