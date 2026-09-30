package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message_receiver")
public class MessageReceiver {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long messageId;

    private Long receiverId;

    private Integer isRead;

    private LocalDateTime readTime;

    @TableLogic
    private Integer deleted;
}
