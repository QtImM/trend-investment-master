package bupt.service;

import bupt.pojo.Index;
import bupt.pojo.IndexData;
import cn.hutool.core.convert.Convert;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ThirdPartIndexClient {
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_OF_MAPS =
            new ParameterizedTypeReference<List<Map<String, Object>>>() {
            };
    private static final TypeReference<List<Map<String, Object>>> LOCAL_LIST_OF_MAPS =
            new TypeReference<List<Map<String, Object>>>() {
            };
    private static final List<Path> LOCAL_INDEX_ROOT_CANDIDATES = Arrays.asList(
            Paths.get("..", "third-part-index-data-project", "src", "main", "resources", "static", "indexes"),
            Paths.get("..", "third-part-index-data-project", "target", "classes", "static", "indexes"),
            Paths.get("third-part-index-data-project", "src", "main", "resources", "static", "indexes"),
            Paths.get("third-part-index-data-project", "target", "classes", "static", "indexes")
    );

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ThirdPartIndexClient(WebClient.Builder webClientBuilder,
                                @Value("${market.sync.third-party-base-url:http://127.0.0.1:8090/indexes}") String thirdPartBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(thirdPartBaseUrl).build();
        this.objectMapper = new ObjectMapper();
    }

    public List<Index> fetchIndexes() {
        try {
            List<Map<String, Object>> temp = webClient.get()
                    .uri("/codes.json")
                    .retrieve()
                    .bodyToMono(LIST_OF_MAPS)
                    .block();
            return mapToIndexes(temp);
        } catch (Exception exception) {
            return mapToIndexes(readLocalJson("codes.json"));
        }
    }

    public List<IndexData> fetchIndexData(String code) {
        try {
            List<Map<String, Object>> temp = webClient.get()
                    .uri("/{code}.json", code)
                    .retrieve()
                    .bodyToMono(LIST_OF_MAPS)
                    .block();
            return mapToIndexDatas(temp);
        } catch (Exception exception) {
            return mapToIndexDatas(readLocalJson(code + ".json"));
        }
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

    private List<Map<String, Object>> readLocalJson(String fileName) {
        for (Path root : LOCAL_INDEX_ROOT_CANDIDATES) {
            Path candidate = root.resolve(fileName).normalize();
            if (!Files.exists(candidate)) {
                continue;
            }
            try {
                return objectMapper.readValue(candidate.toFile(), LOCAL_LIST_OF_MAPS);
            } catch (IOException ignored) {
                // Try the next candidate path.
            }
        }
        return new ArrayList<>();
    }
}
