package bupt.client;

import bupt.pojo.IndexData;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(prefix = "backtest.remote.index-data.resilience4j", name = "enabled", havingValue = "true")
public class Resilience4jIndexDataCallGuard implements IndexDataCallGuard {
    private final CircuitBreaker circuitBreaker;

    public Resilience4jIndexDataCallGuard(
            @Value("${backtest.remote.index-data.resilience4j.failure-rate-threshold:50}") float failureRateThreshold,
            @Value("${backtest.remote.index-data.resilience4j.sliding-window-size:10}") int slidingWindowSize,
            @Value("${backtest.remote.index-data.resilience4j.minimum-number-of-calls:5}") int minimumNumberOfCalls,
            @Value("${backtest.remote.index-data.resilience4j.wait-duration-in-open-state-ms:10000}") long waitDurationInOpenStateMs) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .slidingWindowSize(slidingWindowSize)
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .waitDurationInOpenState(Duration.ofMillis(waitDurationInOpenStateMs))
                .build();
        this.circuitBreaker = CircuitBreakerRegistry.of(config).circuitBreaker("index-data-gateway");
    }

    @Override
    public List<IndexData> execute(String code, Supplier<List<IndexData>> remoteCall) {
        Supplier<List<IndexData>> guardedCall = CircuitBreaker.decorateSupplier(circuitBreaker, remoteCall);
        return guardedCall.get();
    }
}
