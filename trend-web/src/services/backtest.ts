import { http } from './http';
import type { BacktestParams, BacktestResponse, IndexOption } from '../types/backtest';

function normalizePathValue(value: string | null): string {
  return value && value.trim().length > 0 ? value : 'null';
}

export async function fetchIndexes() {
  const { data } = await http.get<IndexOption[]>('/api-codes/codes');
  return data;
}

export async function simulateBacktest(params: BacktestParams) {
  const path = [
    params.currentIndex,
    params.ma,
    params.buyThreshold,
    params.sellThreshold,
    params.serviceCharge,
    normalizePathValue(params.startDate),
    normalizePathValue(params.endDate),
  ].join('/');

  const { data } = await http.get<BacktestResponse>(`/api-backtest/simulate/${path}/`);
  return data;
}
