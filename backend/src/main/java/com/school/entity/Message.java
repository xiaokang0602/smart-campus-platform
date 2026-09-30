package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long schoolId;

    private String title;

    private String content;

    private Integer noticeType;

    private Long publisherId;

    private Integer publisherRole;

    private Integer targetScope;

    private String targetClassIds;

    private String targetUserIds;

    private Integer isTop;

    private String attachment;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
