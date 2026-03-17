package bupt;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ThirdPartIndexDataApplication {
    public static void main(String[] args) {
        int port = 8090;

        if (null != args && 0 != args.length) {
            for (String arg : args) {
                String strPort = StrUtil.subAfter(arg, "port=", true);
                if (NumberUtil.isNumber(strPort)) {
                    port = Convert.toInt(strPort);
                }
            }
        }

        if (!NetUtil.isUsableLocalPort(port)) {
            System.err.printf("端口%d被占用了，无法启动%n", port);
            System.exit(1);
        }
        new SpringApplicationBuilder(ThirdPartIndexDataApplication.class).properties("server.port=" + port).run(args);
    }
}
