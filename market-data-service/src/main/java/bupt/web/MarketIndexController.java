package bupt.web;

import bupt.config.IpConfiguration;
import bupt.pojo.Index;
import bupt.service.MarketIndexCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MarketIndexController {
    @Autowired
    private MarketIndexCodeService marketIndexCodeService;
    @Autowired
    private IpConfiguration ipConfiguration;

    @GetMapping("/codes")
    @CrossOrigin
    public List<Index> codes() {
        System.out.println("market-data-service current instance's port is " + ipConfiguration.getPort());
        return marketIndexCodeService.get();
    }
}
