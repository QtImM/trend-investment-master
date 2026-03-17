package bupt.client;

import bupt.pojo.IndexData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
public class ResilientIndexDataGateway implements IndexDataGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResilientIndexDataGateway.class);

    private final IndexDataTransportGateway indexDataTransportGateway;
    private final IndexDataFallbackGateway indexDataFallbackGateway;
    private final boolean fallbackEnabled;

    public ResilientIndexDataGateway(IndexDataTransportGateway indexDataTransportGateway,
                                     IndexDataFallbackGateway indexDataFallbackGateway,
                                     @Value("${backtest.remote.index-data.fallback.enabled:true}") boolean fallbackEnabled) {
        this.indexDataTransportGateway = indexDataTransportGateway;
        this.indexDataFallbackGateway = indexDataFallbackGateway;
        this.fallbackEnabled = fallbackEnabled;
    }

    @Override
    public List<IndexData> getIndexData(String code) {
        try {
            return indexDataTransportGateway.getIndexData(code);
        } catch (RuntimeException exception) {
            if (!fallbackEnabled) {
                throw exception;
            }

            LOGGER.warn("读取指数数据失败，切换到独立降级策略。code={}", code, exception);
            return indexDataFallbackGateway.getIndexData(code);
        }
    }
}
