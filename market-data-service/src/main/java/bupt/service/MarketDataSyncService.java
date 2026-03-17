package bupt.service;

import bupt.pojo.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketDataSyncService {

    @Autowired
    private MarketIndexCodeService marketIndexCodeService;
    @Autowired
    private MarketIndexDataService marketIndexDataService;

    public List<Index> refreshCodes() {
        return marketIndexCodeService.fresh();
    }

    public List<Index> refreshAll() {
        List<Index> indexes = marketIndexCodeService.fresh();
        for (Index index : indexes) {
            marketIndexDataService.fresh(index.getCode());
        }
        return indexes;
    }

    public void refreshIndexData(String code) {
        marketIndexDataService.fresh(code);
    }
}
