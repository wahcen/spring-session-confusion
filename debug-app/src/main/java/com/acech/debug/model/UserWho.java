package com.acech.debug.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * User info
 *
 * @author wangchen12@xiaomi.com
 * @date 2023/3/18 5:23
 */
@Data
public class UserWho {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss.SSS");

    private String username;
    private Boolean isAdmin;
    private Boolean isRole01;
    private Boolean isRole02;
    private Boolean isRole03;
    private Boolean isRole04;
    private String lastAccessTime;

    public UserWho(String username) {
        this.username = username;
        this.isAdmin = false;
        this.isRole01 = false;
        this.isRole02 = false;
        this.isRole03 = false;
        this.isRole04 = false;
        this.lastAccessTime = dateTimeFormatter.format(LocalDateTime.now());
    }

    public Boolean withAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        return isAdmin;
    }
}
