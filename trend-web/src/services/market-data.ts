import { http } from './http';
import type { IndexOption } from '../types/backtest';

export interface MarketIndexesResult {
  indexes: IndexOption[];
  source: 'market-data-service';
}

export async function fetchMarketIndexes(): Promise<MarketIndexesResult> {
  const { data } = await http.get<IndexOption[]>('/api-market/codes');
  return {
    indexes: data,
    source: 'market-data-service',
  };
}
