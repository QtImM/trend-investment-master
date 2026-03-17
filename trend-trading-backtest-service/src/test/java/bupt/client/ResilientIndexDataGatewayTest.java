package bupt.client;

import bupt.pojo.IndexData;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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

    @Test(expected = IllegalStateException.class)
    public void shouldRethrowWhenRemoteCallFailsAndFallbackDisabled() {
        IndexDataCallGuard callGuard = new DirectIndexDataCallGuard();
        IndexDataTransportGateway transportGateway = code -> {
            throw new IllegalStateException("remote service unavailable");
        };
        IndexDataFallbackGateway fallbackGateway = new IndexDataFallbackGateway();

        ResilientIndexDataGateway gateway = new ResilientIndexDataGateway(callGuard, transportGateway, fallbackGateway, false);

        gateway.getIndexData("000300");
    }

    private IndexData createIndexData(String date, float closePoint) {
        IndexData indexData = new IndexData();
        indexData.setDate(date);
        indexData.setClosePoint(closePoint);
        return indexData;
    }
}
