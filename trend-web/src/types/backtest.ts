export interface IndexOption {
  code: string;
  name: string;
}

export interface IndexDataPoint {
  date: string;
  closePoint: number;
}

export interface ProfitPoint {
  date?: string;
  value: number;
}

export interface TradeRecord {
  buyDate: string;
  buyClosePoint: number;
  sellDate: string;
  sellClosePoint: number;
  rate: number;
}

export interface AnnualProfitRecord {
  year: string | number;
  indexIncome: number;
  trendIncome: number;
}

export interface BacktestParams {
  currentIndex: string;
  ma: number;
  buyThreshold: number;
  sellThreshold: number;
  serviceCharge: number;
  startDate: string | null;
  endDate: string | null;
}

export interface BacktestResponse {
  indexDatas: IndexDataPoint[];
  indexStartDate: string;
  indexEndDate: string;
  profits: ProfitPoint[];
  trades: TradeRecord[];
  years: number;
  indexIncomeTotal: number;
  indexIncomeAnnual: number;
  trendIncomeTotal: number;
  trendIncomeAnnual: number;
  winCount: number;
  lossCount: number;
  avgWinRate: number;
  avgLossRate: number;
  annualProfits: AnnualProfitRecord[];
}
