package bupt.service;

import bupt.pojo.Index;
import bupt.pojo.IndexData;
import cn.hutool.core.convert.Convert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ThirdPartIndexClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${market.sync.third-party-base-url:http://127.0.0.1:8090/indexes}")
    private String thirdPartBaseUrl;

    public List<Index> fetchIndexes() {
        List<Map> temp = restTemplate.getForObject(thirdPartBaseUrl + "/codes.json", List.class);
        return mapToIndexes(temp);
    }

    public List<IndexData> fetchIndexData(String code) {
        List<Map> temp = restTemplate.getForObject(thirdPartBaseUrl + "/" + code + ".json", List.class);
        return mapToIndexDatas(temp);
    }

    private List<Index> mapToIndexes(List<Map> temp) {
        List<Index> indexes = new ArrayList<>();
        if (temp == null) {
            return indexes;
        }

        for (Map map : temp) {
            String code = String.valueOf(map.get("code"));
            String name = String.valueOf(map.get("name"));
            Index index = new Index();
            index.setCode(code);
            index.setName(name);
            indexes.add(index);
        }
        return indexes;
    }

    private List<IndexData> mapToIndexDatas(List<Map> temp) {
        List<IndexData> indexDatas = new ArrayList<>();
        if (temp == null) {
            return indexDatas;
        }

        for (Map map : temp) {
            IndexData indexData = new IndexData();
            indexData.setDate(String.valueOf(map.get("date")));
            indexData.setClosePoint(Convert.toFloat(map.get("closePoint")));
            indexDatas.add(indexData);
        }
        return indexDatas;
    }
}
