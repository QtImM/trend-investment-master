<script setup lang="ts">
import type { EndpointStatus } from '../services/system-status';

defineProps<{
  endpointStatuses: EndpointStatus[];
  checking: boolean;
  checkedAt: string;
  requestPath: string;
  requestStatus: number | null;
  requestError: string;
  requestAt: string;
}>();

const emit = defineEmits<{
  refresh: [];
}>();
</script>

<template>
  <section class="table-card diagnostics-card">
    <div class="card-heading diagnostics-heading">
      <div>
        <span class="eyebrow">调试面板</span>
        <h3>最近一次请求与核心接口状态</h3>
      </div>
      <button type="button" class="action-button ghost compact" :disabled="checking" @click="emit('refresh')">
        {{ checking ? '检查中' : '刷新状态' }}
      </button>
    </div>

    <div class="diagnostics-grid">
      <article class="diagnostics-block">
        <span class="eyebrow">最近回测请求</span>
        <strong>{{ requestStatus ? `HTTP ${requestStatus}` : '等待请求' }}</strong>
        <code>{{ requestPath || '尚未发起回测请求' }}</code>
        <p v-if="requestError">{{ requestError }}</p>
        <p v-else>最近一次回测请求已记录到这里，后续调试时不用先开 Network 面板。</p>
        <small v-if="requestAt">请求时间：{{ requestAt }}</small>
      </article>

      <article class="diagnostics-block">
        <span class="eyebrow">核心接口状态</span>
        <div class="endpoint-list">
          <div v-for="item in endpointStatuses" :key="item.key" class="endpoint-item">
            <div>
              <strong>{{ item.label }}</strong>
              <code>{{ item.path }}</code>
            </div>
            <div class="endpoint-meta">
              <span class="pill" :class="item.ok ? 'gain' : 'loss'">
                {{ item.ok ? `HTTP ${item.status}` : item.status ? `HTTP ${item.status}` : '失败' }}
              </span>
              <small>{{ item.detail }}</small>
            </div>
          </div>
        </div>
        <small v-if="checkedAt">最近检查：{{ checkedAt }}</small>
      </article>
    </div>
  </section>
</template>
