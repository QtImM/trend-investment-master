package bupt.client;

import bupt.pojo.IndexData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class HttpIndexDataGateway implements IndexDataTransportGateway {
    private final WebClient webClient;

    public HttpIndexDataGateway(WebClient.Builder webClientBuilder,
                                @Value("${backtest.remote.index-data.http.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public List<IndexData> getIndexData(String code) {
        IndexData[] response = webClient.get()
                .uri("/data/{code}", code)
                .retrieve()
                .bodyToMono(IndexData[].class)
                .block();
        if (response == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(response);
    }
}
