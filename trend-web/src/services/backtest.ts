import axios from 'axios';
import { http } from './http';
import { fetchMarketIndexes } from './market-data';
import type { BacktestParams, BacktestResponse } from '../types/backtest';

function normalizePathValue(value: string | null): string {
  return value && value.trim().length > 0 ? value : 'null';
}

export const fetchIndexes = fetchMarketIndexes;

export function buildBacktestRequestPath(params: BacktestParams) {
  return [
    params.currentIndex,
    params.ma,
    params.buyThreshold,
    params.sellThreshold,
    params.serviceCharge,
    normalizePathValue(params.startDate),
    normalizePathValue(params.endDate),
  ].join('/');
}

export async function simulateBacktest(params: BacktestParams) {
  const path = buildBacktestRequestPath(params);

  try {
    const { data } = await http.get<BacktestResponse>(`/api-backtest/simulate/${path}/`);
    return data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;
      const statusLabel = status ? `（${status}）` : '';
      throw new Error(`回测接口请求失败${statusLabel}：GET /api-backtest/simulate/${path}/`);
    }
    throw error;
  }
}
