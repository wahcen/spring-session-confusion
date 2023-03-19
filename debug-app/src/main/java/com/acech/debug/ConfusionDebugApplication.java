package com.acech.debug;

import com.acech.debug.config.ConfigEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * A demo application that reproduce session confusion
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 3:35
 */
@SpringBootApplication
@EnableConfigurationProperties(ConfigEntry.class)
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 120 * 3600)
public class ConfusionDebugApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfusionDebugApplication.class, args);
    }

}
