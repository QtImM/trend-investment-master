import { http } from './http';
import type { IndexOption } from '../types/backtest';

export interface MarketIndexesResult {
  indexes: IndexOption[];
  source: 'market-data-service' | 'index-codes-service';
}

export async function fetchMarketIndexes(): Promise<MarketIndexesResult> {
  try {
    const { data } = await http.get<IndexOption[]>('/api-market/codes');
    return {
      indexes: data,
      source: 'market-data-service',
    };
  } catch {
    const { data } = await http.get<IndexOption[]>('/api-codes/codes');
    return {
      indexes: data,
      source: 'index-codes-service',
    };
  }
}
