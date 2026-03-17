package bupt.client;

import bupt.pojo.IndexData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "backtest.remote.index-data", name = "mode", havingValue = "feign", matchIfMissing = true)
public class FeignIndexDataGateway implements IndexDataTransportGateway {
    private final IndexDataClient indexDataClient;

    public FeignIndexDataGateway(IndexDataClient indexDataClient) {
        this.indexDataClient = indexDataClient;
    }

    @Override
    public List<IndexData> getIndexData(String code) {
        return indexDataClient.getIndexData(code);
    }
}
