package bupt.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @Value("${trend.web.entry-url:http://127.0.0.1:8032/trend-web/}")
    String trendWebEntryUrl;

    @GetMapping("/")
    public String view() {
        return "redirect:" + trendWebEntryUrl;
    }

    @GetMapping("/legacy")
    public String legacyView() {
        return "redirect:" + trendWebEntryUrl;
    }

    @GetMapping({"/trend-web", "/trend-web/"})
    public String trendWebIndex() {
        return "forward:/trend-web/index.html";
    }

    @GetMapping("/trend-web/{path:[^.]*}")
    public String trendWebRoutes() {
        return "forward:/trend-web/index.html";
    }
}
