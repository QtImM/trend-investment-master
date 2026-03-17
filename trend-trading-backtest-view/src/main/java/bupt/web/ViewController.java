package bupt.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RefreshScope
public class ViewController {
    @Value("${version:how2j trend trading backtest view version 1.5}")
    String version;

    @Value("${trend.web.entry-url:http://127.0.0.1:8032/trend-web/}")
    String trendWebEntryUrl;

    @GetMapping("/")
    public String view() {
        return "redirect:" + trendWebEntryUrl;
    }

    @GetMapping("/legacy")
    public String legacyView(Model model) {
        model.addAttribute("version",version);
        return "view";
    }
}
