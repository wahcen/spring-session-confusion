package com.acech.debug.config.web;

import com.acech.debug.config.ConfigEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Register cas filter to print logs
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 3:44
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CasFilterConfig {

    private final ConfigEntry configEntry;

    @Bean
    public FilterRegistrationBean<Filter> casAuthenticationFilter() {
        final FilterRegistrationBean<Filter> registry = new FilterRegistrationBean<>();
        registry.setFilter(new AuthenticationFilter());
        registry.setName("CAS Authentication Filter");
        registry.addInitParameter("casServerLoginUrl", configEntry.getCas().getCasServerLoginUrl());
        registry.addInitParameter("serverName", configEntry.getCas().getServerName());
        registry.setUrlPatterns(configEntry.getCas().getSecuredPaths());
        registry.setOrder(1); // Must register after SessionRepositoryFilter
        return registry;
    }
    
    @Bean
    public FilterRegistrationBean<Filter> casValidationFilter() {
        final FilterRegistrationBean<Filter> registry = new FilterRegistrationBean<>();
        registry.setFilter(new Cas20ProxyReceivingTicketValidationFilter());
        registry.setName("CAS Validation Filter");
        registry.addInitParameter("casServerUrlPrefix", configEntry.getCas().getCasServerUrlPrefix());
        registry.addInitParameter("serverName", configEntry.getCas().getServerName());
        registry.addInitParameter("redirectAfterValidation",
                String.valueOf(configEntry.getCas().getRedirectAfterValidation()));
        registry.setUrlPatterns(configEntry.getCas().getSecuredPaths());
        registry.setOrder(2); // Must register after CAS Authentication Filter
        return registry;
    }
    
    @Bean
    public FilterRegistrationBean<Filter> casSafetyGuardFilter() {
        final FilterRegistrationBean<Filter> registry = new FilterRegistrationBean<>();
        registry.setFilter(new CasSafetyGuardFilter());
        registry.setName("CasSafetyGuard Filter"); // Must register after CAS Validation Filter
        registry.setUrlPatterns(configEntry.getCas().getSecuredPaths());
        registry.setOrder(3);
        return registry;
    }

    public static class CasSafetyGuardFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

            final HttpSession session = request.getSession(false);

            if (session != null) {
                final Object originNullableAssertion = session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
                if (originNullableAssertion == null) {
                    throw new IllegalStateException("User hasn't login at CAS");
                }

                final Assertion originAssertion = (Assertion) originNullableAssertion;
                final String originUsername = originAssertion.getPrincipal().getName();
                
                filterChain.doFilter(request, response);

                if (request.getSession(false) == null) {
                    return;
                }

                final Object currentNullableAssertion = session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
                if (currentNullableAssertion == null) {
                    return; // User already logout
                }

                final Assertion currentAssertion = (Assertion) currentNullableAssertion;
                final String currentUsername = currentAssertion.getPrincipal().getName();
                
                if (!StringUtils.equals(originUsername, currentUsername)) {
                    session.invalidate();
                    throw new IllegalStateException("CAS user changed after invoke is unsafe");
                }

            } else {
                filterChain.doFilter(request, response);
            }

        }

    }
}
