package bupt;

import brave.sampler.Sampler;
import cn.hutool.core.util.NetUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableCaching
@EnableScheduling
public class MarketDataApplication {

    public static void main(String[] args) {
        int redisPort = 6379;
        int eurekaServerPort = 8761;
        int nacosServerPort = 8848;
        boolean nacosProfileEnabled = isNacosProfileEnabled(args);

        if (!nacosProfileEnabled && NetUtil.isUsableLocalPort(eurekaServerPort)) {
            System.err.printf("检查到端口%d 未启用，判断 eureka 服务器没有启动，本服务无法使用，故退出%n", eurekaServerPort);
            System.exit(1);
        }

        if (nacosProfileEnabled && NetUtil.isUsableLocalPort(nacosServerPort)) {
            System.err.printf("检查到端口%d 未启用，判断 nacos 服务器没有启动，本服务无法使用，故退出%n", nacosServerPort);
            System.exit(1);
        }

        if (NetUtil.isUsableLocalPort(redisPort)) {
            System.err.printf("检查到端口%d 未启用，判断 redis 服务器没有启动，本服务无法使用，故退出%n", redisPort);
            System.exit(1);
        }

        new SpringApplicationBuilder(MarketDataApplication.class)
                .properties("server.port=8061")
                .run(args);
    }

    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private static boolean isNacosProfileEnabled(String[] args) {
        if (args == null || args.length == 0) {
            return false;
        }

        return Arrays.stream(args)
                .anyMatch(arg -> arg.contains("spring.profiles.active=nacos"));
    }
}
