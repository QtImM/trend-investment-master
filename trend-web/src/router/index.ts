import { createRouter, createWebHistory } from 'vue-router';
import BacktestWorkbench from '../views/BacktestWorkbench.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'backtest-workbench',
      component: BacktestWorkbench,
    },
  ],
});

export default router;
