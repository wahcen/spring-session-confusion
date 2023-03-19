package com.acech.debug.util;

import com.acech.debug.config.log.MdcConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.MDC;

/**
 * Wrapper class used as rest-api return value
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 4:37
 */
@Data
@AllArgsConstructor
public class RestResult<T> {
    private Integer code;
    private String message;
    private T data;
    private String logId;
    private String traceId;

    public static <T> RestResult<T> ok() {
        return new RestResult<>(0, "", null, MDC.get(MdcConfig.MDC_ATTR_LOG_ID), "");
    }

    public static <T> RestResult<T> ok(T data) {
        return new RestResult<>(0, "", data, MDC.get(MdcConfig.MDC_ATTR_LOG_ID), "");
    }

    public static <T> RestResult<T> okMessage(String message) {
        return new RestResult<>(0, message, null, MDC.get(MdcConfig.MDC_ATTR_LOG_ID), "");
    }

    public static <T> RestResult<T> ok(String message, T data) {
        return new RestResult<>(0, message, data, MDC.get(MdcConfig.MDC_ATTR_LOG_ID), "");
    }
}
