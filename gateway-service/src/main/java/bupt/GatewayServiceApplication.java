package bupt;

import brave.sampler.Sampler;
import cn.hutool.core.util.NetUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
public class GatewayServiceApplication {
    public static void main(String[] args) {
        int port = 8032;
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

        if (!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port);
            System.exit(1);
        }
        new SpringApplicationBuilder(GatewayServiceApplication.class)
                .properties("server.port=" + port)
                .run(args);
    }

    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }

    private static boolean isNacosProfileEnabled(String[] args) {
        if (args == null || args.length == 0) {
            return false;
        }

        return Arrays.stream(args)
                .anyMatch(arg -> arg.contains("spring.profiles.active=nacos"));
    }
}
