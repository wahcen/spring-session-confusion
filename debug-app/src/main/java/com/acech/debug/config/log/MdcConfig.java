package com.acech.debug.config.log;

import com.acech.debug.config.ConfigEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Slf4j MDC Context Config
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 4:43
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MdcConfig {

    public static final String MDC_ATTR_LOG_ID = "logId";

    private final ConfigEntry configEntry;

    @PostConstruct
    public void init() {
        MDC.setContextMap(configEntry.getMdc().getStaticContext());
    }

    @Bean
    public LogIdGenerator logIdGenerator() {
        return new TimestampedLogIdGeneratorWrapper(new UuidLogIdGenerator());
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public FilterRegistrationBean<Filter> mdcFilter() {
        final FilterRegistrationBean<Filter> registry = new FilterRegistrationBean<>();
        registry.setFilter(new MdcLogIdFilter(logIdGenerator()));
        registry.setName("Mdc LogId Filter");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE); // Set logId before any filters/interceptors or udc classes
        return registry;
    }

    public interface LogIdGenerator {
        String nextId();
    }

    @RequiredArgsConstructor
    public static class LogIdGeneratorWrapper implements LogIdGenerator {
        protected final LogIdGenerator logIdGenerator;

        @Override
        public String nextId() {
            return logIdGenerator.nextId();
        }
    }

    public static class UuidLogIdGenerator implements LogIdGenerator {
        @Override
        public String nextId() {
            return UUID.randomUUID().toString();
        }
    }

    public static class TimestampedLogIdGeneratorWrapper extends LogIdGeneratorWrapper {

        private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yy.M.d_HH:mm:ss.SSS");

        public TimestampedLogIdGeneratorWrapper(LogIdGenerator logIdGenerator) {
            super(logIdGenerator);
        }

        @Override
        public String nextId() {
            return super.nextId() + "_" + dateTimeFormatter.format(LocalDateTime.now());
        }
    }

    @RequiredArgsConstructor
    public static class MdcLogIdFilter extends OncePerRequestFilter {

        private final LogIdGenerator logIdGenerator;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            MDC.put(MDC_ATTR_LOG_ID, logIdGenerator.nextId());
            filterChain.doFilter(request, response);
        }
    }
}
