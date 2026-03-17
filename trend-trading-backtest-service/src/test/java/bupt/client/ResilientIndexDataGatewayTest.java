package bupt.client;

import bupt.pojo.IndexData;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ResilientIndexDataGatewayTest {

    @Test
    public void shouldReturnTransportResultWhenRemoteCallSucceeds() {
        List<IndexData> expected = Collections.singletonList(createIndexData("2024-01-02", 123.45f));
        IndexDataCallGuard callGuard = new DirectIndexDataCallGuard();
        IndexDataTransportGateway transportGateway = code -> expected;
        IndexDataFallbackGateway fallbackGateway = new IndexDataFallbackGateway();

        ResilientIndexDataGateway gateway = new ResilientIndexDataGateway(callGuard, transportGateway, fallbackGateway, true);

        List<IndexData> actual = gateway.getIndexData("000300");

        assertSame(expected, actual);
    }

    @Test
    public void shouldUseFallbackWhenRemoteCallFailsAndFallbackEnabled() {
        IndexDataCallGuard callGuard = new DirectIndexDataCallGuard();
        IndexDataTransportGateway transportGateway = code -> {
            throw new IllegalStateException("remote service unavailable");
        };
        IndexDataFallbackGateway fallbackGateway = new IndexDataFallbackGateway();

        ResilientIndexDataGateway gateway = new ResilientIndexDataGateway(callGuard, transportGateway, fallbackGateway, true);

        List<IndexData> actual = gateway.getIndexData("000300");

        assertEquals(1, actual.size());
        assertEquals("0000-00-00", actual.get(0).getDate());
        assertEquals(0.0f, actual.get(0).getClosePoint(), 0.0f);
    }

    @Test
    public void shouldRethrowWhenRemoteCallFailsAndFallbackDisabled() {
        IndexDataCallGuard callGuard = new DirectIndexDataCallGuard();
        IndexDataTransportGateway transportGateway = code -> {
            throw new IllegalStateException("remote service unavailable");
        };
        IndexDataFallbackGateway fallbackGateway = new IndexDataFallbackGateway();

        ResilientIndexDataGateway gateway = new ResilientIndexDataGateway(callGuard, transportGateway, fallbackGateway, false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> gateway.getIndexData("000300"));
        assertEquals("remote service unavailable", exception.getMessage());
    }

    @Test
    public void shouldAllowFirstFailureThroughWhenResilience4jEnabled() {
        IndexDataCallGuard callGuard = new Resilience4jIndexDataCallGuard(50, 2, 2, 10000);
        IndexDataTransportGateway transportGateway = code -> {
            throw new IllegalStateException("remote service unavailable");
        };
        IndexDataFallbackGateway fallbackGateway = new IndexDataFallbackGateway();

        ResilientIndexDataGateway gateway = new ResilientIndexDataGateway(callGuard, transportGateway, fallbackGateway, true);

        List<IndexData> actual = gateway.getIndexData("000300");

        assertEquals(1, actual.size());
        assertEquals("0000-00-00", actual.get(0).getDate());
        assertEquals(0.0f, actual.get(0).getClosePoint(), 0.0f);
    }

    @Test
    public void shouldShortCircuitAfterFailureThresholdReachedWhenResilience4jEnabled() {
        IndexDataCallGuard callGuard = new Resilience4jIndexDataCallGuard(50, 2, 2, 10000);
        IndexDataTransportGateway transportGateway = code -> {
            throw new IllegalStateException("remote service unavailable");
        };
        IndexDataFallbackGateway fallbackGateway = new IndexDataFallbackGateway();

        ResilientIndexDataGateway gateway = new ResilientIndexDataGateway(callGuard, transportGateway, fallbackGateway, false);

        try {
            gateway.getIndexData("000300");
            fail("first call should fail");
        } catch (IllegalStateException expected) {
            assertEquals("remote service unavailable", expected.getMessage());
        }

        try {
            gateway.getIndexData("000300");
            fail("second call should fail");
        } catch (IllegalStateException expected) {
            assertEquals("remote service unavailable", expected.getMessage());
        }

        try {
            gateway.getIndexData("000300");
            fail("third call should be short-circuited");
        } catch (Exception expected) {
            assertEquals("CallNotPermittedException", expected.getClass().getSimpleName());
        }
    }

    private IndexData createIndexData(String date, float closePoint) {
        IndexData indexData = new IndexData();
        indexData.setDate(date);
        indexData.setClosePoint(closePoint);
        return indexData;
    }
}
