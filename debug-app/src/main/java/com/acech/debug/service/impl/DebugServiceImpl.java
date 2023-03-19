package com.acech.debug.service.impl;

import com.acech.debug.model.UserWho;
import com.acech.debug.service.DebugService;
import com.acech.debug.util.Https;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Service for debugging
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 5:25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebugServiceImpl implements DebugService {

    private final HttpSession session;
    private final ExecutorService threadPool = new ThreadPoolExecutor(
            1, 5, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(64));

    @Override
    public UserWho whoami(String user) {
        final UserWho userWho = new UserWho(user);

        // Make RequestAttributes visible to child threads
        RequestContextHolder.setRequestAttributes(RequestContextHolder.getRequestAttributes(), true);

        queryPermissionAsync(user, "admin").thenApply(userWho::withAdmin)
            .thenAcceptAsync(isAdmin -> CompletableFuture.allOf(
                    queryPermissionAsync(user, "role01").thenAccept(userWho::setIsRole01),
                    queryPermissionAsync(user, "role02").thenAccept(userWho::setIsRole02),
                    queryPermissionAsync(user, "role03").thenAccept(userWho::setIsRole03),
                    queryPermissionAsync(user, "role04").thenAccept(userWho::setIsRole04)
                ).join()
            ).join();

        return userWho;
    }

    public CompletableFuture<Boolean> queryPermissionAsync(String username, String permission) {
        if (Https.isServletContext()) {
            final Object nullablePermission = session.getAttribute("PERMIT:" + permission);
            if (nullablePermission == null) {
                return CompletableFuture.supplyAsync(() -> {
                    final boolean isPermitted = mockQueryPermission(username, permission);
                    session.setAttribute("PERMIT:" + permission, isPermitted);
                    return isPermitted;
                }, threadPool);
            }
            return CompletableFuture.completedFuture((Boolean) nullablePermission);
        }
        return CompletableFuture.supplyAsync(() -> mockQueryPermission(username, permission), threadPool);
    }


    public boolean mockQueryPermission(String username, String permission) {
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.debug("Mocking request with {}, {}", username, permission);

        return true;
    }
}
