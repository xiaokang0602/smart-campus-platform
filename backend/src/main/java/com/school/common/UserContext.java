package com.school.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户上下文（由 JWT 拦截器写入 ThreadLocal）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private Long userId;
    private Integer role;          // 1学生 2任课教师 3班主任 4后台
    private Integer accountLevel;  // 后台层级；学生教师为0
    private Long schoolId;
    private String username;

    public static void set(UserContext ctx) {
        HOLDER.set(ctx);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static Long userId() {
        UserContext c = HOLDER.get();
        return c == null ? null : c.getUserId();
    }

    public static Long schoolId() {
        UserContext c = HOLDER.get();
        return c == null ? null : c.getSchoolId();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 是否后台角色 */
    public static boolean isAdmin() {
        UserContext c = HOLDER.get();
        return c != null && c.getRole() != null && c.getRole() == 4;
    }

    /** 是否超级管理员 */
    public static boolean isSuperAdmin() {
        UserContext c = HOLDER.get();
        return c != null && c.getRole() != null && c.getRole() == 4
                && c.getAccountLevel() != null && c.getAccountLevel() == 1;
    }
}
