package bupt;

import brave.sampler.Sampler;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Locale;

@SpringBootApplication
public class TrendTradingBackTestViewApplication {
    public static void main(String[] args) {
        int defaultPort = 8041;
        int nacosServerPort = 8848;
        boolean nacosProfileEnabled = isNacosProfileEnabled(args);
        int port = resolveServerPort(args, defaultPort);

        if(nacosProfileEnabled && NetUtil.isUsableLocalPort(nacosServerPort)) {
            System.err.printf("检查到端口%d 未启用，判断 nacos 服务器没有启动，本服务无法使用，故退出%n", nacosServerPort );
            System.exit(1);
        }

        if(!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }
        new SpringApplicationBuilder(TrendTradingBackTestViewApplication.class).properties("server.port=" + port).run(args);

    }
    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }

    private static int resolveServerPort(String[] args, int defaultPort) {
        if (args != null) {
            for (String arg : args) {
                if (arg != null && arg.startsWith("port=")) {
                    String strPort = StrUtil.subAfter(arg, "port=", true);
                    if (NumberUtil.isNumber(strPort)) {
                        return Convert.toInt(strPort);
                    }
                }
                if (arg != null && arg.contains("server.port=")) {
                    String strPort = StrUtil.subAfter(arg, "server.port=", true);
                    if (NumberUtil.isNumber(strPort)) {
                        return Convert.toInt(strPort);
                    }
                }
            }
        }

        String systemPropertyPort = System.getProperty("server.port");
        if (NumberUtil.isNumber(systemPropertyPort)) {
            return Convert.toInt(systemPropertyPort);
        }

        String envPort = System.getenv("SERVER_PORT");
        if (NumberUtil.isNumber(envPort)) {
            return Convert.toInt(envPort);
        }

        return defaultPort;
    }

    private static boolean isNacosProfileEnabled(String[] args) {
        String activeProfiles = resolveActiveProfiles(args);
        if (StrUtil.isBlank(activeProfiles)) {
            return true;
        }

        return Arrays.stream(activeProfiles.split(","))
                .map(String::trim)
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch("nacos"::equals);
    }

    private static String resolveActiveProfiles(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (arg != null && arg.contains("spring.profiles.active=")) {
                    return StrUtil.subAfter(arg, "spring.profiles.active=", true);
                }
            }
        }

        String systemPropertyProfiles = System.getProperty("spring.profiles.active");
        if (StrUtil.isNotBlank(systemPropertyProfiles)) {
            return systemPropertyProfiles;
        }

        return System.getenv("SPRING_PROFILES_ACTIVE");
    }
}
