package com.acech.debug.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Http Utils
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 5:32
 */
public class Https {

    private Https() {
        // forbid
    }

    public static HttpServletRequest getRequest() {
        final ServletRequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getRequest();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return (ServletRequestAttributes) requestAttributes;
    }

    public static boolean isServletContext() {
        return getRequestAttributes() != null;
    }
}
