package com.acech.debug.config.web;

import com.acech.debug.config.ConfigEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Cas Interceptors
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 3:46
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CasInterceptorConfig {

    public static final String SESSION_ATTR_USER = "user";

    private final ConfigEntry configEntry;

    @Bean
    public WebMvcConfigurer webMvcConfigurer(List<ConfigurableHandlerInterceptor> interceptors) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                for (ConfigurableHandlerInterceptor interceptor : interceptors) {
                    interceptor.configurer.config(registry.addInterceptor(interceptor.delegate));
                }
            }
        };
    }

    @Bean
    public ConfigurableHandlerInterceptor casPrincipalInterceptor() {
        return new ConfigurableHandlerInterceptor(
                new CasPrincipalInterceptor(), r -> r.addPathPatterns(configEntry.getCas().getSecuredPaths()));
    }

    @RequiredArgsConstructor
    public static class ConfigurableHandlerInterceptor {
        private final HandlerInterceptor delegate;
        private final InterceptorConfigurer configurer;

        @FunctionalInterface
        public interface InterceptorConfigurer {
            void config(InterceptorRegistration registration);
        }
    }

    public static class CasPrincipalInterceptor extends HandlerInterceptorAdapter {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            final HttpSession session = request.getSession(false);
            final Object nullableAssertion = session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
            if (nullableAssertion == null) {
                throw new NullPointerException("This interceptor should not be used without cas.");
            }

            final Assertion assertion = (Assertion) nullableAssertion;
            session.setAttribute(SESSION_ATTR_USER, assertion.getPrincipal().getName());

            return super.preHandle(request, response, handler);
        }

    }
}
