package bupt.service;

import bupt.pojo.Index;
import bupt.util.SpringContextUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "market_indexes")
public class MarketIndexCodeService {
    private List<Index> indexes;

    private final ThirdPartIndexClient thirdPartIndexClient;

    public MarketIndexCodeService(ThirdPartIndexClient thirdPartIndexClient) {
        this.thirdPartIndexClient = thirdPartIndexClient;
    }

    public List<Index> fresh() {
        try {
            indexes = thirdPartIndexClient.fetchIndexes();
            MarketIndexCodeService marketIndexCodeService = SpringContextUtil.getBean(MarketIndexCodeService.class);
            marketIndexCodeService.remove();
            return marketIndexCodeService.store();
        } catch (Exception exception) {
            return thirdPartNotConnected();
        }
    }

    public List<Index> thirdPartNotConnected() {
        Index index = new Index();
        index.setName("无效指数代码");
        index.setCode("000000");
        return CollectionUtil.toList(index);
    }

    @CachePut(key = "'all_codes'")
    public List<Index> store() {
        return indexes == null ? CollUtil.newArrayList() : indexes;
    }

    @CacheEvict(allEntries = true)
    public void remove() {
    }

    @Cacheable(key = "'all_codes'")
    public List<Index> get() {
        return CollUtil.toList();
    }
}
