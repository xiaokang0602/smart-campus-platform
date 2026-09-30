package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.*;
import com.school.entity.*;
import com.school.mapper.*;
import com.school.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageMapper messageMapper;
    private final MessageReceiverMapper messageReceiverMapper;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherJobMapper teacherJobMapper;
    private final MessageService messageService;

    /** 我的通知中心 */
    @GetMapping("/mine")
    public Result<List<Map<String, Object>>> mine(@RequestParam(required = false) Integer noticeType) {
        Long uid = UserContext.userId();
        List<MessageReceiver> receivers = messageReceiverMapper.selectList(new LambdaQueryWrapper<MessageReceiver>()
                .eq(MessageReceiver::getReceiverId, uid)
                .orderByDesc(MessageReceiver::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (MessageReceiver mr : receivers) {
            Message msg = messageMapper.selectById(mr.getMessageId());
            if (msg == null) continue;
            if (noticeType != null && !noticeType.equals(msg.getNoticeType())) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("receiverId", mr.getId());
            m.put("messageId", msg.getId());
            m.put("title", msg.getTitle());
            m.put("content", msg.getContent());
            m.put("noticeType", msg.getNoticeType());
            m.put("isRead", mr.getIsRead());
            m.put("isTop", msg.getIsTop());
            m.put("createTime", msg.getCreateTime());
            User pub = userMapper.selectById(msg.getPublisherId());
            m.put("publisher", pub == null ? "系统" : pub.getRealName());
            list.add(m);
        }
        list.sort((a, b) -> {
            int top = Integer.compare((Integer) b.get("isTop"), (Integer) a.get("isTop"));
            if (top != 0) return top;
            return String.valueOf(b.get("createTime")).compareTo(String.valueOf(a.get("createTime")));
        });
        return Result.ok(list);
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long c = messageReceiverMapper.selectCount(new LambdaQueryWrapper<MessageReceiver>()
                .eq(MessageReceiver::getReceiverId, UserContext.userId())
                .eq(MessageReceiver::getIsRead, 0));
        return Result.ok(c);
    }

    @PostMapping("/{receiverId}/read")
    public Result<Void> read(@PathVariable Long receiverId) {
        MessageReceiver mr = messageReceiverMapper.selectById(receiverId);
        if (mr != null) {
            mr.setIsRead(1);
            mr.setReadTime(LocalDateTime.now());
            messageReceiverMapper.updateById(mr);
        }
        return Result.ok();
    }

    /** 发布通知（按角色校验发布范围） */
    @PostMapping("/publish")
    @OpLog(module = "notice", action = "CREATE", targetType = "message")
    public Result<Long> publish(@RequestBody Map<String, Object> body) {
        UserContext ctx = UserContext.get();
        Set<Long> allowedClassIds = allowedPublishClasses(ctx);

        Message msg = new Message();
        msg.setSchoolId(ctx.getSchoolId() == null ? 0L : ctx.getSchoolId());
        msg.setTitle((String) body.get("title"));
        msg.setContent((String) body.get("content"));
        msg.setNoticeType((Integer) body.getOrDefault("noticeType", 4));
        msg.setPublisherId(ctx.getUserId());
        msg.setPublisherRole(ctx.getRole());
        int scope = (Integer) body.getOrDefault("targetScope", 1);
        msg.setTargetScope(scope);
        if (scope == 2) {
            msg.setTargetClassIds(toJson(body.get("classIds")));
        }
        if (scope == 3) {
            msg.setTargetUserIds(toJson(body.get("userIds")));
        }
        msg.setIsTop(0);

        // 学生（班委）/教师只能发本班/任课班；管理员不受限
        if (ctx.getRole() != 4 && (scope == 1)) {
            throw new BusinessException(403, "您无权发布全校通知");
        }
        messageService.publish(msg, ctx.getRole() == 4 ? null : allowedClassIds);
        return Result.ok(msg.getId());
    }

    /** 我发布的通知（管理员/教师/班委） */
    @GetMapping("/sent")
    public Result<List<Message>> sent() {
        return Result.ok(messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getPublisherId, UserContext.userId())
                .orderByDesc(Message::getCreateTime)));
    }

    /** 已读统计（管理员） */
    @GetMapping("/{id}/readers")
    public Result<Map<String, Object>> readers(@PathVariable Long id) {
        List<MessageReceiver> list = messageReceiverMapper.selectList(new LambdaQueryWrapper<MessageReceiver>()
                .eq(MessageReceiver::getMessageId, id));
        long read = list.stream().filter(r -> r.getIsRead() == 1).count();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", list.size());
        m.put("read", read);
        m.put("unread", list.size() - read);
        return Result.ok(m);
    }

    @DeleteMapping("/{id}")
    @OpLog(module = "notice", action = "DELETE", targetType = "message")
    public Result<Void> withdraw(@PathVariable Long id) {
        messageMapper.deleteById(id);
        messageReceiverMapper.delete(new LambdaQueryWrapper<MessageReceiver>().eq(MessageReceiver::getMessageId, id));
        return Result.ok();
    }

    @PostMapping("/{id}/top")
    public Result<Void> top(@PathVariable Long id) {
        Message msg = messageMapper.selectById(id);
        msg.setIsTop(msg.getIsTop() == 1 ? 0 : 1);
        messageMapper.updateById(msg);
        return Result.ok();
    }

    private Set<Long> allowedPublishClasses(UserContext ctx) {
        Set<Long> ids = new LinkedHashSet<>();
        if (ctx.getRole() == 1) {
            Student s = studentMapper.selectById(ctx.getUserId());
            if (s != null && s.getClassId() != null) ids.add(s.getClassId());
        } else if (ctx.getRole() == 2 || ctx.getRole() == 3) {
            teacherJobMapper.selectList(new LambdaQueryWrapper<TeacherJob>()
                    .eq(TeacherJob::getTeacherId, ctx.getUserId()))
                    .forEach(j -> ids.add(j.getClassId()));
        }
        return ids;
    }

    private String toJson(Object o) {
        if (o == null) return "[]";
        if (o instanceof String s) return s;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(o);
        } catch (Exception e) {
            return "[]";
        }
    }
}
