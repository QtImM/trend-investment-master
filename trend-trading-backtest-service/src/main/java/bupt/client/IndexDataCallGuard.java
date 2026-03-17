package bupt.client;

import bupt.pojo.IndexData;

import java.util.List;
import java.util.function.Supplier;

public interface IndexDataCallGuard {
    List<IndexData> execute(String code, Supplier<List<IndexData>> remoteCall);
}
