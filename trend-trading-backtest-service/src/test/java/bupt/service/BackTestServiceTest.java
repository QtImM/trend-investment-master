package bupt.service;

import bupt.client.IndexDataGateway;
import bupt.pojo.AnnualProfit;
import bupt.pojo.IndexData;
import bupt.pojo.Profit;
import bupt.pojo.Trade;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackTestServiceTest {

    @Test
    void shouldReverseRemoteDataInListIndexData() {
        List<IndexData> remoteResult = new ArrayList<>(List.of(
                createIndexData("2020-01-01", 100f),
                createIndexData("2020-01-02", 110f),
                createIndexData("2020-01-03", 120f)
        ));
        BackTestService backTestService = new BackTestService(code -> remoteResult);

        List<IndexData> result = backTestService.listIndexData("000300");

        assertEquals(3, result.size());
        assertEquals("2020-01-03", result.get(0).getDate());
        assertEquals("2020-01-02", result.get(1).getDate());
        assertEquals("2020-01-01", result.get(2).getDate());
    }

    @Test
    void shouldReturnZeroYearsWhenDataIsMissingOrTooShort() {
        BackTestService backTestService = new BackTestService(code -> List.of());

        assertEquals(0.0f, backTestService.getYear(null), 0.0f);
        assertEquals(0.0f, backTestService.getYear(List.of()), 0.0f);
        assertEquals(0.0f, backTestService.getYear(List.of(createIndexData("2020-01-01", 100f))), 0.0f);
    }

    @Test
    void shouldReturnStableEmptySimulationResultWhenIndexDataIsEmpty() {
        BackTestService backTestService = new BackTestService(code -> List.of());

        Map<String, Object> result = backTestService.simulate(20, 0.99f, 1.01f, 0.001f, List.of());

        assertTrue(((List<?>) result.get("profits")).isEmpty());
        assertTrue(((List<?>) result.get("trades")).isEmpty());
        assertTrue(((List<?>) result.get("annualProfits")).isEmpty());
        assertEquals(0, result.get("winCount"));
        assertEquals(0, result.get("lossCount"));
        assertEquals(0.0f, (Float) result.get("avgWinRate"), 0.0f);
        assertEquals(0.0f, (Float) result.get("avgLossRate"), 0.0f);
    }

    @Test
    void shouldProduceTradeStatsAndAnnualProfitsForTypicalSimulation() {
        BackTestService backTestService = new BackTestService(noopGateway());
        List<IndexData> indexDatas = List.of(
                createIndexData("2020-01-01", 100f),
                createIndexData("2020-01-02", 100f),
                createIndexData("2020-01-03", 120f),
                createIndexData("2020-01-04", 80f)
        );

        Map<String, Object> result = backTestService.simulate(1, 0.99f, 1.01f, 0.001f, indexDatas);

        @SuppressWarnings("unchecked")
        List<Profit> profits = (List<Profit>) result.get("profits");
        @SuppressWarnings("unchecked")
        List<Trade> trades = (List<Trade>) result.get("trades");
        @SuppressWarnings("unchecked")
        List<AnnualProfit> annualProfits = (List<AnnualProfit>) result.get("annualProfits");

        assertEquals(4, profits.size());
        assertEquals(1, trades.size());
        assertEquals("2020-01-03", trades.get(0).getBuyDate());
        assertEquals("2020-01-04", trades.get(0).getSellDate());
        assertEquals(0, result.get("winCount"));
        assertEquals(1, result.get("lossCount"));
        assertEquals(0.0f, (Float) result.get("avgWinRate"), 0.0f);
        assertTrue((Float) result.get("avgLossRate") < 0);
        assertFalse(annualProfits.isEmpty());
        assertEquals(2020, annualProfits.get(0).getYear());
    }

    private IndexDataGateway noopGateway() {
        return code -> List.of();
    }

    private IndexData createIndexData(String date, float closePoint) {
        IndexData indexData = new IndexData();
        indexData.setDate(date);
        indexData.setClosePoint(closePoint);
        return indexData;
    }
}
