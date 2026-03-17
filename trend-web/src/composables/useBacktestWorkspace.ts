import { onMounted } from 'vue';
import { useBacktestStore } from '../stores/backtest';

export function useBacktestWorkspace() {
  const store = useBacktestStore();

  onMounted(async () => {
    if (!store.initialized) {
      await store.bootstrap();
    }
  });

  return store;
}
