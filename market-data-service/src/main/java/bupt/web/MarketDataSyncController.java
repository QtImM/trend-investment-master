package bupt.web;

import bupt.pojo.Index;
import bupt.service.MarketDataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MarketDataSyncController {

    @Autowired
    private MarketDataSyncService marketDataSyncService;

    @GetMapping("/sync/codes")
    public List<Index> refreshCodes() {
        return marketDataSyncService.refreshCodes();
    }

    @GetMapping("/sync/all")
    public String refreshAll() {
        List<Index> indexes = marketDataSyncService.refreshAll();
        return "refreshed " + indexes.size() + " indexes";
    }

    @GetMapping("/sync/data/{code}")
    public String refreshIndexData(@PathVariable("code") String code) {
        marketDataSyncService.refreshIndexData(code);
        return "refreshed " + code;
    }
}
