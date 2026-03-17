import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { fetchIndexes, simulateBacktest } from '../services/backtest';
import type {
  AnnualProfitRecord,
  BacktestParams,
  BacktestResponse,
  IndexDataPoint,
  IndexOption,
  ProfitPoint,
  TradeRecord,
} from '../types/backtest';

const defaultParams = (): BacktestParams => ({
  currentIndex: '000300',
  ma: 20,
  buyThreshold: 1.01,
  sellThreshold: 0.99,
  serviceCharge: 0,
  startDate: null,
  endDate: null,
});

export const useBacktestStore = defineStore('backtest', () => {
  const indexes = ref<IndexOption[]>([]);
  const params = ref<BacktestParams>(defaultParams());
  const loading = ref(false);
  const initialized = ref(false);
  const error = ref('');
  const lastUpdatedAt = ref('');
  const indexSource = ref<'market-data-service' | 'index-codes-service' | ''>('');

  const indexDatas = ref<IndexDataPoint[]>([]);
  const profits = ref<ProfitPoint[]>([]);
  const trades = ref<TradeRecord[]>([]);
  const annualProfits = ref<AnnualProfitRecord[]>([]);

  const indexStartDate = ref('');
  const indexEndDate = ref('');
  const years = ref(0);
  const indexIncomeTotal = ref(0);
  const indexIncomeAnnual = ref(0);
  const trendIncomeTotal = ref(0);
  const trendIncomeAnnual = ref(0);
  const winCount = ref(0);
  const lossCount = ref(0);
  const avgWinRate = ref(0);
  const avgLossRate = ref(0);

  const totalTrades = computed(() => winCount.value + lossCount.value);
  const winRatio = computed(() => (totalTrades.value ? winCount.value / totalTrades.value : 0));
  const hasResults = computed(() => indexDatas.value.length > 0 && profits.value.length > 0);
  const hasIndexes = computed(() => indexes.value.length > 0);

  const profitChartRows = computed(() =>
    indexDatas.value.map((point, index) => ({
      date: point.date,
      closePoint: Number(point.closePoint ?? 0),
      profitValue: Number(profits.value[index]?.value ?? 0),
    })),
  );

  const annualChartRows = computed(() =>
    annualProfits.value.map((item) => ({
      year: String(item.year),
      indexIncome: Number(item.indexIncome ?? 0) * 100,
      trendIncome: Number(item.trendIncome ?? 0) * 100,
    })),
  );

  function applyResult(result: BacktestResponse) {
    indexDatas.value = result.indexDatas ?? [];
    profits.value = result.profits ?? [];
    trades.value = result.trades ?? [];
    annualProfits.value = result.annualProfits ?? [];
    indexStartDate.value = result.indexStartDate ?? '';
    indexEndDate.value = result.indexEndDate ?? '';
    years.value = Number(result.years ?? 0);
    indexIncomeTotal.value = Number(result.indexIncomeTotal ?? 0);
    indexIncomeAnnual.value = Number(result.indexIncomeAnnual ?? 0);
    trendIncomeTotal.value = Number(result.trendIncomeTotal ?? 0);
    trendIncomeAnnual.value = Number(result.trendIncomeAnnual ?? 0);
    winCount.value = Number(result.winCount ?? 0);
    lossCount.value = Number(result.lossCount ?? 0);
    avgWinRate.value = Number(result.avgWinRate ?? 0);
    avgLossRate.value = Number(result.avgLossRate ?? 0);
    lastUpdatedAt.value = new Date().toLocaleString('zh-CN');

    if (!params.value.startDate) {
      params.value.startDate = result.indexStartDate ?? null;
    }
    if (!params.value.endDate) {
      params.value.endDate = result.indexEndDate ?? null;
    }
  }

  async function bootstrap() {
    loading.value = true;
    error.value = '';
    try {
      const marketIndexesResult = await fetchIndexes();
      indexes.value = marketIndexesResult.indexes;
      indexSource.value = marketIndexesResult.source;
      initialized.value = true;
      if (!indexes.value.some((item) => item.code === params.value.currentIndex) && indexes.value.length > 0) {
        params.value.currentIndex = indexes.value[0].code;
      }
      await runSimulation(true);
    } catch (err) {
      error.value = err instanceof Error ? err.message : '初始化失败';
    } finally {
      loading.value = false;
    }
  }

  async function runSimulation(resetDate = false) {
    if (!hasIndexes.value) {
      error.value = '当前没有可用指数数据，请先检查 /api-market/** 或 /api-codes/** 链路。';
      return;
    }

    if (params.value.startDate && params.value.endDate && params.value.startDate > params.value.endDate) {
      error.value = '开始日期不能晚于结束日期。';
      return;
    }

    loading.value = true;
    error.value = '';
    try {
      if (resetDate) {
        params.value.startDate = null;
        params.value.endDate = null;
      }
      const result = await simulateBacktest(params.value);
      applyResult(result);
    } catch (err) {
      error.value = err instanceof Error ? err.message : '回测失败';
    } finally {
      loading.value = false;
    }
  }

  function patchParams(nextParams: Partial<BacktestParams>) {
    params.value = {
      ...params.value,
      ...nextParams,
    };
  }

  function resetDateRange() {
    patchParams({
      startDate: null,
      endDate: null,
    });
  }

  function clearError() {
    error.value = '';
  }

  return {
    indexes,
    params,
    loading,
    initialized,
    error,
    indexDatas,
    profits,
    trades,
    annualProfits,
    indexStartDate,
    indexEndDate,
    years,
    indexIncomeTotal,
    indexIncomeAnnual,
    trendIncomeTotal,
    trendIncomeAnnual,
    winCount,
    lossCount,
    avgWinRate,
    avgLossRate,
    totalTrades,
    winRatio,
    profitChartRows,
    annualChartRows,
    hasResults,
    hasIndexes,
    lastUpdatedAt,
    indexSource,
    bootstrap,
    runSimulation,
    patchParams,
    resetDateRange,
    clearError,
  };
});
