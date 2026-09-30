package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.entity.*;
import com.school.mapper.ClassInfoMapper;
import com.school.mapper.MessageMapper;
import com.school.mapper.MessageReceiverMapper;
import com.school.mapper.StudentMapper;
import com.school.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 消息通知：按接收人维度落库 message_receiver
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;
    private final MessageReceiverMapper messageReceiverMapper;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;

    /**
     * 发布通知并计算接收人
     */
    public void publish(Message msg, Set<Long> allowedClassIds) {
        Set<Long> receivers = new LinkedHashSet<>();
        int scope = msg.getTargetScope() == null ? 1 : msg.getTargetScope();

        if (scope == 1) {
            // 全校：本校全部用户
            List<User> users = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getSchoolId, msg.getSchoolId()));
            users.forEach(u -> receivers.add(u.getId()));
        } else if (scope == 2) {
            // 指定班级：这些班级的师生
            List<Long> classIds = parseIds(msg.getTargetClassIds());
            for (Long cid : classIds) {
                if (allowedClassIds != null && !allowedClassIds.contains(cid)) {
                    continue; // 越权班级直接忽略
                }
                List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .eq(User::getSchoolId, msg.getSchoolId()));
                for (User u : users) {
                    if (u.getRole() == 1) {
                        Student s = studentMapper.selectById(u.getId());
                        if (s != null && cid.equals(s.getClassId())) {
                            receivers.add(u.getId());
                        }
                    }
                }
            }
        } else {
            // 指定个人
            receivers.addAll(parseIds(msg.getTargetUserIds()));
        }

        messageMapper.insert(msg);
        for (Long rid : receivers) {
            MessageReceiver mr = new MessageReceiver();
            mr.setMessageId(msg.getId());
            mr.setReceiverId(rid);
            mr.setIsRead(0);
            messageReceiverMapper.insert(mr);
        }
    }

    public List<Long> parseIds(String json) {
        List<Long> ids = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return ids;
        }
        String cleaned = json.replaceAll("[\\[\\]\"\\s]", "");
        if (cleaned.isEmpty()) {
            return ids;
        }
        for (String s : cleaned.split(",")) {
            try {
                ids.add(Long.valueOf(s.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }
}
