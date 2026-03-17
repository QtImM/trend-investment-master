package bupt.client;

import bupt.pojo.IndexData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndexDataClientFeignHystrix implements IndexDataClient {
    private final IndexDataFallbackGateway indexDataFallbackGateway;

    public IndexDataClientFeignHystrix(IndexDataFallbackGateway indexDataFallbackGateway) {
        this.indexDataFallbackGateway = indexDataFallbackGateway;
    }

    @Override
    public List<IndexData> getIndexData(String code) {
        return indexDataFallbackGateway.getIndexData(code);
    }
}
