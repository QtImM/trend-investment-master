package bupt.client;

import bupt.pojo.IndexData;

import java.util.List;

public interface IndexDataTransportGateway {
    List<IndexData> getIndexData(String code);
}
