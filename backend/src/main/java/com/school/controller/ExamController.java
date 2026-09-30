package com.school.controller;

import com.school.common.OpLog;
import com.school.common.Result;
import com.school.common.UserContext;
import com.school.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping("/start/{paperId}")
    public Result<Map<String, Object>> start(@PathVariable Long paperId) {
        return Result.ok(examService.start(paperId, UserContext.userId()));
    }

    @PostMapping("/draft")
    public Result<Void> draft(@RequestBody Map<String, Object> body) {
        Long recordId = Long.valueOf(String.valueOf(body.get("recordId")));
        examService.saveDraft(recordId, UserContext.userId(), (Map<Long, String>) body.get("answers"));
        return Result.ok();
    }

    @PostMapping("/submit")
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        Long recordId = Long.valueOf(String.valueOf(body.get("recordId")));
        return Result.ok(examService.submit(recordId, UserContext.userId(), (Map<Long, String>) body.get("answers")));
    }

    @PostMapping("/cheat")
    public Result<Map<String, Object>> cheat(@RequestBody Map<String, String> body) {
        Long recordId = Long.valueOf(body.get("recordId"));
        return Result.ok(examService.cheat(recordId, UserContext.userId()));
    }

    @GetMapping("/history/{paperId}")
    public Result<Map<String, Object>> history(@PathVariable Long paperId) {
        return Result.ok(examService.historyPaper(paperId, UserContext.userId()));
    }

    @PostMapping("/reopen/{recordId}")
    @OpLog(module = "exam", action = "REOPEN_EXAM", targetType = "exam_record")
    public Result<Void> reopen(@PathVariable Long recordId) {
        examService.reopen(recordId, UserContext.userId());
        return Result.ok();
    }
}
