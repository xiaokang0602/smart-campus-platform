package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("friend_guardian")
public class FriendGuardian {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long guardianUserId;

    private Integer isPrimary;

    private LocalDateTime bindTime;

    @TableLogic
    private Integer deleted;
}
