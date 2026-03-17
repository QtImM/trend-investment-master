import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('../layouts/AppShell.vue'),
      children: [
        {
          path: '',
          name: 'overview',
          component: () => import('../views/OverviewView.vue'),
        },
        {
          path: 'trades',
          name: 'trades',
          component: () => import('../views/TradesView.vue'),
        },
        {
          path: 'status',
          name: 'status',
          component: () => import('../views/StatusView.vue'),
        },
      ],
    },
  ],
});

export default router;
