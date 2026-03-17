package bupt.service;

import bupt.pojo.IndexData;
import bupt.util.SpringContextUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = "market_index_datas")
public class MarketIndexDataService {
    private Map<String, List<IndexData>> indexDatas = new HashMap<>();

    private final ThirdPartIndexClient thirdPartIndexClient;

    public MarketIndexDataService(ThirdPartIndexClient thirdPartIndexClient) {
        this.thirdPartIndexClient = thirdPartIndexClient;
    }

    public List<IndexData> fresh(String code) {
        try {
            indexDatas.put(code, thirdPartIndexClient.fetchIndexData(code));
            MarketIndexDataService marketIndexDataService = SpringContextUtil.getBean(MarketIndexDataService.class);
            marketIndexDataService.remove(code);
            return marketIndexDataService.store(code);
        } catch (Exception exception) {
            return thirdPartNotConnected(code);
        }
    }

    @CachePut(key = "'indexData-code-'+#p0")
    public List<IndexData> store(String code) {
        return indexDatas.get(code);
    }

    @CacheEvict(key = "'indexData-code-'+#p0")
    public void remove(String code) {
    }

    @Cacheable(key = "'indexData-code-'+#p0")
    public List<IndexData> get(String code) {
        return CollUtil.toList();
    }

    public List<IndexData> thirdPartNotConnected(String code) {
        IndexData indexData = new IndexData();
        indexData.setDate("n/a");
        indexData.setClosePoint(0);
        return CollectionUtil.toList(indexData);
    }
}
