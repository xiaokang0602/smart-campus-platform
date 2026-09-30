package com.school.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具（BCrypt 加密）
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    public static String encode(String raw) {
        return ENCODER.encode(raw);
    }

    public static boolean matches(String raw, String encoded) {
        if (raw == null || encoded == null) {
            return false;
        }
        return ENCODER.matches(raw, encoded);
    }

    /** 生成 6 位随机数字临时密码 */
    public static String randomTempPassword() {
        int v = (int) (Math.random() * 1000000);
        return String.format("%06d", v);
    }
}
