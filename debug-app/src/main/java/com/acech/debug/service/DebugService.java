package com.acech.debug.service;

import com.acech.debug.model.UserWho;

/**
 * Service for debugging
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 5:23
 */
public interface DebugService {
    /**
     * Read user from session
     * @param user username
     * @return User
     */
    UserWho whoami(String user);
}
