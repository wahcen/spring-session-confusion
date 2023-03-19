package com.acech.debug.api;

import com.acech.debug.config.ConfigEntry;
import com.acech.debug.config.web.CasInterceptorConfig;
import com.acech.debug.model.UserWho;
import com.acech.debug.service.DebugService;
import com.acech.debug.util.RestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Invoke below api and reproduce session confusion
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 4:31
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/debug")
public class DebugApi {

    private final HttpSession session; // Spring autowired JDK dynamic proxy object
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final RedisOperationsSessionRepository sessionRepository; // Session storage redis backend

    private final ConfigEntry configEntry;

    private final DebugService debugService;

    @GetMapping("/whoami")
    public RestResult<UserWho> whoami() {
        final Cookie[] cookies = request.getCookies();
        if (cookies.length == 2) {
            throw new IllegalStateException("There maybe a race condition which read another request's cookie");
        }

        final String originId = session.getId();

        final UserWho whoami = debugService.whoami((String)
                session.getAttribute(CasInterceptorConfig.SESSION_ATTR_USER));

        final String currentId = session.getId();
        if (!StringUtils.equals(originId, currentId)) {
            final Session origin = sessionRepository.findById(originId);
            final Session current = sessionRepository.findById(currentId);
            throw new IllegalStateException("There maybe a race condition which changed " +
                    "current request's session-id, origin " + origin.getId() + " current " + current.getId());
        }

        return RestResult.ok(whoami);
    }

    @PostMapping("/authenticate")
    public Map<String, Object> authenticate() {
        // Read Credentials passed by the cas server
        final String base64EncodedCredential = StringUtils
                .substringAfter(request.getHeader("Authorization"), "Basic ");
        final String credential = new String(Base64.getDecoder().decode(base64EncodedCredential));
        final String[] credentialPair = StringUtils.split(credential, ":");

        if (!"Mellon".equals(credentialPair[1])) {
            response.setStatus(HttpStatus.SC_FORBIDDEN);
        }

        final Map<String, Object> principal = new HashMap<>();
        final Map<String, Object> attributes = new HashMap<>();
        final List<Object> names = new ArrayList<>();
        names.add("java.util.List");
        names.add(Arrays.asList("txId", UUID.randomUUID().toString()));
        attributes.put("@class", "java.util.LinkedHashMap");
        attributes.put("names", names);
        principal.put("@class", "org.apereo.cas.authentication.principal.SimplePrincipal");
        principal.put("id", credentialPair[0]);
        principal.put("attributes", attributes);

        response.setHeader("X-CAS-Warning", "Currently authenticate from Debug Application");

        return principal;
    }

    @GetMapping("/logout")
    public RestResult<Void> logout() throws IOException {
        if (session == null) {
            return RestResult.okMessage("You should login first to logout");
        }
        final String id = session.getId();
        session.invalidate();

        final String casLogout = MessageFormat.format(
                "{0}/logout?service={1}/api/debug/fin",
                configEntry.getCas().getCasServerUrlPrefix(),
                configEntry.getCas().getServerName());
        response.sendRedirect(casLogout);
        return RestResult.okMessage("Redirected to cas and logout, session-id " + id);
    }

    @GetMapping("/fin")
    public RestResult<Void> fin() {
        return RestResult.okMessage("You have logged out");
    }

}
