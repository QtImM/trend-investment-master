import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

const gatewayTarget = process.env.VITE_GATEWAY_TARGET ?? 'http://127.0.0.1:8032';

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api-codes': {
        target: gatewayTarget,
        changeOrigin: true,
      },
      '/api-backtest': {
        target: gatewayTarget,
        changeOrigin: true,
      },
      '/api-view': {
        target: gatewayTarget,
        changeOrigin: true,
      },
    },
  },
});
