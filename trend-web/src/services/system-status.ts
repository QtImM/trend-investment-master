import axios from 'axios';
import { http } from './http';

export interface EndpointStatus {
  key: string;
  label: string;
  path: string;
  ok: boolean;
  status: number | null;
  detail: string;
}

const endpointDefinitions = [
  { key: 'trend-web', label: '前端入口', path: '/trend-web/' },
  { key: 'api-market', label: '市场数据接口', path: '/api-market/codes' },
  { key: 'api-view', label: '视图服务健康检查', path: '/api-view/actuator/health' },
  { key: 'api-backtest', label: '回测服务健康检查', path: '/api-backtest/actuator/health' },
];

async function checkEndpoint(path: string) {
  try {
    const response = await http.get(path);
    return {
      ok: true,
      status: response.status,
      detail: '可访问',
    };
  } catch (error) {
    if (axios.isAxiosError(error)) {
      return {
        ok: false,
        status: error.response?.status ?? null,
        detail: error.response?.status ? `请求失败 ${error.response.status}` : '请求失败',
      };
    }
    return {
      ok: false,
      status: null,
      detail: '请求失败',
    };
  }
}

export async function fetchEndpointStatuses(): Promise<EndpointStatus[]> {
  const statuses = await Promise.all(
    endpointDefinitions.map(async (item) => {
      const result = await checkEndpoint(item.path);
      return {
        ...item,
        ...result,
      };
    }),
  );

  return statuses;
}
