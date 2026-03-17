<script setup lang="ts">
defineProps<{
  loading: boolean;
  initialized: boolean;
  hasResults: boolean;
  error: string;
  lastUpdatedAt: string;
}>();

const emit = defineEmits<{
  retry: [];
  reset: [];
  clearError: [];
}>();
</script>

<template>
  <section v-if="error" class="state-card error">
    <span class="eyebrow">异常状态</span>
    <h3>当前回测结果没有刷新成功</h3>
    <p>{{ error }}</p>
    <div class="state-actions">
      <button type="button" class="action-button primary" @click="emit('retry')">重试回测</button>
      <button type="button" class="action-button ghost" @click="emit('clearError')">关闭提示</button>
      <a class="action-link" href="/legacy">查看旧页面</a>
    </div>
  </section>

  <section v-else-if="loading" class="state-card loading">
    <span class="eyebrow">同步中</span>
    <h3>正在拉取指数与回测结果</h3>
    <p>当前入口已经切到新前端，正在通过 Gateway 复用现有后端接口。</p>
  </section>

  <section v-else-if="initialized && !hasResults" class="state-card empty">
    <span class="eyebrow">空状态</span>
    <h3>暂时还没有可展示的回测结果</h3>
    <p>这通常意味着当前数据链路还没准备好，或者所选参数没有返回可用结果。</p>
    <div class="state-actions">
      <button type="button" class="action-button primary" @click="emit('reset')">重新初始化</button>
      <a class="action-link" href="/legacy">查看旧页面</a>
    </div>
  </section>

  <p v-if="hasResults && lastUpdatedAt" class="status-banner">
    最近更新时间：{{ lastUpdatedAt }}
  </p>
</template>
