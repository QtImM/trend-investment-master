package bupt.client;

import bupt.pojo.IndexData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeignIndexDataGateway implements IndexDataGateway {
    private final IndexDataClient indexDataClient;

    public FeignIndexDataGateway(IndexDataClient indexDataClient) {
        this.indexDataClient = indexDataClient;
    }

    @Override
    public List<IndexData> getIndexData(String code) {
        return indexDataClient.getIndexData(code);
    }
}
