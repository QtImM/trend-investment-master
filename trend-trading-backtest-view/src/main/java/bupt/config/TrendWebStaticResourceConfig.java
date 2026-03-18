package bupt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class TrendWebStaticResourceConfig implements WebMvcConfigurer {
    private final String[] distResourceLocations;

    public TrendWebStaticResourceConfig(
            @Value("${trend.web.dist-location:trend-web/dist/}") String distLocation) {
        this.distResourceLocations = resolveResourceLocations(distLocation);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/trend-web/**")
                .addResourceLocations(distResourceLocations);
    }

    private String[] resolveResourceLocations(String configuredDistLocation) {
        Set<String> locations = new LinkedHashSet<>();
        List<Path> candidates = new ArrayList<>();
        candidates.add(Path.of(configuredDistLocation));
        candidates.add(Path.of("trend-web", "dist"));
        candidates.add(Path.of("..", "trend-web", "dist"));

        for (Path candidate : candidates) {
            Path normalizedPath = candidate.toAbsolutePath().normalize();
            if (normalizedPath.toFile().exists()) {
                locations.add(normalizedPath.toUri().toString());
            }
        }

        if (locations.isEmpty()) {
            locations.add(Path.of(configuredDistLocation).toAbsolutePath().normalize().toUri().toString());
        }

        return locations.toArray(new String[0]);
    }
}
