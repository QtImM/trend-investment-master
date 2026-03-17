package bupt.client;

import bupt.pojo.IndexData;

import java.util.List;

public interface IndexDataGateway {
    List<IndexData> getIndexData(String code);
}
