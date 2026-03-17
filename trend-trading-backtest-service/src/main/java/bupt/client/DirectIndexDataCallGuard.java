package bupt.client;

import bupt.pojo.IndexData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(prefix = "backtest.remote.index-data.resilience4j", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DirectIndexDataCallGuard implements IndexDataCallGuard {
    @Override
    public List<IndexData> execute(String code, Supplier<List<IndexData>> remoteCall) {
        return remoteCall.get();
    }
}
