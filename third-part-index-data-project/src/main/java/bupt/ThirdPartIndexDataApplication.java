package bupt;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.net.ServerSocket;
import java.io.IOException;

@SpringBootApplication
public class ThirdPartIndexDataApplication {
    public static void main(String[] args) {
        int port = 8090;

        if (null != args && 0 != args.length) {
            for (String arg : args) {
                String portStr = arg;
                String prefix = "port=";
                if (portStr.contains(prefix)) {
                    portStr = portStr.substring(portStr.indexOf(prefix) + prefix.length());
                    try {
                        port = Integer.parseInt(portStr);
                    } catch (NumberFormatException e) {
                        // Ignore, use default port
                    }
                }
            }
        }

        if (!isPortAvailable(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port);
            System.exit(1);
        }
        new SpringApplicationBuilder(ThirdPartIndexDataApplication.class).properties("server.port=" + port).run(args);
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
