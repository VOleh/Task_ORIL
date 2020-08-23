package com.vanivskyi.test.security;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Component
public class CookieProvider {
    public final static String KEY_VALUE_FOR_COOKIE = "test_app_jwt";
    public  final static int EXPIRED_TIME_FOR_COOKIE = 1000000;

    public Cookie createCookie(String token) {
        Cookie cookie = new Cookie(KEY_VALUE_FOR_COOKIE, token);
        cookie.setMaxAge(EXPIRED_TIME_FOR_COOKIE);
        cookie.setPath("/");
        return cookie;
    }


    public String readCookie(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(KEY_VALUE_FOR_COOKIE))
                    token = c.getValue();
            }
        }
        return token;
    }

    public Cookie deleteCookie() {
        Cookie cookie = new Cookie(KEY_VALUE_FOR_COOKIE, "");
        cookie.setMaxAge(0);
        return cookie;
    }
}