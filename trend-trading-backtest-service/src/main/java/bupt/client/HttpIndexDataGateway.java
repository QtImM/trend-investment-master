package bupt.client;

import bupt.pojo.IndexData;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class HttpIndexDataGateway implements IndexDataTransportGateway {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public HttpIndexDataGateway(RestTemplateBuilder restTemplateBuilder,
                                @Value("${backtest.remote.index-data.http.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = baseUrl;
    }

    @Override
    public List<IndexData> getIndexData(String code) {
        IndexData[] response = restTemplate.getForObject(baseUrl + "/data/{code}", IndexData[].class, code);
        if (response == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(response);
    }
}
