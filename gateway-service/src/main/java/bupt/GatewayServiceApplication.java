package bupt;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Arrays;
import java.util.Locale;
import java.net.ServerSocket;
import java.io.IOException;

@SpringBootApplication
public class GatewayServiceApplication {
    public static void main(String[] args) {
        int port = 8032;
        int nacosServerPort = 8848;
        boolean nacosProfileEnabled = isNacosProfileEnabled(args);

        if (nacosProfileEnabled && !isPortAvailable(nacosServerPort)) {
            System.err.printf("检查到端口%d 未启用，判断 nacos 服务器没有启动，本服务无法使用，故退出%n", nacosServerPort);
            System.exit(1);
        }

        if (!isPortAvailable(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port);
            System.exit(1);
        }
        new SpringApplicationBuilder(GatewayServiceApplication.class)
                .properties("server.port=" + port)
                .run(args);
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isNacosProfileEnabled(String[] args) {
        String activeProfiles = resolveActiveProfiles(args);
        if (activeProfiles == null || activeProfiles.trim().isEmpty()) {
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
                    return arg.substring(arg.indexOf("spring.profiles.active=") + "spring.profiles.active=".length());
                }
            }
        }

        String systemPropertyProfiles = System.getProperty("spring.profiles.active");
        if (systemPropertyProfiles != null && !systemPropertyProfiles.trim().isEmpty()) {
            return systemPropertyProfiles;
        }

        return System.getenv("SPRING_PROFILES_ACTIVE");
    }
}
