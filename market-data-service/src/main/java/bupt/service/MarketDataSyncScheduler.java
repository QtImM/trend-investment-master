package bupt.service;

import cn.hutool.core.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MarketDataSyncScheduler {

    @Autowired
    private MarketDataSyncService marketDataSyncService;

    @Scheduled(
            fixedDelayString = "${market.sync.interval-ms:60000}",
            initialDelayString = "${market.sync.initial-delay-ms:15000}"
    )
    public void refreshAll() {
        System.out.println("market-data-service 定时同步启动：" + DateUtil.now());
        marketDataSyncService.refreshAll();
    }
}
