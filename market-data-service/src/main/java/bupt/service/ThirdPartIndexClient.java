package bupt.service;

import bupt.pojo.Index;
import bupt.pojo.IndexData;
import cn.hutool.core.convert.Convert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ThirdPartIndexClient {
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_OF_MAPS =
            new ParameterizedTypeReference<List<Map<String, Object>>>() {
            };

    private final WebClient webClient;

    public ThirdPartIndexClient(WebClient.Builder webClientBuilder,
                                @Value("${market.sync.third-party-base-url:http://127.0.0.1:8090/indexes}") String thirdPartBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(thirdPartBaseUrl).build();
    }

    public List<Index> fetchIndexes() {
        List<Map<String, Object>> temp = webClient.get()
                .uri("/codes.json")
                .retrieve()
                .bodyToMono(LIST_OF_MAPS)
                .block();
        return mapToIndexes(temp);
    }

    public List<IndexData> fetchIndexData(String code) {
        List<Map<String, Object>> temp = webClient.get()
                .uri("/{code}.json", code)
                .retrieve()
                .bodyToMono(LIST_OF_MAPS)
                .block();
        return mapToIndexDatas(temp);
    }

    private List<Index> mapToIndexes(List<Map<String, Object>> temp) {
        List<Index> indexes = new ArrayList<>();
        if (temp == null) {
            return indexes;
        }

        for (Map<String, Object> map : temp) {
            String code = String.valueOf(map.get("code"));
            String name = String.valueOf(map.get("name"));
            Index index = new Index();
            index.setCode(code);
            index.setName(name);
            indexes.add(index);
        }
        return indexes;
    }

    private List<IndexData> mapToIndexDatas(List<Map<String, Object>> temp) {
        List<IndexData> indexDatas = new ArrayList<>();
        if (temp == null) {
            return indexDatas;
        }

        for (Map<String, Object> map : temp) {
            IndexData indexData = new IndexData();
            indexData.setDate(String.valueOf(map.get("date")));
            indexData.setClosePoint(Convert.toFloat(map.get("closePoint")));
            indexDatas.add(indexData);
        }
        return indexDatas;
    }
}
