package bupt.web;

import bupt.config.IpConfiguration;
import bupt.pojo.IndexData;
import bupt.service.MarketIndexDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MarketIndexDataController {
    @Autowired
    private MarketIndexDataService marketIndexDataService;
    @Autowired
    private IpConfiguration ipConfiguration;

    @GetMapping("/data/{code}")
    public List<IndexData> get(@PathVariable("code") String code) {
        System.out.println("market-data-service current instance's port is " + ipConfiguration.getPort());
        return marketIndexDataService.get(code);
    }
}
