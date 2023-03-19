package com.acech.debug.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CAS属性配置
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 3:48
 */
@Data
@Configuration
@ConfigurationProperties("sys.conf")
public class ConfigEntry {

    private CasConfigEntry cas;
    private MdcConfigEntry mdc;

    @Data
    public static class CasConfigEntry {
        private String serverName;
        private String casServerLoginUrl;
        private String casServerUrlPrefix;
        private Boolean redirectAfterValidation;
        private List<String> securedPaths;
    }

    @Data
    public static class MdcConfigEntry {
        private Map<String, String> staticContext = new HashMap<>();
    }
}
