package bupt;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Arrays;
import java.util.Locale;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

@SpringBootApplication
public class TrendTradingBackTestServiceApplication {
    public static void main(String[] args) {
        int defaultPort = 8051;
        int nacosServerPort = 8848;
        boolean nacosProfileEnabled = isNacosProfileEnabled(args);
        int port = resolveServerPort(args, defaultPort);

        if(nacosProfileEnabled && !isPortListening("127.0.0.1", nacosServerPort)) {
            System.err.printf("检查到端口%d 未启用，判断 nacos 服务器没有启动，本服务无法使用，故退出%n", nacosServerPort );
            System.exit(1);
        }

        if(!isPortAvailable(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port );
            System.exit(1);
        }
        new SpringApplicationBuilder(TrendTradingBackTestServiceApplication.class).properties("server.port=" + port).run(args);

    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isPortListening(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static int resolveServerPort(String[] args, int defaultPort) {
        if (args != null) {
            for (String arg : args) {
                if (arg != null && arg.startsWith("port=")) {
                    String portStr = extractValue(arg, "port=");
                    try {
                        return Integer.parseInt(portStr);
                    } catch (NumberFormatException e) {
                        // Ignore, use default
                    }
                }
                if (arg != null && arg.contains("server.port=")) {
                    String portStr = extractValue(arg, "server.port=");
                    try {
                        return Integer.parseInt(portStr);
                    } catch (NumberFormatException e) {
                        // Ignore, use default
                    }
                }
            }
        }

        String systemPropertyPort = System.getProperty("server.port");
        if (systemPropertyPort != null && !systemPropertyPort.isEmpty()) {
            try {
                return Integer.parseInt(systemPropertyPort);
            } catch (NumberFormatException e) {
                // Ignore, use default
            }
        }

        String envPort = System.getenv("SERVER_PORT");
        if (envPort != null && !envPort.isEmpty()) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                // Ignore, use default
            }
        }

        return defaultPort;
    }

    private static String extractValue(String arg, String prefix) {
        int index = arg.indexOf(prefix);
        if (index >= 0) {
            return arg.substring(index + prefix.length());
        }
        return "";
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
