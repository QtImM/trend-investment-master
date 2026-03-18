package bupt.web;

import bupt.pojo.AnnualProfit;
import bupt.pojo.IndexData;
import bupt.pojo.Profit;
import bupt.pojo.Trade;
import bupt.service.BackTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BackTestController.class)
class BackTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BackTestService backTestService;

    @Test
    void shouldReturnEmptyResultWhenNoIndexDataAvailable() throws Exception {
        given(backTestService.listIndexData("000300")).willReturn(List.of());

        mockMvc.perform(get("/simulate/000300/20/1.01/0.99/0/null/null"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("未获取到可用于回测的指数数据"))
                .andExpect(jsonPath("$.requestedStartDate").value("null"))
                .andExpect(jsonPath("$.requestedEndDate").value("null"))
                .andExpect(jsonPath("$.indexDatas").isArray())
                .andExpect(jsonPath("$.trades").isArray())
                .andExpect(jsonPath("$.profits").isArray())
                .andExpect(jsonPath("$.years").value(0.0));
    }

    @Test
    void shouldSupportTrailingSlashRouteAndReturnSimulationResult() throws Exception {
        List<IndexData> indexDatas = List.of(
                createIndexData("2018-01-01", 100f),
                createIndexData("2018-01-02", 110f)
        );
        List<Profit> profits = List.of(
                createProfit("2018-01-01", 1000f),
                createProfit("2018-01-02", 1200f)
        );
        List<Trade> trades = List.of(new Trade());
        List<AnnualProfit> annualProfits = List.of(new AnnualProfit());

        Map<String, Object> simulateResult = new HashMap<>();
        simulateResult.put("profits", profits);
        simulateResult.put("trades", trades);
        simulateResult.put("winCount", 1);
        simulateResult.put("lossCount", 0);
        simulateResult.put("avgWinRate", 0.1f);
        simulateResult.put("avgLossRate", 0.0f);
        simulateResult.put("annualProfits", annualProfits);

        given(backTestService.listIndexData("000300")).willReturn(indexDatas);
        given(backTestService.simulate(anyInt(), anyFloat(), anyFloat(), anyFloat(), anyList())).willReturn(simulateResult);
        given(backTestService.getYear(anyList())).willReturn(1.0f);

        mockMvc.perform(get("/simulate/000300/20/1.01/0.99/0/null/null/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexStartDate").value("2018-01-01"))
                .andExpect(jsonPath("$.indexEndDate").value("2018-01-02"))
                .andExpect(jsonPath("$.winCount").value(1))
                .andExpect(jsonPath("$.lossCount").value(0))
                .andExpect(jsonPath("$.profits.length()").value(2))
                .andExpect(jsonPath("$.trades.length()").value(1));
    }

    private IndexData createIndexData(String date, float closePoint) {
        IndexData indexData = new IndexData();
        indexData.setDate(date);
        indexData.setClosePoint(closePoint);
        return indexData;
    }

    private Profit createProfit(String date, float value) {
        Profit profit = new Profit();
        profit.setDate(date);
        profit.setValue(value);
        return profit;
    }
}
