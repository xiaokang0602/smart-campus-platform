package com.school.controller;

import com.school.common.OpLog;
import com.school.common.Result;
import com.school.common.UserContext;
import com.school.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @OpLog(module = "login", action = "LOGIN")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        return Result.ok(authService.login(body.get("username"), body.get("password")));
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.ok(authService.profile(UserContext.userId()));
    }

    @PostMapping("/logout")
    @OpLog(module = "login", action = "LOGOUT")
    public Result<Void> logout() {
        return Result.ok();
    }

    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        authService.changePassword(UserContext.userId(), body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }

    @PostMapping("/firstLogin")
    public Result<Void> firstLogin(@RequestBody Map<String, Object> body) {
        authService.firstLogin(
                UserContext.userId(),
                (String) body.get("newPassword"),
                (List<String>) body.get("questions"),
                (List<String>) body.get("answers"),
                (List<Long>) body.get("guardianIds"));
        return Result.ok();
    }

    @PostMapping("/recover/questions")
    public Result<List<String>> recoverQuestions(@RequestBody Map<String, String> body) {
        return Result.ok(authService.recoverQuestions(body.get("username")));
    }

    @PostMapping("/recover/answers")
    public Result<Void> recoverAnswers(@RequestBody Map<String, Object> body) {
        authService.recoverVerifyAnswers((String) body.get("username"), (List<String>) body.get("answers"));
        return Result.ok();
    }

    @PostMapping("/recover/verify")
    public Result<Boolean> recoverVerify(@RequestBody Map<String, Object> body) {
        boolean done = authService.recoverVerifyGuardian(
                (String) body.get("username"),
                (String) body.get("code"),
                Long.valueOf(String.valueOf(body.get("guardianUserId"))));
        return Result.ok(done);
    }
}
